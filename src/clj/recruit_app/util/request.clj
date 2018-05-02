(ns recruit-app.util.request
  (:require [clojure.string :as s]))

(defn recruiter-id
  "Returns recruiter-id from request"
  [{:keys [session]}]
  (-> session :user :recruiter-id))

(defn email
  "Returns email from request."
  [{:keys [session]}]
  (-> session :user :email))

(defn ladders-user?
  "Returns true if the user has a theladders.com email domain"
  [email]
  (s/includes? email "theladders.com"))
