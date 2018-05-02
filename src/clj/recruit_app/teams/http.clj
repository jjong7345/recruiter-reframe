(ns recruit-app.teams.http
  (:require [recruit-app.util.http :as h]
            [config.core :refer [env]]
            [clj-http.client :as http]
            [org.purefn.irulan.response :as response]))

(defn url
  "Returns URL for given key within teams services in config"
  [key]
  (-> env :services :teams (get key)))

(defn submit-post-request
  "Submits post request of given type with given params"
  [type request]
  (-> request
      h/edn-request
      (->> (http/post (url type)))
      h/body-map
      ::response/payload))
