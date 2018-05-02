(ns recruit-app.util.recruiter-search)

(def page-size 10)
(def searchable-keys [:firstname :lastname :email :recruiter-id :company-id
                      :company-name])

(defn- without-empty-strings
  "Removes any keys from map that have empty string values"
  [m]
  (into {} (remove (comp (complement seq) second) m)))

(defn- params-with-ints
  "Will parse values for recruiter-id and company-id to ints"
  [{:keys [recruiter-id company-id] :as params}]
  (cond-> params
          recruiter-id (assoc :recruiter-id (js/parseInt recruiter-id))
          company-id (assoc :company-id (js/parseInt company-id))))

(defn search-params
  "Selects only searchable keys from search db and removes empty strings"
  [params]
  (-> params
      (select-keys searchable-keys)
      without-empty-strings
      params-with-ints))

(defn search-result
  "Flattens recruiter profile into top level map"
  [{:keys [recruiter_profile recruiter_id] :as result}]
  (-> result
      (dissoc :recruiter_profile :recruiter_id)
      (merge (first recruiter_profile))
      (assoc :subscriber_id recruiter_id)))
