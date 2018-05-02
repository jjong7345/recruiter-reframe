(ns recruit-app.email-templates.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [clojure.tools.reader.edn :as edn]
            [config.core :refer [env]]))

(defn- templates-url
  "Returns templates url for given type from config"
  [type]
  (-> env :services :templates (get type)))

(defn get-templates [recruiter-id]
  (->> recruiter-id
       (hash-map :recruiter-id)
       (json/generate-string)
       (http/post (templates-url :read))
       (json/generate-string)))

(defn with-fields
  [{:keys [msg-body subject name]} data]
  (merge data {:fields {:template-name    name
                        :template-body    msg-body
                        :template-subject subject}}))

(defn update-template
  [recruiter-id {:keys [template-id] :as request}]
  (->> {:recruiter-id recruiter-id
        :template-id  template-id}
       (with-fields request)
       (json/generate-string)
       (http/post (templates-url :update))
       str))

(defn save-template
  [recruiter-id request]
  (->> {:recruiter-id recruiter-id}
       (with-fields request)
       (json/generate-string)
       (http/post (templates-url :create))
       (json/generate-string)))

(defn delete-template
  [recruiter-id {:keys [template-id]}]
  (->> {:recruiter-id recruiter-id
        :template-id  template-id}
       (json/generate-string)
       (http/post (templates-url :delete))
       str))
