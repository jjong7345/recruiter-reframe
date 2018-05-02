(ns recruit-app.modals.email.subs
  (:require [recruit-app.util.subscription :as sub]
            [re-frame.core :as rf]))

(sub/reg-subs "email" [["subject" ""] ["greetings" 0] ["msg-body" ""] ["templates" []] ["show-email-modal?" false] ["active-template" nil]
                       ["name" ""] ["recipients" []] ["show-errors?" false] ["is-sending?" false]])

(rf/reg-sub
  :email
  (fn [db _]
    (:email db)))

(rf/reg-sub
  :email/first-recipient
  :<- [:email/recipients]
  (fn [recipients _]
    (first recipients)))

(rf/reg-sub
  :email/recipient-count
  :<- [:email/recipients]
  (fn [recipients _]
    (count recipients)))

(rf/reg-sub
  :email/can-add-template?
  :<- [:email/templates]
  (fn [templates _]
    (< (count templates) 10)))
