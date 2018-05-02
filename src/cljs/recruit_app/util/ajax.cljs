(ns recruit-app.util.ajax
  (:require [ajax.core :as a]
            [ajax.protocols :refer [-body]]))

(defn- form-data
  [params]
  (let [key (-> params keys first)]
    (doto
      (js/FormData.)
      (.append (name key) (get-in params [key :blob]) (get-in params [key :filename])))))

(defn multipart-form-request-format
  []
  {:content-type nil
   :write        form-data})

(defn json-response-format
  "Returns nil if response is empty, otherwise runs json-response-format from ajax."
  [options]
  (let [{:keys [content-type read]} (a/json-response-format options)]
    {:content-type content-type
     :read         #(when (not-empty (-body %)) (read %))}))
