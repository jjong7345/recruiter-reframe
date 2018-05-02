(ns recruit-app.inventory.api
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [recruit-app.util.http :as h]))

(defn inventory
  "Returns inventory for recruiter-id"
  [recruiter-id]
  (->> recruiter-id
       (str (-> env :services :inventory :read))
       (http/get)))

(defn- save-inventory-url
  "Returns correct url based on action"
  [action]
  (case action
    "purchase-inventory" (-> env :services :inventory :purchase)
    "use-inventory" (-> env :services :inventory :use)))

(defn save-inventory
  "Makes API call to save inventory for recruiter"
  [{:keys [action] :as params}]
  (->> params
       json/generate-string
       h/post-request
       (http/post (save-inventory-url action))))
