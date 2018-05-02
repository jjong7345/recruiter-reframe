(ns recruit-app.auth.handlers
  (:require [ring.util.response :as response]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [recruit-app.util.http :as h]))

(defn- auth-url
  "Returns url of given type from config"
  [type]
  (-> env :services :auth (get type)))

(defn- with-content-type
  [content-type request]
  (assoc request :content-type content-type))

(defn- ignore-exceptions
  [request]
  (assoc request :throw-exceptions false))

(defn- auth-response
  "Returns parsed body and status in map"
  [{:keys [body status]}]
  {:body   (json/parse-string body true)
   :status status})

(defn- post-request
  [url body]
  (->> body
       (hash-map :body)
       (with-content-type :json)
       (ignore-exceptions)
       (http/post url)
       (auth-response)))

(defn authenticate
  "Authenticates user and returns access and refresh tokens"
  [{:keys [username password]}]
  (->> {:email    username
        :password password}
       (json/generate-string)
       (post-request (auth-url :authenticate))))

(defn refresh-auth-token
  "Uses refresh-token to retrieve new token-pair"
  [token]
  (->> token
       (hash-map :token)
       (json/generate-string)
       (post-request (auth-url :refresh-token))))

(defn invalidate-refresh-token
  [refresh-token]
  (->> refresh-token
       (hash-map :token)
       (json/generate-string)
       (post-request (auth-url :invalidate-token))))

(defn impersonate
  "Authenticates user and returns access token for supplied recruiter id"
  [{:keys [token recruiter-id]}]
  (->> {:token        token
        :recruiter-id recruiter-id}
       (json/generate-string)
       (post-request (auth-url :impersonate))))

(defn generate-verification-code
  "Generates verification code for recruiter ID to be sent in verify email"
  [recruiter-id]
  (->> recruiter-id
       (hash-map :recruiter-id)
       json/generate-string
       h/post-request
       (http/post (auth-url :generate-code))
       :body))

(defn verified?
  "Returns whether or not code has already been used"
  [code]
  (->> {:code code}
       json/generate-string
       h/post-request
       (http/post (auth-url :verified?))
       :body
       read-string))

(defn authenticate-via-code
  "Authenticates user using verification code"
  [code]
  (->> {:code code}
       json/generate-string
       (post-request (auth-url :authenticate-via-code))))
