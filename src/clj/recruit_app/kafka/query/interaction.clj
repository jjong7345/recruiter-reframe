(ns recruit-app.kafka.query.interaction
  (:require [config.core :refer [env]]
            [hal.uuid :as uuid]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [recruit-app.kafka.api :as cqrs]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

(defn- ri-url
  "Returns recruiter interaction url of given type from config"
  [type recruiter-id]
  (str (-> env :services :kafka :query) (-> env :interaction (get type)) recruiter-id))


(defn ri-interactions
  [recruiter-id type]
  (try
    (->> recruiter-id
         (uuid/type-6 "subscriber")
         (ri-url type)
         cqrs/query)
    (catch Exception e (log/error "Can't fetch recruiter interactions") '())))

(defn ri
  [recruiter-id type]
  (->> (ri-interactions recruiter-id type)
       keys
       (map name)
       (map uuid/int)))

(defn ri-with-timestamp
  [recruiter-id type]
  (let [viewed (ri-interactions recruiter-id type)]
    (map #(vector (uuid/int (name (key %))) (val %)) viewed)))


