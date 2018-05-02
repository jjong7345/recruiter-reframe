(ns recruit-app.location.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [config.core :refer [env]]))

(defn locations [q]
  (->> {:chrs q}
       (json/generate-string)
       (http/post (-> env :services :locations :autocomplete))
       (map #(hash-map :name %))
       (json/generate-string)))
