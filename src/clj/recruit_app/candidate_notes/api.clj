(ns recruit-app.candidate-notes.api
  (:require [recruit-app.util.http :as http]
            [cheshire.core :as json]
            [recruit-app.util.encryption :as d]
            [config.core :refer [env]]))

(defn- notes-url
  "Returns url to retrieve recruiter notes for candidate"
  [jobseeker-id recruiter-id]
  (str (-> env :services :candidate-notes) recruiter-id "/" jobseeker-id))

(defn notes-response
  "Returns notes if exists. Catches 400 and returns empty string if not found"
  [url]
  (try
    (http/get url)
    (catch Exception e "")))

(defn get-notes
  "Returns candidate notes for given recruiter and jobseeker"
  [recruiter-id jobseeker-id]
  (-> jobseeker-id
      (d/decrypt-secureid)
      (notes-url recruiter-id)
      (notes-response)
      (json/generate-string)))

(defn save-notes
  "Saves notes via api"
  [recruiter-id {:keys [secure-id notes]}]
  (-> secure-id
      (d/decrypt-secureid)
      (notes-url recruiter-id)
      (http/post (json/generate-string {:note-content notes}))
      (json/generate-string)))
