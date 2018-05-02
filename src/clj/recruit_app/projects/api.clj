(ns recruit-app.projects.api
  (:require [cheshire.core :as json]
            [secretary.core :as sec]
            [recruit-app.util.http :as h]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [ladders-domains.recruiter.save-candidates :as candidate]
            [recruit-app.candidates.api :as candidates]
            [hal.uuid :as uuid]
            [org.purefn.irulan.command :as cmd]
            [recruit-app.util.encryption :as d]
            [recruit-app.kafka.api :as k]))

(defn projects-url
  "Returns url to retrieve projects"
  [recruiter-id]
  (str (-> env :services :projects :project-view) "?key=" (uuid/type-6 "subscriber" recruiter-id)))

(defn projects-vector
  "Returns vector of maps with a projectId key"
  [projects]
  (mapv #(assoc (val %) :projectId (name (key %))) projects))

(defn project-mapper
  "Maps projects data to correct keys"
  [{:keys [projectId project-name time project-count]}]
  {:projectId       projectId
   :title           project-name
   :date_created    time
   :candidate_count project-count})

(defn projects
  "Returns projects"
  [recruiter-id]
  (->> recruiter-id
       projects-url
       (k/query)
       projects-vector
       (mapv project-mapper)
       (json/generate-string)))

(defn projects-candidates-url
  "Returns url to retrieve project candidates"
  [recruiter-id]
  (str (-> env :services :projects :project-candidates-view) "?key=" (uuid/type-6 "subscriber" recruiter-id)))

(defn project-candidates-map
  "Returns vector of all project and candidates ids"
  [recruiter-id]
  (-> recruiter-id
      (projects-candidates-url)
      (k/query)))

(defn project-candidates-ids
  "Returns vector of all numerical candidate ids"
  [recruiter-id project-id]
  (mapv
    (fn [[key value]] (uuid/int (name key)))
    (get (project-candidates-map recruiter-id) (keyword project-id))))

(defn candidates-list
  "Returns vector of candidate maps for selected project from jobseeker"
  [recruiter-id project-id]
  (-> (project-candidates-ids recruiter-id project-id)
      (->> (map d/encrypt-subscriberid))
      candidates/candidates
      :body
      (json/parse-string true)))

(defn candidates-mapper
  "Maps candidates data to correct keys"
  [{:keys [email address firstname telephone optin jobseeker-id lastname
           current-compensation experience education last-resume-update
           last-email-open last-login last-profile-update job-preferences]}]
  {:subscriber {:email              email
                :city               (:city address)
                :firstName          firstname
                :stateProvince      (:state address)
                :street             (:street address)
                :telephone          telephone
                :emailOptinAccepted (:email-optin-accepted optin)
                :id                 jobseeker-id
                :secureId           (d/encrypt-subscriberid jobseeker-id)
                :lastName           lastname
                :postalCode         (:zipcode address)
                :compensationRank   {:recruiterProfileDescription (:description current-compensation)}
                :lastResumeUpdate   last-resume-update
                :lastEmailOpen      last-email-open
                :lastLogin          last-login
                :lastProfileUpdate  last-profile-update}
   :profile    {:experience      (:experiences experience)
                :education       (:educations education)
                :job-preferences job-preferences}})

(defn project-candidates
  [recruiter-id project-id]
  (json/generate-string (mapv candidates-mapper (:jobseekers (candidates-list recruiter-id project-id)))))

(defn projects-for-candidate-url
  "Returns url to retrieve projects"
  [recruiter-id secure-id]
  (str (-> env :services :projects :candidate-projects-view) "?key=" (uuid/type-6 "subscriber" recruiter-id)))

(defn candidate-conversion
  "Return encrypted candidate id"
  [id]
  (d/encrypt-subscriberid (uuid/int (name id))))

(defn candidate-key-conversion
  "Returns vector of converted keys"
  [response]
  (mapv candidate-conversion (keys response)))

(defn projects-for-candidate
  "Returns map of candidate ids and project ids"
  [recruiter-id secure-id]
  (let [url (projects-for-candidate-url recruiter-id secure-id)
        query (k/query url)]
    (json/generate-string (zipmap (-> query candidate-key-conversion) (-> query vals)))))

(defn rename-project-payload [project-id recruiter-id project-name]
  (candidate/rename-project 1
                            {::candidate/project-id   (java.util.UUID/fromString project-id)
                             ::candidate/recruiter-id (uuid/type-6 "subscriber" recruiter-id)
                             ::candidate/project-name project-name
                             ::candidate/time         (System/currentTimeMillis)}))

(defn create-project-payload [recruiter-id project-name]
  (candidate/create-project 1
                            {::candidate/project-id   (java.util.UUID/randomUUID)
                             ::candidate/recruiter-id (uuid/type-6 "subscriber" recruiter-id)
                             ::candidate/project-name project-name
                             ::candidate/time         (System/currentTimeMillis)}))

(defn remove-project-payload [project-id recruiter-id]
  (candidate/remove-project 1
                            {::candidate/project-id   (java.util.UUID/fromString project-id)
                             ::candidate/recruiter-id (uuid/type-6 "subscriber" recruiter-id)
                             ::candidate/time         (System/currentTimeMillis)}))

(defn remove-candidate-payload [recruiter-id project-id jobseeker-id]
  (candidate/remove-candidate 1
                              {::candidate/recruiter-id (uuid/type-6 "subscriber" recruiter-id)
                               ::candidate/project-id   (java.util.UUID/fromString project-id)
                               ::candidate/candidate-id (uuid/type-6 "subscriber" (d/decrypt-secureid jobseeker-id))
                               ::candidate/time         (System/currentTimeMillis)}))

(defn save-candidate-payload [recruiter-id project-id jobseeker-id]
  (candidate/save-candidate 1
                            {::candidate/recruiter-id (uuid/type-6 "subscriber" recruiter-id)
                             ::candidate/project-id   (java.util.UUID/fromString project-id)
                             ::candidate/candidate-id (uuid/type-6 "subscriber" (d/decrypt-secureid jobseeker-id))
                             ::candidate/time         (System/currentTimeMillis)}))

(defn edit-project
  "Renames project folder"
  [recruiter-id {:keys [project-id name]}]
  (k/command (rename-project-payload project-id recruiter-id name)))

(defn add-project-map
  "Maps project data to correct keys"
  [response]
  {:project-id (:ladders-domains.recruiter.save-candidates/project-id response)
   :title      (:ladders-domains.recruiter.save-candidates/project-name response)})

(defn add-project
  "Adds new project for recruiter"
  [recruiter-id {:keys [folderName]}]
  (-> (k/command (create-project-payload recruiter-id folderName))
      :body
      (json/parse-string true)
      :org.purefn.irulan.response/payload
      :ladders-domains.recruiter.save-candidates/message
      (add-project-map)
      (json/generate-string)))

(defn delete-project
  "Delete project"
  [recruiter-id project-id]
  (k/command (remove-project-payload project-id recruiter-id)))

(defn remove-candidate-from-project
  "Removes given secure-id from given project"
  [recruiter-id project-id secure-id]
  (k/command (remove-candidate-payload recruiter-id project-id secure-id)))

(defn add-candidate-to-project
  "Removes given secure-id from given project"
  [recruiter-id project-id secure-id]
  (k/command (save-candidate-payload recruiter-id project-id secure-id)))



