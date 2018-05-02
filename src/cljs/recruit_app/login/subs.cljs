(ns recruit-app.login.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]
            [cljs.spec.alpha :as s]
            [recruit-app.login.db :as db]
            [recruit-app.specs.onboarding :as specs]))

(def reg-subs (partial sub/reg-subs "login"))

(reg-subs [["username" ""] ["password" ""] ["error-msg" ""]
           ["show-onboarding-message?" false] ["show-create-account?" false]
           ["show-forgot-password?" false] ["confirm-password" ""]
           ["show-change-password?" false] ["sending-forgot-password?" false]
           ["show-change-password-errors?" false] ["logging-in?" false]
           ["creating-account?" false] ["show-forgot-password-errors?" false]])

(defn show-errors?
  "Show email verification error only if user has entered an email"
  [email _]
  (seq email))

(defn email-valid?
  "Form is valid if email address is valid"
  [email _]
  (or (not (seq email)) (s/valid? ::db/email email)))

(defn show-login-form?
  "Show login form when all other forms not being shown"
  [[show-forgot-password? show-create-account? show-change-password?] _]
  (not (or show-forgot-password? show-create-account? show-change-password?)))

(defn show-privacy-policy-agreement?
  "Only needs to be shown for login or create-account"
  [[show-forgot-password? show-change-password?] _]
  (not (or show-forgot-password? show-change-password?)))

(defn show-secure-site-img?
  "Only needs to be shown for login or create-account"
  [[show-forgot-password? show-change-password?] _]
  (not (or show-forgot-password? show-change-password?)))

(defn show-not-a-member-info?
  "Show only for login form and forgot password"
  [[show-login-form? show-forgot-password?] _]
  (or show-login-form? show-forgot-password?))

(defn show-already-a-member-info?
  "Show only for create account form"
  [show-create-account? _]
  show-create-account?)

(defn show-privacy-and-security-info?
  "Show for all forms except change password"
  [show-change-password? _]
  (not show-change-password?))

(defn show-password-info?
  "Show password info when using change password form"
  [show-change-password? _]
  show-change-password?)

(defn password-valid?
  "Valid if passes spec"
  [password _]
  (s/valid? ::specs/password password))

(defn passwords-match?
  "Checks if password and confirm-password match"
  [[password confirm-password] _]
  (= password confirm-password))

(defn change-password-valid?
  "Valid if password equals confirm-password"
  [[password-valid? passwords-match?] _]
  (and password-valid? passwords-match?))

(rf/reg-sub
  :login/show-errors?
  :<- [:login/username]
  show-errors?)

(rf/reg-sub
  :login/email-valid?
  :<- [:login/username]
  email-valid?)

(rf/reg-sub
  :login/show-login-form?
  :<- [:login/show-forgot-password?]
  :<- [:login/show-create-account?]
  :<- [:login/show-change-password?]
  show-login-form?)

(rf/reg-sub
  :login/show-privacy-policy-agreement?
  :<- [:login/show-forgot-password?]
  :<- [:login/show-change-password?]
  show-privacy-policy-agreement?)

(rf/reg-sub
  :login/show-secure-site-img?
  :<- [:login/show-forgot-password?]
  :<- [:login/show-change-password?]
  show-secure-site-img?)

(rf/reg-sub
  :login/show-not-a-member-info?
  :<- [:login/show-login-form?]
  :<- [:login/show-forgot-password?]
  show-not-a-member-info?)

(rf/reg-sub
  :login/show-already-a-member-info?
  :<- [:login/show-create-account?]
  show-already-a-member-info?)

(rf/reg-sub
  :login/show-privacy-and-security-info?
  :<- [:login/show-change-password?]
  show-privacy-and-security-info?)

(rf/reg-sub
  :login/show-password-info?
  :<- [:login/show-change-password?]
  show-password-info?)

(rf/reg-sub
  :login/password-valid?
  :<- [:login/password]
  password-valid?)

(rf/reg-sub
  :login/passwords-match?
  :<- [:login/password]
  :<- [:login/confirm-password]
  passwords-match?)

(rf/reg-sub
  :login/change-password-valid?
  :<- [:login/password-valid?]
  :<- [:login/passwords-match?]
  change-password-valid?)
