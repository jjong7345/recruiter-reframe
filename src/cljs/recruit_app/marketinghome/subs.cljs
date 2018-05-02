(ns recruit-app.marketinghome.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]
            [recruit-app.marketinghome.db :as spec]))

(sub/reg-subs "marketinghome" [["email" ""] ["error-msg" ""]
                               ["toggle-errors" true] ["show-errors?" false]
                               ["show-email-status?" false]
                               ["submitting?" false]])

(defn generic-email?
  "Returns whether submitted email is corporate according to spec"
  [email _]
  (spec/generic-email? email))

(defn valid-email-format?
  "Returns whether email is valid according to spec"
  [email _]
  (spec/valid-email-format? email))

(defn show-error-message?
  "Shows error message if email provided, not generic and not valid"
  [[email generic-email? valid-email-format?] _]
  (and (> (count email) 1) (not generic-email?) (not valid-email-format?)))

(defn show-success-message?
  "Shows success message if email is provided that is not generic and valid"
  [[email generic-email? valid-email-format?] _]
  (and (> (count email) 1) (not generic-email?) valid-email-format?))

(defn show-warning-message?
  "Shows warning message if email is provided that is valid but generic"
  [[email generic-email? valid-email-format?] _]
  (and (> (count email) 1) generic-email? valid-email-format?))

(rf/reg-sub
  :marketinghome/generic-email?
  :<- [:marketinghome/email]
  generic-email?)

(rf/reg-sub
  :marketinghome/valid-email-format?
  :<- [:marketinghome/email]
  valid-email-format?)

(rf/reg-sub
  :marketinghome/show-error-message?
  :<- [:marketinghome/email]
  :<- [:marketinghome/generic-email?]
  :<- [:marketinghome/valid-email-format?]
  show-error-message?)

(rf/reg-sub
  :marketinghome/show-success-message?
  :<- [:marketinghome/email]
  :<- [:marketinghome/generic-email?]
  :<- [:marketinghome/valid-email-format?]
  show-success-message?)

(rf/reg-sub
  :marketinghome/show-warning-message?
  :<- [:marketinghome/email]
  :<- [:marketinghome/generic-email?]
  :<- [:marketinghome/valid-email-format?]
  show-warning-message?)


