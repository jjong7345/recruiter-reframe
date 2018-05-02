(ns recruit-app.resume.share-key
  (:require [clj-time.core :as time]
            [clj-time.format :as f]
            [recruit-app.util.http :as h]
            [config.core :refer [env]]
            [clojure.spec.alpha :as s]
            [recruit-app.specs.common :as common]
            [clj-http.client :as http]
            [recruit-app.util.encryption :as enc]
            [cheshire.core :as json]
            [clojure.string :refer [split]]
            [taoensso.timbre :as log]))

(def date-formatter (f/formatter "EEE MMM dd HH:mm:ss z yyyy"))

(defn share-data
  "Returns share data for given key"
  [key]
  (try
    (-> (-> env :services :resumes :share-data)
        (str key)
        h/get)
    (catch Exception e
      (log/warn (str "Invalid resume share key: " key)))))

(defn- within-30-days?
  "Returns whether given date string is within 30 days of now"
  [date-string]
  (time/after?
    (f/parse date-formatter date-string)
    (time/minus (time/now) (time/days 30))))

(defn valid-key?
  "Validates confirm email key"
  [key email]
  (let [{:keys [created emailAddress] :as info} (share-data key)]
    (when info
      (and (= emailAddress email) (within-30-days? created)))))

(defn- share-key
  "Assembles key using secure-id of jobseeker and email"
  [secure-id email]
  (->> (.hashCode email)
       Math/abs
       enc/encrypt-subscriberid
       (str secure-id "_")))

(defn create
  "Creates key to be sent with share resume email"
  [recruiter-id secure-id email]
  (let [key (share-key secure-id email)]
    (http/put
      (str (-> env :services :resumes :share-data) key)
      {:body (json/generate-string
               {:recruiterId  recruiter-id
                :jobseekerId  (enc/decrypt-secureid secure-id)
                :emailAddress email
                :created      (f/unparse date-formatter (time/now))})})
    key))

(defn- key?
  "Validates that key is in proper format"
  [key]
  (let [[part1 part2] (split key #"_")]
    (and (s/valid? ::common/secure-id part1)
         (s/valid? ::common/secure-id part2))))

(s/def ::key (s/and string? key?))
(s/fdef create
        :args (s/cat :recruiter-id ::common/recruiter-id
                     :secure-id ::common/secure-id
                     :email ::common/email)
        :ret ::key)
