(ns recruit-app.util.response
  (:require [cljs.reader :as edn]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.job :as ju]))

(defn keyed-map
  "Returns a map of given collection keyed by given k"
  [k coll]
  (reduce #(assoc %1 (get %2 k) %2) {} coll))

(defn loc-auto-response
  "Parses response from location autocomplete"
  [response]
  (edn/read-string (str response)))

(defn jobs
  "Parses jobs response to json"
  [response]
  (->> response
       (map ju/sanitize-job)
       (keyed-map :job_id)))

(defn job
  "Parses response from get-job to job record"
  [response]
  (when (not-empty response)
    (->> response
         ju/with-unique-jobseekers
         keywordize-keys)))
