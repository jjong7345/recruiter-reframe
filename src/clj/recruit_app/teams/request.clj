(ns recruit-app.teams.request
  (:require [hal.uuid :as uuid]
            [ladders-domains.recruiter.team :as team]
            [clojure.set :refer [rename-keys]])
  (:import [java.util UUID]))

(def param-mapping
  "Mapping of parameter keys to fully qualified keys"
  {:team-id      ::team/team-id
   :recruiter-id ::team/admin-id
   :team-name    ::team/team-name
   :member-id    ::team/recruiter-id})

(defn params-with-member-uuid
  "Converts member id to uuid if present"
  [{:keys [member-id] :as params}]
  (if member-id
    (assoc params :member-id (uuid/type-6 "subscriber" member-id))
    params))

(defn team-request
  "Performs transformation of request to fully qualified CQRS request"
  [params]
  (-> params
      (update :team-id #(UUID/fromString %))
      (update :recruiter-id (partial uuid/type-6 "subscriber"))
      params-with-member-uuid
      (rename-keys param-mapping)
      (assoc ::team/time (System/currentTimeMillis))))
