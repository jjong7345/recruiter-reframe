(ns recruit-app.email.api
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [recruit-app.util.http :as h]
            [recruit-app.recruiter.api :as r]
            [recruit-app.util.email :as eu]
            [recruit-app.util.date :as date]))

(defn- send
  "Sends an email with given parameters to api endpoint"
  [params]
  (->> params
       json/generate-string
       h/post-request
       (http/post (-> env :services :email))))

(defn recruiter-data
  "Formats recruiter data for use in email from recruiter profile"
  [{:keys [firstname lastname email company postal_code
           recruiter_id subscribe_date title]}]
  {:firstName     firstname
   :lastName      lastname
   :fullName      (str firstname " " lastname)
   :subscriber_id recruiter_id
   :zipcode       postal_code
   :signup_date   (date/iso-8601-date-est subscribe_date)
   :title         title
   :companyName   (:name company)})

(defn send-email
  "Constructs request and sends email"
  [recruiter-id data type]
  (let [profile (r/recruiter-profile recruiter-id)]
    (send
      (assoc
        (eu/base-transactional-request profile (-> env :emails (get type)))
        :data
        (merge data (recruiter-data profile))))))
