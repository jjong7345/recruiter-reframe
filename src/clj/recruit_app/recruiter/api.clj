(ns recruit-app.recruiter.api
  (:require [clj-http.client :as http]
            [recruit-app.util.http :as h]
            [cheshire.core :as json]
            [config.core :refer [env]]
            [recruit-app.requests.create-recruiter :as cr]
            [clojure.spec.alpha :as s]
            [recruit-app.specs.onboarding :as specs]
            [clojure.set :refer [rename-keys]]
            [recruit-app.ats.api :as ats]
            [recruit-app.superuser.api :as su]
            [taoensso.timbre :as log]
            [clj-time.coerce :as c]
            [clj-time.core :as time]
            [recruit-app.util.date :as dt]
            [ring.util.response :as rr]))

(defn recruiter-profile
  "Retrieves full recruiter profile from api"
  [recruiter-id]
  (-> (str (-> env :services :recruiters :read) "/" recruiter-id)
      h/get
      (assoc :ats-provider (ats/ats-provider recruiter-id))
      (assoc :paid-membership (-> (su/redact-permissions recruiter-id)
                                  :body
                                  (json/parse-string true)))))

(defn request-valid?
  "Validates on either preapproved or non-preapproved spec"
  [{:keys [company-id] :as request}]
  (if company-id
    (s/valid? ::specs/preapproved request)
    (s/valid? ::specs/non-preapproved request)))

(defn- validatable-request
  "Converts request back into format necessary for spec"
  [request]
  (rename-keys request {:firstName   :firstname
                        :lastName    :lastname
                        :companyId   :company-id
                        :companyName :companyname
                        :companyType :companytype
                        :phone       :phonenumber
                        :zip         :zipcode}))

(defn validate-request
  "Validates request based on specs and throws exception if invalid"
  [request]
  (if (request-valid? (validatable-request request))
    request
    (throw (Exception. "Invalid request to create recruiter"))))

(defn create-recruiter
  "Makes API call to create recruiter"
  [request]
  (try
    (->> request
         validate-request
         cr/request
         json/generate-string
         (h/post (-> env :services :recruiters :create)))
    (catch Exception e
      (log/warn (str "Create recruiter failed. Request: " request))
      (throw e))))

(defn email-exists?
  "Checks if given email exists for recruiters"
  [email]
  (->> {:email email}
       json/generate-string
       h/post-request
       (http/post (-> env :services :recruiters :email-exists?))))

(defn pending-recruiters
  "Returns all pending recruiters for certain timeframe (from config)"
  []
  (->> (time/minus (time/now) (time/days (:recruiter-pending-days env)))
       c/to-long
       (hash-map :since)
       json/generate-string
       (h/post (-> env :services :recruiters :pending-recruiters))))

(defn pending-escalated-recruiters
  "Returns all pending escalated recruiters for certain timeframe (from config)"
  []
  (->> (time/minus (time/now) (time/days (:recruiter-pending-escalated-days env)))
       c/to-long
       (hash-map :since)
       json/generate-string
       (h/post (-> env :services :recruiters :pending-escalated-recruiters))))

(defn by-approval-period
  "Fetches recruiters approved in the given time period.
  Date params expressed as millisec."
  ([since]
   (by-approval-period since (c/to-long (time/now))))
  ([since until]
   (->> {:since since
         :until until}
        json/generate-string
        (h/post (-> env :services :recruiters :by-approval-period)))))

(defn approved-past-week
  "Fetches recruiters approved in the past week."
  []
  (by-approval-period (-> (time/now)
                          (time/minus (time/weeks 1))
                          c/to-long)))

(defn update-profile
  "Updates recruiter profile"
  [recruiter-id update-data]
  (http/put
    (str (-> env :services :recruiters :update) "/" recruiter-id)
    (h/post-request (json/generate-string update-data))))

(defn approve
  "Makes request to approve given recruiter"
  [recruiter-id]
  (update-profile recruiter-id {:approved true}))

(defn escalate
  "Makes request to escalate given recruiter"
  [recruiter-id]
  (update-profile recruiter-id {:escalated true}))

(defn decline
  "Makes request to decline given recruiter"
  [recruiter-id]
  (update-profile recruiter-id {:declined true}))

(defn permanently-decline
  "Makes request to permanently decline given recruiter"
  [recruiter-id]
  (update-profile recruiter-id {:permanently-declined true}))

(def update-key-map
  "Map of keys from frontend request to backend format"
  {:state-province        :state_province
   :postal-code           :postal_code
   :recruiter-website-url :recruiter_website_url})

(defn make-superuser
  "Makes update request to give superuser status to recruiter"
  [recruiter-id]
  (http/post
    (-> env :services :recruiters :make-superuser)
    (h/post-request (json/generate-string {:recruiter-id recruiter-id}))))

(defn remove-superuser
  "Makes update request to give superuser status to recruiter"
  [recruiter-id]
  (http/post
    (-> env :services :recruiters :remove-superuser)
    (h/post-request (json/generate-string {:recruiter-id recruiter-id}))))

(defn- contains-superuser-role?
  "Checks roles for superuser role"
  [roles]
  (-> roles
      (->> (map :role_id))
      set
      (contains? (:superuser-role-id env))))

(defn- update-superuser-status
  "Removes superuser? flag if no update needed"
  [{:keys [id roles superuser?] :as request}]
  (cond
    (and superuser? (not (contains-superuser-role? roles))) (make-superuser id)
    (and (not superuser?) (contains-superuser-role? roles)) (remove-superuser id)))

(defn- update-request
  "Creates update map from request parameters"
  [{:keys [recruiter-guest-role job-function] :as params}]
  (-> params
      (rename-keys update-key-map)
      (dissoc :recruiter-guest-role :job-function :superuser?)
      (assoc :recruiter_guest_role_id (:recruiter_guest_role_id recruiter-guest-role))
      (assoc :recruiter_guest_job_function_id (:id job-function))))

(defn- redact-permissions-request
  "Renames keys to proper request format"
  [recruiter-id request]
  (-> request
      (rename-keys {:unredacted-start-date :start-date
                    :unredacted-end-date   :end-date})
      (assoc :recruiter-id recruiter-id)))

(defn update-recruiter
  "Updates recruiter profile data and paid membership dates"
  [{:keys [id paid-membership] :as params} updater-id]
  (update-profile id (update-request (assoc params :updater_id updater-id)))
  (update-superuser-status params)
  (when paid-membership
    (su/save-redact-permissions (redact-permissions-request id paid-membership)))
  {:status 204})

(defn transform-admin-notes-response
  [response]
  (-> (rename-keys response {:reference_id     :recruiter-id
                             :adm_note_id      :adm-note-id
                             :adm_ref_table_id :adm-ref-table-id
                             :notes            :notes
                             :insert_time      :insert-time
                             :submitter_id     :submitter-id})
      (assoc :insert-time (dt/formatted-datestring :date-and-time (:insert_time response)))))

(defn get-admin-note
  "Returns internal admin note for a given recruiter"
  [recruiter-id]
  (-> (str (-> env :services :recruiters :admin-note) "/" recruiter-id)
      h/get
      transform-admin-notes-response))

(defn create-admin-note
  "Creates admin note for a given recruiter"
  [{:keys [recruiter-id notes] :as request} submitter-id]
  (try
    (->> {:recruiter-id recruiter-id
          :notes        notes
          :submitter-id submitter-id}
         json/generate-string
         (h/post (-> env :services :recruiters :create-admin-note)))
    (catch Exception e
      (log/warn (str "Create admin note failed. Request: " request))
      (throw e))))

(defn update-admin-note
  "Updates admin note for a given recruiter"
  [{:keys [recruiter-id notes] :as request} submitter-id]
  (try
    (->> {:recruiter-id recruiter-id
          :notes        notes
          :submitter-id submitter-id}
         json/generate-string
         (h/post (-> env :services :recruiters :update-admin-note)))
    (catch Exception e
      (log/warn (str "Update admin note failed. Request: " request))
      (throw e))))

(defn- search-result
  "Flattens recruiter profile into top level map"
  [{:keys [recruiter_profile recruiter_id] :as result}]
  (-> result
      (dissoc :recruiter_profile :recruiter_id)
      (merge (first recruiter_profile))
      (assoc :subscriber_id recruiter_id)))

(defn search
  "Calls search API with params"
  [params]
  (-> {:search-params (rename-keys params {:offset :from})}
      json/generate-string
      (->> (h/post (-> env :services :recruiters :search)))
      (update :results (partial map search-result))
      json/generate-string
      rr/response))

(defn recruiter-id-by-email
  "Finds recruiter id by email"
  [email]
  (let [{:keys [total results]} (-> {:email email} search h/body-map)]
    (when (> total 0)
      (-> results first :subscriber_id))))
