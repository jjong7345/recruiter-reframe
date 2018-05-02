(ns recruit-app.recruiter-admin.subs
  (:require [recruit-app.util.subscription :as subs]
            [re-frame.core :as rf]
            [clojure.string :as string]
            [recruit-app.util.date :as d]
            [recruit-app.util.account :as account]
            [recruit-app.specs.recruiter-admin :as specs]
            [recruit-app.util.recruiter-search :as rs]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]))

(subs/reg-subs "recruiter-admin" [["search" {}] ["pending-recruiters-map" {}]
                                  ["pending-escalated-recruiters-map" {}]
                                  ["recently-approved-recruiters-map" {}]
                                  ["recruiters" {}] ["recently-viewed" []]
                                  ["active-recruiter-id" nil]
                                  ["editing-recruiter" nil]
                                  ["show-errors?" false]
                                  ["admin-note-info-map" {}]
                                  ["editing-admin-note" nil]
                                  ["active-admin-note" ""]
                                  ["breadcrumb" {}]])

(def recruiter-subs [["firstname" ""] ["lastname" ""] ["email" ""]
                     ["profile-status" ""] ["subscribe-date" nil]
                     ["telephone" ""] ["company" {}] ["title" ""]
                     ["job-function" ""] ["recruiter-guest-role" {}]
                     ["street" ""] ["city" ""] ["state-province" ""]
                     ["postal-code" ""] ["country" ""]
                     ["recruiter-website-url" ""] ["paid-membership" {}]
                     ["roles" []] ["contact-preferences" []]
                     ["superuser?" nil]])

(def search-subs [["firstname" ""] ["lastname" ""] ["email" ""]
                  ["recruiter-id" ""] ["company-id" ""] ["company-name" ""]])

(subs/reg-nested-subs "recruiter-admin" "active-recruiter" recruiter-subs)
(subs/reg-subs "recruiter-admin" "editing-recruiter" recruiter-subs)
(subs/reg-subs "recruiter-admin" "search" search-subs)
(subs/reg-subs "recruiter-admin" "breadcrumb" [["label" ""] ["on-click" #()]])

(defn pending-recruiters
  "Returns vector of pending recruiters from map"
  [pending-recruiters-map _]
  (vals pending-recruiters-map))

(defn pending-escalated-recruiters
  "Returns vector of pending escalated recruiters from map"
  [pending-escalated-recruiters-map _]
  (vals pending-escalated-recruiters-map))

(defn recently-approved-recruiters
  "Returns vector of recently approved recruiters from map"
  [recently-approved-recruiters-map _]
  (vals recently-approved-recruiters-map))

(defn active-recruiter
  "Returns recruiter from recruiters map given id"
  [[recruiters recruiter-id] _]
  (get recruiters recruiter-id))

(defn recruiter-name
  "Concatenates first and last name"
  [[firstname lastname] _]
  (str firstname " " lastname))

(defn recruiter-subscribe-date-display
  "Parses subscribe date into proper display format"
  [subscribe-date _]
  (when subscribe-date
    (d/formatted-date :date (d/subscribe-date-time subscribe-date))))

(defn recruiter-company-name
  "Returns name from recruiter company"
  [company _]
  (:name company))

(defn recruiter-job-function-name
  "Returns name from recruiter job function"
  [job-function _]
  (:name job-function))

(defn recruiter-role-name
  "Returns name from recruiter role"
  [role _]
  (:name role))

(defn recruiter-job-function-id
  "Returns id from recruiter job function"
  [job-function _]
  (:id job-function))

(defn recruiter-role-id
  "Returns id from recruiter role"
  [role _]
  (:id role))

(defn recruiter-paid-membership-start-date
  "Returns UTC date time of start date for paid membership start date"
  [{:keys [unredacted-start-date]} _]
  (d/utc-date-time unredacted-start-date))

(defn recruiter-paid-membership-end-date
  "Returns UTC date time of end date for paid membership end date"
  [{:keys [unredacted-end-date] :as m} _]
  (d/utc-date-time unredacted-end-date))

(defn recruiter-superuser-checked?
  "Returns either superuser? or whether or not roles contains superuser"
  [[roles superuser?] _]
  (if (nil? superuser?)
    (-> (map :name roles)
        set
        (contains? "Ladders Superuser"))
    superuser?))

(defn editing?
  "User is in editing view when editing recruiter is not nil"
  [recruiter _]
  (some? recruiter))

(defn editing-admin-note?
  "Admin notes is in editing view when editing-admin-notes is not nil"
  [admin-notes _]
  (some? admin-notes))

(defn form-valid?
  "Validates form using spec"
  [recruiter _]
  (s/valid? ::specs/recruiter-admin-account recruiter))

(defn admin-note-updated-by
  "Returns full name of person who updated Admin Note"
  [{:keys [submitter-id firstname lastname]} _]
  (if (and firstname lastname) (str firstname " " lastname)
                               submitter-id))

(rf/reg-sub
  :recruiter-admin/pending-recruiters
  :<- [:recruiter-admin/pending-recruiters-map]
  pending-recruiters)

(rf/reg-sub
  :recruiter-admin/pending-escalated-recruiters
  :<- [:recruiter-admin/pending-escalated-recruiters-map]
  pending-escalated-recruiters)

(rf/reg-sub
  :recruiter-admin/recently-approved-recruiters
  :<- [:recruiter-admin/recently-approved-recruiters-map]
  recently-approved-recruiters)

(rf/reg-sub
  :recruiter-admin/active-recruiter
  :<- [:recruiter-admin/recruiters]
  :<- [:recruiter-admin/active-recruiter-id]
  active-recruiter)

(rf/reg-sub
  :recruiter-admin/active-recruiter-name
  :<- [:recruiter-admin/active-recruiter-firstname]
  :<- [:recruiter-admin/active-recruiter-lastname]
  recruiter-name)

(rf/reg-sub
  :recruiter-admin/active-recruiter-subscribe-date-display
  :<- [:recruiter-admin/active-recruiter-subscribe-date]
  recruiter-subscribe-date-display)

(rf/reg-sub
  :recruiter-admin/active-recruiter-company-name
  :<- [:recruiter-admin/active-recruiter-company]
  recruiter-company-name)

(rf/reg-sub
  :recruiter-admin/active-recruiter-job-function-name
  :<- [:recruiter-admin/active-recruiter-job-function]
  recruiter-job-function-name)

(rf/reg-sub
  :recruiter-admin/active-recruiter-role-name
  :<- [:recruiter-admin/active-recruiter-recruiter-guest-role]
  recruiter-role-name)

(rf/reg-sub
  :recruiter-admin/active-recruiter-job-function-id
  :<- [:recruiter-admin/active-recruiter-job-function]
  recruiter-job-function-id)

(rf/reg-sub
  :recruiter-admin/active-recruiter-role-id
  :<- [:recruiter-admin/active-recruiter-recruiter-guest-role]
  recruiter-role-id)

(rf/reg-sub
  :recruiter-admin/active-recruiter-paid-membership-start-date
  :<- [:recruiter-admin/active-recruiter-paid-membership]
  recruiter-paid-membership-start-date)

(rf/reg-sub
  :recruiter-admin/active-recruiter-paid-membership-end-date
  :<- [:recruiter-admin/active-recruiter-paid-membership]
  recruiter-paid-membership-end-date)

(rf/reg-sub
  :recruiter-admin/active-recruiter-superuser-checked?
  :<- [:recruiter-admin/active-recruiter-roles]
  :<- [:recruiter-admin/active-recruiter-superuser?]
  recruiter-superuser-checked?)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-name
  :<- [:recruiter-admin/editing-recruiter-firstname]
  :<- [:recruiter-admin/editing-recruiter-lastname]
  recruiter-name)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-subscribe-date-display
  :<- [:recruiter-admin/editing-recruiter-subscribe-date]
  recruiter-subscribe-date-display)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-company-name
  :<- [:recruiter-admin/editing-recruiter-company]
  recruiter-company-name)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-job-function-name
  :<- [:recruiter-admin/editing-recruiter-job-function]
  recruiter-job-function-name)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-role-name
  :<- [:recruiter-admin/editing-recruiter-recruiter-guest-role]
  recruiter-role-name)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-job-function-id
  :<- [:recruiter-admin/editing-recruiter-job-function]
  recruiter-job-function-id)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-role-id
  :<- [:recruiter-admin/editing-recruiter-recruiter-guest-role]
  recruiter-role-id)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-paid-membership-start-date
  :<- [:recruiter-admin/editing-recruiter-paid-membership]
  recruiter-paid-membership-start-date)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-paid-membership-end-date
  :<- [:recruiter-admin/editing-recruiter-paid-membership]
  recruiter-paid-membership-end-date)

(rf/reg-sub
  :recruiter-admin/editing-recruiter-superuser-checked?
  :<- [:recruiter-admin/editing-recruiter-roles]
  :<- [:recruiter-admin/editing-recruiter-superuser?]
  recruiter-superuser-checked?)

(rf/reg-sub
  :recruiter-admin/editing?
  :<- [:recruiter-admin/editing-recruiter]
  editing?)

(rf/reg-sub
  :recruiter-admin/editing-admin-note?
  :<- [:recruiter-admin/editing-admin-note]
  editing-admin-note?)

(rf/reg-sub
  :recruiter-admin/form-valid?
  :<- [:recruiter-admin/editing-recruiter]
  form-valid?)

(rf/reg-sub
  :recruiter-admin/admin-note-updated-by
  :<- [:recruiter-admin/admin-note-info-map]
  admin-note-updated-by)

(rf/reg-sub
  :recruiter-admin/search-params
  :<- [:recruiter-admin/search]
  rs/search-params)