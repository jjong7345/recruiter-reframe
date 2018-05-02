(ns recruit-app.email.forgot-password
  (:require [recruit-app.auth.forgot-password :as fp]
            [recruit-app.email.api :as e]
            [config.core :refer [env]]
            [recruit-app.util.encryption :as enc]
            [cemerick.url :as u]))

(def ^:private forgot-password-url
  (str (:host env) "/forgotpasswordlink?token="))

(defn- data
  "Returns data for email"
  [recruiter-id token]
  {:forgotPasswordData {:token                 (u/url-encode token)
                        :forgotPasswordUrlBase forgot-password-url
                        :secureSubscriberId    (enc/encrypt-subscriberid recruiter-id)}})

(defn send-email
  "Sends forgot password email for user with given email"
  [email]
  (let [token (fp/generate-token email)
        recruiter-id (-> token fp/decrypt-token :recruiter-id)]
    (e/send-email
      recruiter-id
      (data recruiter-id token)
      :forgot-password)))
