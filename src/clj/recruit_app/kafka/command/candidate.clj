(ns recruit-app.kafka.command.candidate
  (:require [hal.uuid :as uuid]
            [ladders-domains.tracking.candidate :as candidate]))

(defn with-recruiter-id
  "Adds UUID for recruiter-id"
  [recruiter-id command]
  (->> recruiter-id
       (uuid/type-6 "subscriber")
       (assoc command ::candidate/recruiter-id)))

(defn with-candidate-id
  "Adds UUID for candidate-id"
  [candidate-id command]
  (->> candidate-id
       (uuid/type-6 "subscriber")
       (assoc command ::candidate/candidate-id)))
