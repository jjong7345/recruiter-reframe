(ns recruit-app.auth.forgot-password
  (:require [ring.util.response :as rr]
            [cheshire.core :as json]
            [recruit-app.util.http :as h]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [recruit-app.recruiter.api :as rec]
            [recruit-app.auth.security :as sec]))

(defn- api-url
  "Returns url from config for given type"
  [type]
  (-> env :services :auth (get type)))

(defn generate-token
  "Generates forgot password token with email"
  [email]
  (->> {:email email}
       json/generate-string
       h/post-request
       (http/post (api-url :generate-forgot-password-token))
       :body))

(defn decrypt-token
  "Calls endpoint to decrypt forgot password token"
  [token]
  (->> {:token token}
       json/generate-string
       (h/post (api-url :decrypt-forgot-password-token))))

(defn token-valid?
  "Checks if given token is valid"
  [token]
  (try
    (decrypt-token token)
    (rr/response "true")
    (catch Exception e (rr/response "false"))))

(defn- update-password
  "Updates password for user with given token"
  [{:keys [token password]}]
  (->> {:token    token
        :password password}
       json/generate-string
       h/post-request
       (http/post (api-url :change-password))
       :body
       read-string))

(defn- auth-request
  "Creates request to authenticate"
  [{:keys [email]} password]
  {:username email
   :password password})

(defn change-password
  "Updates password and authenticates"
  [{:keys [token password] :as request}]
  (if (= 1 (update-password request))
    (-> token
        decrypt-token
        :recruiter-id
        rec/recruiter-profile
        (auth-request password)
        sec/login)
    {:status 500 :body "Failed to update password"}))
