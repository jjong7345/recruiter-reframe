(ns recruit-app.suggested-candidates.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [config.core :refer [env]]
            [recruit-app.util.encryption :as e]))

(defn add-secure-id
  [jobseeker-map]
  (->> (e/encrypt-subscriberid (:jobseeker-id jobseeker-map))
       (assoc jobseeker-map :secure-id)))

(defn suggested [job-id]
  "Returns suggested candidates for the job-id specified"
  (try (->> {:parameters {:job-id job-id}}
            (json/generate-string)
            (http/post (-> env :services :jobseekers :suggested-candidates))
            (:jobseekers)
            (map add-secure-id))
       (catch Exception e [])))