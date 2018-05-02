(ns recruit-app.modals.create-account.views
  (:require [re-frame.core :as rf]
            [recruit-app.util.input-view :as iv]
            [recruit-app.onboarding.views :as onboarding]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.modal :as modal]
            [re-com.core :as rc]
            [recruit-app.components.button :as btn]))

(def sub-header
  [:span.modal-sub-header "Post jobs and connect with our 8 million candidates as much as you like, for free."])

(defn already-a-member
  "Renders note to login if you're already a member"
  []
  [rc/h-box
   :class "already-a-member"
   :justify :center
   :children [[:span "Already a Member?"]
              [rc/hyperlink
               :class "login-link"
               :label "Sign in to your account here."
               :on-click #(rf/dispatch [:go-to-login])]]])

(defn modal
  "Renders modal for user to confirm email when viewing candidate as guest"
  []
  [modal/modal
   :modal-key ::modal/create-account
   :class "create-account-modal"
   :title "Fill Your Positions Faster"
   :body [[layout/column
           :padding 0
           :children [sub-header
                      [onboarding/signup-form]
                      [layout/row
                       :padding 0
                       :justify :center
                       :children [[onboarding/submit-btn]]]
                      [already-a-member]]]]])
