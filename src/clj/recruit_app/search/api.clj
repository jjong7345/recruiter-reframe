(ns recruit-app.search.api
  (:require [cheshire.core :as json]
            [config.core :refer [env]]
            [recruit-app.kafka.event :as event]
            [recruit-app.kafka.api :as kafka]
            [recruit-app.search.core :as core]
            [recruit-app.kafka.query :as ri]))

(defn search
  "Performs a brand new search given search criteria"
  [{:keys [recruiter-id criteria] :as req} ladders-user?]
  (let [viewed (ri/viewed recruiter-id)
        contacted (ri/contacted recruiter-id)
        external-search? (not ladders-user?)
        saved-search-id (:search-id criteria)
        search-req (core/format-search-request req viewed contacted external-search?)
        {:keys [total] :as response} (core/search search-req saved-search-id viewed contacted)]
    (kafka/emit-event (event/search req saved-search-id total))
    (json/generate-string response {:escape-non-ascii true})))
