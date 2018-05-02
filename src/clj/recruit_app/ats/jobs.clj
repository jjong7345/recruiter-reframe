(ns recruit-app.ats.jobs
  (:require [clj-http.client :as http]
            [config.core :refer [env]]))

(defmulti jobs :ats-provider)

(defmethod jobs "workable"
  [{:keys [subdomain api-key]}]
  (http/get
    (str (-> env :workable :base-api-url)
         "/"
         subdomain
         (-> env :workable :jobs)
         "?state=published")
    {:oauth-token api-key}))
