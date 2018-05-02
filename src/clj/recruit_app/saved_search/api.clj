(ns recruit-app.saved-search.api
  (:require [cheshire.core :as json]
            [recruit-app.saved-search.core :as core]
            [recruit-app.saved-search.request :as request]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [ring.util.response :as rr]))

(defn saved-searches
  "Returns all saved searches for recruiter"
  [recruiter-id]
  (->> (core/saved-searches recruiter-id)
       (sort-by
         (comp (partial f/parse (f/formatters :date-time)) :update-time)
         time/before?)
       json/generate-string))

(defn saved-search
  "Returns saved search by ID"
  [saved-search-id]
  (json/generate-string (core/saved-search saved-search-id)))

(defn update-saved-search
  "Performs an update of existing saved search"
  [params]
  (-> (request/update-saved-search params)
      core/update-saved-search
      json/generate-string))

(defn create
  "Creates new saved search with criteria"
  [{:keys [reporting] :as params}]
  (let [{:keys [saved-search]} (core/create (request/create params))]
    (update-saved-search (assoc saved-search :reporting reporting))))

(defn delete
  "Deletes a saved search specified by saved-search-id and a recruiter-id"
  [saved-search-id]
  (json/generate-string (core/delete-saved-search saved-search-id)))

(defn delete-multi
  "Deletes saved searches specified in a list of saved-search-id's"
  [{:keys [saved-search-ids]}]
  (doseq [id saved-search-ids]
    (core/delete-saved-search id))
  (rr/response "OK"))
