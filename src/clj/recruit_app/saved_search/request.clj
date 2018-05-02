(ns recruit-app.saved-search.request)

(defn create
  "Flattens request to proper request for create"
  [{:keys [search-criteria search-parameters] :as request}]
  {:new-search (merge
                 (select-keys request [:recruiter-id :search-name])
                 search-criteria
                 search-parameters)})

(defn update-saved-search
  "Wraps saved search request in map for update"
  [request]
  {:saved-search request})
