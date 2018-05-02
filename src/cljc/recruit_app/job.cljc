(ns recruit-app.job
  (:require #?(:clj [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
    #?(:clj
                    [clj-time.format :as f]
       :cljs [cljs-time.format :as f])))

(defn date-expired?
  "Returns whether publication_date was 56 days before now"
  [{:keys [publication_date]}]
  (try
    (t/after?
      (t/now)
      (t/plus
        (f/parse (f/formatters :date-time-no-ms) publication_date)
        (t/days 56)))
    (catch #?(:clj Exception :cljs :default) e false)))

(defn expiring-within-7-days?
  "Returns whether publication_date is within 7 days of expiration (between 49 and 56 days before now)"
  [{:keys [publication_date]}]
  (try
    (t/within?
      (t/interval (t/plus
                    (f/parse (f/formatters :date-time-no-ms) publication_date)
                    (t/days 49)) (t/plus
                                   (f/parse (f/formatters :date-time-no-ms) publication_date)
                                   (t/days 56)))
      (t/now))
    (catch #?(:clj Exception :cljs :default) e false)))

(defn approved?
  "Job is approved if the status is Approved"
  [{:keys [job_status]}]
  (= "Approved" job_status))

(defn active?
  "Job is considered active if it is approved and not expired"
  [job]
  (and (approved? job) (not (date-expired? job))))

(defn pending?
  "Checks if job_status is Pending"
  [{:keys [job_status]}]
  (= "Pending" job_status))

(defn rejected?
  "Checks if job_status is Rejected"
  [{:keys [job_status]}]
  (= "Rejected" job_status))

(defn filled?
  "Checks if job_status is Filled"
  [{:keys [job_status]}]
  (= "Filled" job_status))

(defn expired?
  "Job is only considered expired if it is approved and date is expired"
  [job]
  (and (approved? job) (date-expired? job)))

(defn removed?
  "Job is removed if status is Cancelled or Deleted or it is expired"
  [{:keys [job_status] :as job}]
  (or (= "Cancelled" job_status)
      (= "Deleted" job_status)
      (= "Filled" job_status)
      (expired? job)))
