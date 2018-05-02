(ns recruit-app.util.projects
  (:require [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [cljs-time.core :as t]
            [cljs.reader :as edn]
            [clojure.string :as cs]
            [clojure.set :refer [rename-keys]]
            [recruit-app.util.sort :as s]))

(def date-formatter (f/formatter "MMM dd, yyyy"))

(defn formatted-date
  "Returns date formatted as MMM dd, yyyy"
  [date-time]
  (f/unparse date-formatter date-time))

(defn json->clj
  "Converts json response to clj sequences"
  [json]
  (-> json str edn/read-string))

(defn project
  "Returns formatted project. The use of multiple keys is to support RL endpoint"
  [{:keys [projectId containerId size candidate_count date_created createTime title]}]
  (let [timestamp (or date_created createTime)]
    {:title           title
     :projectId       (or projectId containerId)
     :date_created    timestamp
     :candidate_count (or candidate_count size)}))

(defn with-project
  "Associates project to projects using projectId"
  [projects project]
  (assoc projects (:projectId project) project))


(defn projects
  "Returns formatted projects from server response"
  [response]
  (->> response
       (json->clj)
       (map project)
       (reduce with-project {})))

(defn new-project
  "Returns map of new project record after creation"
  [title response]
  {:projectId       (:project-id response)
   :title           title
   :date_created    (c/to-long (t/now))
   :candidate_count 0})

(defn with-new-project
  "Formats project from response and adds to projects"
  [projects title response]
  (->> response
       (new-project title)
       (with-project projects)))

(def update-project-candidates-request {:folders {:selectedIds   []
                                                  :unselectedIds []}
                                        :jreqs   {:selectedIds   []
                                                  :unselectedIds []}
                                        :roles   {:selectedIds   []
                                                  :unselectedIds []}})

(defn add-candidate-request
  "Request to add candidate to project per RL endpoint"
  [project-id]
  (->> project-id
       str
       (update-in update-project-candidates-request [:folders :selectedIds] conj)))

(defn remove-candidate-request
  "Request to remove candidate from project per RL endpoint"
  [project-id]
  (->> project-id
       str
       (update-in update-project-candidates-request [:folders :unselectedIds] conj)))

(defn candidate-projects
  "Parses http response to return set of project ids for candidate"
  [response]
  (zipmap (keys response) (map set (vals response))))

(defn candidate-index
  "Returns index of candidate within project list"
  [candidates secure-id]
  (if candidates
    (->> candidates
         (keep-indexed
           (fn [idx candidate]
             (when (= secure-id (-> candidate :subscriber :secureId)) idx)))
         first)
    0))

(defn profile-route
  "Returns route to candidate profile page"
  [{:keys [candidates-map curr-project-id]} idx secure-id]
  (str "/candidate/project/" curr-project-id "/" idx "?jobseekerId=" secure-id))

(def custom-formatter (f/formatter "MMM dd, yyyy hh:mm:ss a"))

(def unparse-formatter (f/formatter "yyyy-MM-dd'T'hh:mm:ssZ"))

(defn parsed-date
  [string]
  (when string
    (f/parse unparse-formatter string)))

(defn format-date [date]
  (when (seq date)
    (f/unparse unparse-formatter (parsed-date date))))

(defn desired-compensation
  "Returns formatted string for desired compensation of candidate"
  [{:keys [compensationRank]}]
  (-> compensationRank
      :recruiterProfileDescription
      (cs/replace #"k" "K")))

(defn email-candidate
  "Renames keys for candidate to be used in email modal"
  [candidate]
  (rename-keys candidate {:secureId  :secure-id
                          :firstName :jobSeekerFirstName
                          :lastName  :jobSeekerLastName}))

(defn sorted-history
  "Returns history sorted by start date for project list"
  [history]
  (when history
    (sort-by
      #(parsed-date (:start-date %))
      s/after?
      history)))
