(ns recruit-app.email.verify-email
  (:require [recruit-app.auth.handlers :as auth]
            [config.core :refer [env]]
            [cemerick.url :as u]
            [recruit-app.email.api :as e]))

(defn- verification-url
  "Assembles verification url based on config"
  [code]
  (-> env
      :host
      (str "/verify/" (u/url-encode code))))

(defn- data
  "Returns map including verification_code for verify email"
  [recruiter-id]
  (let [code (auth/generate-verification-code recruiter-id)]
    {:verification {:verification_code code
                    :verification_url (verification-url code)}}))

(defn send-email
  "Sends email for user to verify email address"
  [recruiter-id]
  (e/send-email recruiter-id (data recruiter-id) :verify-email))
