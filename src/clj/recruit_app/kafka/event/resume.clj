(ns recruit-app.kafka.event.resume
  (:require [hal.uuid :as uuid]
            [ladders-domains.recruiter.resume :as resume]))

(defn with-candidate-id
  "Adds UUID for candidate-id"
  [candidate-id event]
  (->> candidate-id
       (uuid/type-6 "subscriber")
       (assoc event ::resume/candidate-id)))

(defn with-job-location-id
  "Adds UUID for job-location-id if given"
  [job-location-id event]
  (if job-location-id
    (->> job-location-id
         (uuid/type-6 "job_location")
         (assoc event ::resume/job-location-id))
    event))
