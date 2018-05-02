(ns recruit-app.onboarding.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]
            [cljs.spec.alpha :as s]
            [recruit-app.specs.onboarding :as spec]))

(sub/reg-subs "onboarding" [["firstname" ""] ["lastname" ""] ["phonenumber" ""]
                            ["extension" ""] ["companyname" ""] ["company-id" nil]
                            ["companytype" 0] ["zipcode" ""] ["password" ""]
                            ["error-msg" ""] ["show-throbber?" false]
                            ["show-errors?" false] ["show-password-hint?" false]
                            ["error-response" nil] ["email" nil]])

(defn password-contains-number?
  "Returns whether password contains number"
  [password _]
  (spec/contains-number? password))

(defn password-length-valid?
  "Returns whether password contains number"
  [password _]
  (spec/length-valid? password))

(defn password-valid?
  "Password is valid if it contains a number and is a certain length"
  [[contains-number? valid-length?] _]
  (and contains-number? valid-length?))

(defn show-password-errors?
  "Password error should be shown if showing password hint and password invalid"
  [[valid? show-password-hint?] _]
  (and (not valid?) show-password-hint?))

(defn company-type-valid?
  "Returns whether company type is valid"
  [company-type _]
  (s/valid? ::spec/companytype company-type))

(defn show-company-type-errors?
  "Show errors when company type not valid and showing errors"
  [[valid? show-errors?]]
  (and (not valid?) show-errors?))

(defn preapproved?
  "Returns whether company-id is set"
  [company-id _]
  (some? company-id))

(defn form
  "Returns entire onboarding map"
  [{:keys [onboarding]} _]
  onboarding)

(defn form-valid?
  "Returns whether entire form is valid"
  [[form preapproved?] _]
  (s/valid? (if preapproved? ::spec/preapproved ::spec/non-preapproved) form))

(defn password-contains-letter?
  "Returns whether password contains a letter"
  [password _]
  (spec/contains-letter? password))

(defn password-contains-special-character?
  "Returns whether password contains a special character"
  [password _]
  (spec/contains-special-character? password))

(rf/reg-sub
  :onboarding/password-contains-number?
  :<- [:onboarding/password]
  password-contains-number?)

(rf/reg-sub
  :onboarding/password-length-valid?
  :<- [:onboarding/password]
  password-length-valid?)

(rf/reg-sub
  :onboarding/password-contains-letter?
  :<- [:onboarding/password]
  password-contains-letter?)

(rf/reg-sub
  :onboarding/password-contains-special-character?
  :<- [:onboarding/password]
  password-contains-special-character?)

(rf/reg-sub
  :onboarding/password-valid?
  :<- [:onboarding/password-contains-number?]
  :<- [:onboarding/password-length-valid?]
  password-valid?)

(rf/reg-sub
  :onboarding/show-password-errors?
  :<- [:onboarding/password-valid?]
  :<- [:onboarding/show-password-hint?]
  show-password-errors?)

(rf/reg-sub
  :onboarding/company-type-valid?
  :<- [:onboarding/companytype]
  company-type-valid?)

(rf/reg-sub
  :onboarding/show-company-type-errors?
  :<- [:onboarding/company-type-valid?]
  :<- [:onboarding/show-errors?]
  show-company-type-errors?)

(rf/reg-sub
  :onboarding/preapproved?
  :<- [:onboarding/company-id]
  preapproved?)

(rf/reg-sub
  :onboarding/form
  form)

(rf/reg-sub
  :onboarding/form-valid?
  :<- [:onboarding/form]
  :<- [:onboarding/preapproved?]
  form-valid?)

