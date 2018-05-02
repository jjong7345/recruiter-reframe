(ns recruit-app.account.api
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [recruit-app.email.api :as e]
            [clojure.tools.reader.edn :as edn]
            [config.core :refer [env]]))

(defn recruiter-url
  "Returns recruiter url with id attached"
  [recruiter-id type]
  (str (-> env :services :recruiters (get type)) "/" recruiter-id))

(defn update-recruiter-profile
  [recruiter-id req]
  (->> req
       (json/generate-string)
       (http/put (recruiter-url recruiter-id :update))
       (json/generate-string)))

(defn- send-change-password-email
  "Sends change password email for user with given email"
  [recruiter-id]
  (-> recruiter-id
      (e/send-email {} :change-password)
      (json/generate-string)))

(defn change-password
  [recruiter-id req]
  (let [response (update-recruiter-profile recruiter-id req)]
    (send-change-password-email recruiter-id)
    response))