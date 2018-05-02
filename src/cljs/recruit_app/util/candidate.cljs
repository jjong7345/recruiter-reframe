(ns recruit-app.util.candidate
  (:require [recruit-app.util.date :as f]
            [clojure.string :as string]
            [cljs-time.format :as tf]
            [recruit-app.util.number_format :as nf]
            [recruit-app.util.projects :as p]
            [recruit-app.util.date :as date]))

(defn date-range
  "Returns formatted date-range for job history item"
  [start-date end-date]
  (when start-date
    (let [start-date (f/formatted-date :year (f/db-date-time start-date))
          end-date (f/formatted-date :year (f/db-date-time end-date))]
      (str start-date "-" (if (and (seq end-date) (not= "1970" end-date)) end-date "Present")))))

(defn company-and-time
  "Returns string of company name and time in role"
  [company time-in-role]
  (cond-> ""
          (seq company) (str company)
          (and (seq company) (seq time-in-role)) (str " • ")
          (seq time-in-role) (str time-in-role)))

(defn job-details
  "Returns formatted job details for history"
  [title company-name]
  (cond-> ""
          (seq title) (str title ", ")
          (seq company-name) (str company-name)))

(defn previous-job
  "Returns properly formatted job history record"
  [{:keys [title company_name start_date end_date]}]
  {:details      (job-details title company_name)
   :time-in-role (date-range start_date end_date)})

(defn history
  "Formats history record from candidate"
  [{:keys [history]}]
  (->> history
       (sort-by :start_date >)
       (map previous-job)))

(defn candidate-name
  "Returns formatted candidate name"
  [{:keys [first-name last-name]}]
  (str first-name " " last-name))

(defn candidate-location
  "Returns formatted candidate location"
  [{:keys [city state]}]
  (cond-> city
          state (str ", " state)))

(defn active-candidate
  "Returns active candidate from db"
  [{:keys [active-id candidates]}]
  (get candidates active-id))

(defn email-recipient
  "Returns candidate info formatted for email"
  [{:keys [first-name last-name secure-id]}]
  {:secure-id          secure-id
   :jobSeekerFirstName first-name
   :jobSeekerLastName  last-name})

(defn resume-filename
  "Returns filename for candidate resume"
  [{:keys [first-name last-name]}]
  (str "resume_" first-name last-name ".pdf"))

(defn share-resume-request
  "Returns request to share resume from :share-resume in db.
  FYI, the reason for both resume-id and resume-version is to support RL"
  [{:keys [emails message jobseeker-id resume-version job-location-id]
    :or   {message ""}}]
  {:recipients      emails
   :message         message
   :resume-id       0
   :jobseekerId     jobseeker-id
   :resume-version  resume-version
   :job-location-id job-location-id})

(defn list-candidate-history-item
  "Returns formatted history item for list candidate"
  [{:keys [title companyName startDate endDate]}]
  {:title        title
   :company_name companyName
   :start_date   (p/format-date startDate)
   :end_date     (p/format-date endDate)})

(defn list-candidate
  "Returns list candidate formatted for use in profile page"
  [{:keys [subscriber profile] :as candidate}]
  (let [{:keys [id secureId firstName lastName compensationRank
                city stateProvince lastEmailOpen lastResumeUpdate lastLogin
                lastProfileUpdate]} subscriber
        {:keys [education experience job-preferences]} profile]
    {:id                  id
     :secure-id           secureId
     :desired             (p/desired-compensation subscriber)
     :first-name          firstName
     :last-name           lastName
     :location            {:city city :state stateProvince}
     :apply-time          nil
     :education           (map :educationDegree education)
     :history             (map list-candidate-history-item experience)
     :years-experience    (:years-of-experience job-preferences)
     :last-email-open     lastEmailOpen
     :last-resume-update  lastResumeUpdate
     :last-login          lastLogin
     :last-profile-update lastProfileUpdate}))

(defn jobseeker-previous-job
  "Returns properly formatted job history record"
  [{:keys [title company start-date end-date]}]
  {:title        title
   :company_name company
   :start_date   start-date
   :end_date     end-date})

(defn jobseeker->candidate
  "Converts jobseeker service response to candidate record"
  [{:keys [firstname lastname jobseeker-id current-compensation
           address experience last-email-open last-resume-update last-login last-profile-update] :as jobseeker} secure-id]
  (when jobseeker
    {:id                  jobseeker-id
     :secure-id           secure-id
     :desired             (:description current-compensation)
     :first-name          firstname
     :last-name           lastname
     :location            address
     :education           nil
     :history             (map jobseeker-previous-job (:experiences experience))
     :last-email-open     last-email-open
     :last-resume-update  last-resume-update
     :last-login          last-login
     :last-profile-update last-profile-update}))

(defn last-active-date
  [& dates]
  (when (not-every? nil? dates)
    (date/most-recent-date dates)))
