(ns recruit-app.ats.export
  (:require [recruit-app.candidates.api :as candidates]
            [recruit-app.util.encryption :as d]
            [recruit-app.resume.api :as resume]
            [cheshire.core :as json]
            [config.core :refer [env]]
            [clj-http.client :as http]))

(defn- candidate-info
  "Returns map of candidate info for secure-id"
  [secure-id]
  (-> secure-id
      candidates/candidate
      :body
      (json/parse-string true)))

(defn- greenhouse-request
  "Returns request sent to greenhouse from info from member service"
  [{:keys [firstname lastname jobseeker-id resume-url]}]
  {:first_name  firstname
   :last_name   lastname
   :external_id jobseeker-id
   :resume      resume-url})

(defn- workable-request
  "Returns request to workable for candidate from info from member service"
  [{:keys [firstname lastname email telephone mobile-phone resume-url]}]
  {:candidate {:name       (str firstname " " lastname)
               :firstname  firstname
               :lastname   lastname
               :email      email
               :phone      (if (seq telephone) telephone mobile-phone)
               :resume_url resume-url}})

(defn- with-public-resume-url
  "Assocs resume-url to candidate info"
  [candidate secure-id recruiter-id]
  (assoc
    candidate
    :resume-url
    (resume/public-resume-url secure-id recruiter-id)))

(defmulti save-candidate :ats-provider)

(defmethod save-candidate "lever"
  [{:keys [api-key user-id] :as params}]
  (http/post
    (str (-> env :lever :base-api-url)
         (-> env :lever :save-candidate)
         "?perform_as="
         user-id
         "&parse=true")
    {:basic-auth api-key
     :multipart  [{:name    "resumeFile"
                   :content (:body (resume/resume params))}]}))

(defmethod save-candidate "greenhouse"
  [{:keys [secure-id recruiter-id api-key]}]
  (http/post
    (str (-> env :greenhouse :base-api-url)
         (-> env :greenhouse :save-candidate))
    {:oauth-token api-key
     :body        (-> secure-id
                      candidate-info
                      (with-public-resume-url secure-id recruiter-id)
                      greenhouse-request
                      json/generate-string)}))

(defmethod save-candidate "workable"
  [{:keys [secure-id subdomain job-id recruiter-id api-key]}]
  (http/post
    (str (-> env :workable :base-api-url)
         "/"
         subdomain
         "/jobs/"
         job-id
         (-> env :workable :save-candidate))
    {:oauth-token api-key
     :body        (-> secure-id
                      candidate-info
                      (with-public-resume-url secure-id recruiter-id)
                      workable-request
                      json/generate-string)}))
