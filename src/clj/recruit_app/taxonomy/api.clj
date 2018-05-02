(ns recruit-app.taxonomy.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as h]
            [config.core :refer [env]]))

(defn role-labels
  "Returns labels for given role ids"
  [role-ids]
  (->> {:roles role-ids}
       json/generate-string
       (h/post (-> env :services :taxonomy :role-labels))
       (map :label)))

(defn function-ids-to-disciplines
  "Function to convert function-ids to disciplines"
  [function-ids]
  (let [role-ids {:roles function-ids}]
    (->> role-ids
         (json/generate-string)
         (h/post (-> env :services :taxonomy :roles-to-last-specialty-ids))
         (vec))))

(defn disciplines-to-function-ids
  "Function which uses the taxonomy service to convert disciplines from existing/new saved search to the latest set of Roles based."
  [disciplines]
  (let [payload {:specialties disciplines
                 :version     0}]
    (->> payload
         (json/generate-string)
         (h/post (-> env :services :taxonomy :specialties-to-last-role-ids))
         (vec))))