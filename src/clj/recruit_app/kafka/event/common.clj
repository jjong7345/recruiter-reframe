(ns recruit-app.kafka.event.common
  (:require [hal.uuid :as uuid]
            [ladders-domains.recruiter.common :as common]
            [clj-time.coerce :as c]
            [clj-time.core :as time]))

(defn with-recruiter-id
  "Adds recruiter-id uuid to kafka event"
  [recruiter-id event]
  (->> recruiter-id
       (uuid/type-6 "subscriber")
       (assoc event ::common/recruiter-id)))

(defn with-timestamp
  "Adds current timestamp to event"
  [key event]
  (assoc event key (c/to-long (time/now))))
