(ns recruit-app.modals.resend-verification-success.views
  (:require [recruit-app.components.modal :as modal]
            [re-frame.core :as rf]
            [recruit-app.components.typography :as type]
            [recruit-app.components.layout :as layout]))

(defn modal
  "Renders modal to show upon successful sending of forgot password email"
  []
  (let [email (rf/subscribe [:recruiter/email])]
    (fn []
      [modal/modal
       :modal-key ::modal/resend-verification-success
       :title "Email Sent"
       :body [[layout/row-top
               :padding 6
               :children [[type/modal-copy (str "We've sent a new verification code to " @email ".")]]]
              [layout/row-bottom
               :padding 6
               :children [[type/modal-copy "Please check your email. If you don't see it, please check your spam or junk folders."]]]]])))
