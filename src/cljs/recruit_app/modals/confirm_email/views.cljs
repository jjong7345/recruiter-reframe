(ns recruit-app.modals.confirm-email.views
  (:require [re-frame.core :as rf]
            [recruit-app.util.input-view :as iv]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.form :as form]
            [recruit-app.components.typography :as type]
            [recruit-app.components.layout :as layout]))

(defn errors
  "Renders error message indicating incorrect email address"
  []
  [layout/column
   :padding 0
   :children [[layout/row-top
               :padding 3
               :children [[type/error-copy "Please enter the email the candidate was shared to."]]]
              [layout/row-bottom
               :padding 3
               :children [[type/error-copy "The share link expires after 30 days!"]]]]])

(defn modal
  "Renders modal for user to confirm email when viewing candidate as guest"
  []
  (let [show-errors? (rf/subscribe [:confirm-email/show-errors?])]
    (fn []
      [modal/modal
       :modal-key ::modal/confirm-email
       :title "Confirm your email"
       :body [[layout/column
               :padding 0
               :children [[layout/row-top
                           :padding 6
                           :children [[type/modal-copy "Enter your email address to see the resume, or sign in to your Ladders account."]]]
                          [layout/row-bottom
                           :padding 6
                           :children [[iv/actionable-on-enter-key
                                       [form/input-text
                                        :ns "confirm-email"
                                        :type "email"
                                        :placeholder "Email Address"]
                                       #(rf/dispatch [:confirm-email/submit])]]]
                          (when @show-errors?
                            [layout/row-bottom
                             :padding 12
                             :children [[errors]]])]]]
       :action {:label    "Submit"
                :on-click #(rf/dispatch [:confirm-email/submit])}])))
