(ns recruit-app.recruiter.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as rs]
            [cljs-time.coerce :as c]
            [recruit-app.util.uri :as u]
            [clojure.string :as cs]
            [recruit-app.util.recruiter :as rec]
            [recruit-app.specs.ats :as specs]))

(rs/reg-subs "recruiter" [["firstname" ""] ["lastname" ""] ["recruiter-email" ""] ["recruiter-id" nil] ["telephone" ""]
                          ["profile-img-last-update" nil] ["has-photo?" true] ["ats-provider" nil] ["full-access" false]
                          ["pjl-count" 0] ["profile_status" "APPROVED"] ["email-confirmed?" false]
                          ["superuser?" false] ["email" ""] ["email_optin_accepted" true]])

(defn recruiter
  "Returns recruiter from db"
  [db _]
  (:recruiter db))

(defn account-executive
  "Returns account executive"
  [recruiter _]
  (:account_executive recruiter))

(defn full-name
  "Returns full name of recruiter"
  [[firstname lastname] _]
  (str firstname " " lastname))

(defn profile-img-last-update-timestamp
  "Returns update timestamp from long"
  [update _]
  (when update (c/to-long update)))

(defn img-cache-buster
  "Uses last update time if available, otherwise hash of recruiter-id"
  [[last-update recruiter-id] _]
  (or last-update (-> recruiter-id str hash)))

(defn profile-img
  "Returns profile img URI from recruiter-id and last-update time"
  [[recruiter-id last-update] _]
  (u/uri :get-recruiter-image recruiter-id last-update))

(defn has-promotions?
  "Returns whether pjl-count is greater than 0"
  [pjl-count _]
  (< 0 pjl-count))

(defn is-authenticated?
  "Returns whether recruiter id is set"
  [recruiter-id _]
  (rec/valid-recruiter-id? recruiter-id))

(defn phone
  "Returns phone without extension"
  [phone _]
  (when phone
    (-> phone
        (cs/replace #"x.*" "")
        cs/trim)))

(defn guest?
  "User is considered a guest if they are not authenticated"
  [is-authenticated? _]
  (not is-authenticated?))

(defn approved?
  "Returns true if profile status is approved"
  [status _]
  (= "approved" (cs/lower-case status)))

(defn can-save-to-ats?
  "If ats-provider is not nil, then recruiter can export to ATS"
  [provider _]
  (specs/implemented-providers provider))

(defn ats-job-required?
  "Job is required only for workable implementation"
  [provider _]
  (= "workable" provider))

(defn declined?
  "Returns true if status is declined or permanently declined"
  [status]
  (or (= "declined" (cs/lower-case status))
      (= "permanently declined" (cs/lower-case status))))

(rf/reg-sub
  :recruiter/recruiter
  recruiter)

(rf/reg-sub
  :recruiter/account-executive
  :<- [:recruiter/recruiter]
  account-executive)

(rf/reg-sub
  :recruiter/full-name
  :<- [:recruiter/firstname]
  :<- [:recruiter/lastname]
  full-name)

(rf/reg-sub
  :recruiter/profile-img-last-update-timestamp
  :<- [:recruiter/profile-img-last-update]
  profile-img-last-update-timestamp)

(rf/reg-sub
  :recruiter/img-cache-buster
  :<- [:recruiter/profile-img-last-update-timestamp]
  :<- [:recruiter/recruiter-id]
  img-cache-buster)

(rf/reg-sub
  :recruiter/profile-img
  :<- [:recruiter/recruiter-id]
  :<- [:recruiter/img-cache-buster]
  profile-img)

(rf/reg-sub
  :recruiter/has-promotions?
  :<- [:recruiter/pjl-count]
  has-promotions?)

(rf/reg-sub
  :recruiter/is-authenticated?
  :<- [:recruiter/recruiter-id]
  is-authenticated?)

(rf/reg-sub
  :recruiter/phone
  :<- [:recruiter/telephone]
  phone)

(rf/reg-sub
  :recruiter/guest?
  :<- [:recruiter/is-authenticated?]
  guest?)

(rf/reg-sub
  :recruiter/approved?
  :<- [:recruiter/profile_status]
  approved?)

(rf/reg-sub
  :recruiter/can-save-to-ats?
  :<- [:recruiter/ats-provider]
  can-save-to-ats?)

(rf/reg-sub
  :recruiter/ats-job-required?
  :<- [:recruiter/ats-provider]
  ats-job-required?)

(rf/reg-sub
  :recruiter/declined?
  :<- [:recruiter/profile_status]
  declined?)
