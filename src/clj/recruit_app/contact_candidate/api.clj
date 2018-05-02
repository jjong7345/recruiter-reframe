(ns recruit-app.contact-candidate.api
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [recruit-app.util.http :as h]
            [recruit-app.email.api :as email]
            [recruit-app.recruiter.api :as r]
            [recruit-app.candidates.api :as c]
            [recruit-app.util.encryption :as ru-crypt]
            [recruit-app.util.date :as date]
            [clojure.string :as cs]))

(defn- replace-vars
  [input, js-firstname, recruiter-fullname]
  (-> input
      (cs/replace #"\{js-first-name\}" js-firstname)
      (cs/replace #"\{recruiter-full-name\}" recruiter-fullname)))

(defn- post-request
  [data url]
  (->> data
       json/generate-string
       h/post-request
       (http/post url)))

(defn- should-send-email?
  [member-data]
  ; not opted out of recruiter contact
  ; not global optout optin.email-optin-accepted
  ; not inactive subscription-status
  (let [resp (post-request {:jsid (:jobseeker-id member-data) :param-id 160} (-> env :services :email-prefs :by-id))]
    (if (and (= "true" (:body resp)) (-> member-data :optin :email-optin-accepted) (= "A" (-> member-data :subscription :subscription-status)))
      true
      false)))

(defn- template-data [base-email-req note subject member-data recruiter-data]
  (merge base-email-req
         {:to        (:email member-data)
          :id-to     (:jobseeker-id member-data)
          :recipient {:name    (str (:firstname member-data) " " (:lastname member-data))
                      :address (or (-> env :services :forward-email :recipient-override) (:email member-data))}
          :data      {:contactJobseeker {:note                     (replace-vars note (:firstname member-data) (:fullName recruiter-data))
                                         :recruiterSecureId        (ru-crypt/encrypt-subscriberid (:subscriber_id recruiter-data))
                                         :subject                  (replace-vars subject (:firstname member-data) (:fullName recruiter-data))
                                         :recruiterHomePageUrlBase "https://www.theladders.com/recruiter/"}
                      :firstName        (:firstname member-data)
                      :lastName         (:lastname member-data)
                      :signup_date      (date/iso-8601-date-est (:profile-create-time member-data))
                      :recruiter        (merge recruiter-data
                                               {:secureId (ru-crypt/encrypt-subscriberid (:subscriber_id recruiter-data))})
                      :zipcode          (:zipcode (:address member-data))}}))

(defn- contact-one-candidate
  [js-sid note subject recruiter-data base-email-req]
  (let [member-data (json/parse-string (:body (c/candidate js-sid)) true)]
    (if (should-send-email? member-data)
      (post-request {:mail-info (template-data base-email-req note subject member-data recruiter-data)} (-> env :services :forward-email :initiate-forward))
      {:status 400
       :body (str "Candidate " (:firstname member-data) " " (:lastname member-data) "has opted out")})))

  (defn contact-candidates
    [{:keys [recruiter-id recruiter-email jobseekerIds note subject] :as params}]
    (let [recruiter-data (email/recruiter-data (r/recruiter-profile recruiter-id))
          base-email-req {:name    (-> env :services :forward-email :email-name)
                          :from    recruiter-email
                          :id-from recruiter-id}]
      (map #(contact-one-candidate % note subject recruiter-data base-email-req) jobseekerIds)))
