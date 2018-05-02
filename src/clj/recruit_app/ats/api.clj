(ns recruit-app.ats.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [ring.util.response :as rr]
            [recruit-app.ats.export :as export]
            [config.core :refer [env]]
            [taoensso.timbre :as log]
            [recruit-app.ats.jobs :as ats-jobs]
            [recruit-app.specs.ats :as specs]))

(defn ats-url
  "Returns ATS url"
  [type]
  (-> env :services :ats (get type)))

(defn lever-url
  "Returns LEVER api url"
  []
  (str (-> env :lever :base-url)))

(defn get-settings
  [recruiter-id]
  (-> (ats-url :get-settings)
      (http/get {:query-params {"recruiter-id" recruiter-id}})
      (json/generate-string)))

(defn save-candidate
  "Attempts to save candidate to ATS"
  [{:keys [recruiter-id secure-id] :as params}]
  (try
    (export/save-candidate
      (merge params (json/parse-string (get-settings recruiter-id) true)))
    (json/generate-string {:success true})
    (catch Exception e
      (log/error e :ats-export params)
      (json/generate-string "{ \"code\" : \"BadRequestError\" }"))))

(defn save-settings
  [req]
  (->> req
       (json/generate-string)
       (http/post (ats-url :save-settings))
       (json/generate-string)))

(defn get-users-lever
  [req]
  (-> (lever-url)
      (http/get {:basic-auth [(:api-key req) ""]})
      (json/generate-string)))

(defn get-greenhouse-url
  []
  (rr/response (str (-> env :greenhouse :base-url)
                    "?response_type=code&scope=candidates.create+candidates.view+jobs.view&redirect_uri="
                    (:host env)
                    "/%23/account&client_id="
                    (-> env :greenhouse :secret))))

(defn ats-provider
  "Returns provider from ATS settings if provider is implemented"
  [recruiter-id]
  (-> (get-settings recruiter-id)
      (json/parse-string true)
      :ats-provider
      specs/implemented-providers))

(defn jobs
  "Returns jobs for recruiter's ATS provider"
  [recruiter-id]
  (try
    (-> (get-settings recruiter-id)
        (json/parse-string true)
        ats-jobs/jobs)
    (catch Exception e
      (json/generate-string ""))))
