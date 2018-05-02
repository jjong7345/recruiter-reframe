(ns recruit-app.account.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as rs]
            [cljs.spec.alpha :as s]
            [recruit-app.specs.account :as spec]))

(rs/reg-subs "account" [["fname" ""] ["lname" ""]
                        ["job-title" ""] ["function" 0]
                        ["company" ""] ["role" 0]
                        ["company-site" ""]
                        ["linkedin" ""] ["facebook" ""]
                        ["twitter" ""] ["blog" ""]
                        ["bio" ""]
                        ["email" ""]
                        ["phone" ""] ["ext" ""]
                        ["street" ""] ["city" ""] ["state" ""]
                        ["zip" ""] ["country" ""]
                        ["active-tab" :tab0]
                        ["is-uploading?" false]
                        ["profile-img-url" nil]
                        ["current-password" ""]
                        ["new-password" ""]
                        ["confirm-password" ""]
                        ["api-key" ""] ["job-board-token" ""]
                        ["secondary-api-key" ""] ["ats-name" ""]
                        ["ats" ""] ["newsletter" false]
                        ["special-offers" false] ["connection-req" false]
                        ["feedback" false] ["suggested-cand" false] ["search-based-cand" false]
                        ["show-sample-bio?" false] ["show-errors?" false] ["ats-settings" nil]
                        ["lever-users" nil] ["lever-user-selected" nil]])

(defn form
  "Returns entire onboarding map"
  [{:keys [account]} _]
  account)

(defn form-valid?
  "Returns whether entire form is valid"
  [form _]
  (s/valid? ::spec/account form))

(defn show-bio-error?
  "Returns whether bio is valid"
  [[show-errors? bio] _]
  (and show-errors? (not (s/valid? ::spec/bio bio))))

(defn bio-contains-not-enough-characters?
  "Returns whether bio contains 100 characters or more"
  [[show-errors? bio] _]
  (and show-errors? (not (spec/contains-enough-characters? bio))))

(defn bio-contains-contact?
  "Returns whether bio contains contact information (email or phone)"
  [[show-errors? bio] _]
  (and show-errors? (or (spec/contain-email? bio) (spec/contain-phone? bio))))

(defn password-match?
  "Returns whether new password and confirm password is same"
  [form _]
  (let [new-password (:new-password form)
        confirm-password (:confirm-password form)]
    (= new-password confirm-password)))

(defn password-valid?
  "Returns whether password form is valid"
  [[form match?] _]
  (let [current-password (:current-password form)
        new-password (:new-password form)
        confirm-password (:confirm-password form)]
    (and (s/valid? ::spec/change-password {:current-password current-password
                                           :new-password new-password
                                           :confirm-password confirm-password})
         match?)))

(rf/reg-sub
  :account/profile-img
  :<- [:account/profile-img-url]
  :<- [:recruiter/profile-img]
  (fn [[url profile-url] _]
    (or url profile-url)))

(rf/reg-sub
  :account/form
  form)

(rf/reg-sub
  :account/form-valid?
  :<- [:account/form]
  form-valid?)

(rf/reg-sub
  :account/show-bio-error?
  :<- [:account/show-errors?]
  :<- [:account/bio]
  show-bio-error?)

(rf/reg-sub
  :account/bio-contains-not-enough-characters?
  :<- [:account/show-errors?]
  :<- [:account/bio]
  bio-contains-not-enough-characters?)

(rf/reg-sub
  :account/bio-contains-contact?
  :<- [:account/show-errors?]
  :<- [:account/bio]
  bio-contains-contact?)

(rf/reg-sub
  :account/password-match?
  :<- [:account/form]
  password-match?)

(rf/reg-sub
  :account/password-valid?
  :<- [:account/form]
  :<- [:account/password-match?]
  password-valid?)

