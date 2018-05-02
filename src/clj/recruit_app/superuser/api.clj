(ns recruit-app.superuser.api
  (:require [recruit-app.util.http :as h]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [cheshire.core :as json]))

(defn redact-permissions
  "Fetches redact permissions for recruiter-id from api URL"
  [recruiter-id]
  (->> (str recruiter-id)
       (hash-map :recruiter-id)
       json/generate-string
       h/post-request
       (http/post (-> env :services :superuser :read-redact-permissions))))

(defn save-redact-permissions
  "Saves redact permissions for recruiter

  Params:
    * recruiter-id int
    * start-date int Timestamp
    * end-date int Timestamp"
  [params]
  (->> params
       json/generate-string
       h/post-request
       (http/post (-> env :services :superuser :save-redact-permissions))))

(defn remove-redact-permissions
  "Fetches redact permissions for recruiter-id from api URL"
  [recruiter-id]
  (->> (str recruiter-id)
       (hash-map :recruiter-id)
       json/generate-string
       h/post-request
       (http/post (-> env :services :superuser :remove-redact-permissions))))
