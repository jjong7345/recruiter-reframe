(ns recruit-app.saved-search.core
  (:require [taoensso.timbre :as log]
            [recruit-app.util.http :as http]
            [config.core :refer [env]]
            [recruit-app.util.http :as h]
            [cheshire.core :as json]))

(defn saved-searches
  "Function to retrieve all existing saved searches for a given recruiter id"
  [recruiter-id]
  (try
    (-> (str (-> env :services :saved-search :fetch-all) recruiter-id)
        http/get
        :saved-searches)
    (catch Exception e
      (log/error
        "Could not retrieve saved searches for recruiter-id: "
        recruiter-id)
      [])))

(defn saved-search
  "Returns saved search by id"
  [saved-search-id]
  (h/get (str (-> env :services :saved-search :fetch-one) saved-search-id)))

(defn create
  "Function to persist a new search to KV (ace ventura)"
  [{:keys [new-search] :as request}]
  (try
    (http/post
      (-> env :services :saved-search :force-add)
      (json/generate-string request))
    (catch Exception e
      (log/error
        "Could not create saved search for recruiter: "
        (:recruiter-id new-search)
        " Reason: "
        (.getMessage e)))))

(defn update-saved-search
  "Function to persist a saved search to KV (ace ventura)"
  [{:keys [saved-search] :as request}]
  (try
    (http/post
      (-> env :services :saved-search :update)
      (json/generate-string request))
    (catch Exception e
      (log/error
        "Could not update saved search for recruiter "
        (:recruiter-id saved-search)
        " Reason: "
        (.getMessage e)))))

(defn delete-saved-search
  "Function to delete a saved search for a given saved-search-id and recruiter id"
  [saved-search-id]
  (->> (json/generate-string {:search-id saved-search-id})
       (http/post (-> env :services :saved-search :delete))
       h/as-json))
