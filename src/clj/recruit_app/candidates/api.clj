(ns recruit-app.candidates.api
  (:require [cheshire.core :as json]
            [recruit-app.util.encryption :as d]
            [clj-http.client :as http]
            [recruit-app.util.http :as h]
            [config.core :refer [env]]))

(defn- jobseekers-url
  "Returns url of type from config"
  [type]
  (-> env :services :jobseekers (get type)))

(defn- candidate-request
  "Returns request to fetch candidate"
  [jobseeker-id]
  (->> {:jsid jobseeker-id}
       json/generate-string
       (hash-map :body)
       (merge {:content-type :json})))

(defn candidate
  "Fetches candidate by secure-id"
  [secure-id]
  (->> secure-id
       d/decrypt-secureid
       candidate-request
       (http/post (jobseekers-url :read))
       h/as-json))

(defn candidates
  "Fetches candidate by secure-id"
  [secure-ids]
  (->> secure-ids
       (map d/decrypt-secureid)
       (hash-map :jsids)
       json/generate-string
       h/post-request
       (http/post (jobseekers-url :read-multi))))
