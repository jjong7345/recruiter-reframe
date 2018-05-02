(ns recruit-app.slack.api
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.reader.edn :as edn]
            [recruit-app.recruiter.api :as rec]
            [config.core :refer [env]]
            [clj-time.format :as f]
            [clj-time.core :as time]))

(defn- slack-url
  "get slack url for a given channel"
  [channel]
  (-> env
      :slack
      (get (keyword channel))))

(defn- post-slack
  "Post a message to slack channel"
  [channel message]
  (-> channel
      slack-url
      (http/post {:body         (json/generate-string {:text message})
                  :content-type :json})
      :body))

(defn slack-referral-hiring
  "Sends referral hiring info to slack channel"
  [{:keys [fullname email company referral]}]
  (post-slack
    :referral-hiring
    (format
      "HR: %s, Email: %s, Company: %s, Bonus: %s, Date: %s"
      fullname
      email
      company
      referral
      (f/unparse (f/formatters :rfc822) (time/now)))))
