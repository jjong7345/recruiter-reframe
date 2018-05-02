(ns recruit-app.onboarding-email-verification.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.util.input-view :as iv]
            [recruit-app.modals.resend-verification-success.views :as rvs]))

(defn code-input-view
  []
  (fn []
    [rc/h-box
     :class "code-input-view"
     :justify :between
     :children [[iv/input
                 :label "ENTER VALIDATION CODE"
                 :ns "email-verification"
                 :type "code"]
                [rc/button
                 :label "Verify"
                 :style {:align-self "flex-end"}
                 :on-click #(rf/dispatch [:email-verification/verify])]]]))

(defn body
  []
  (let [email (rf/subscribe [:recruiter/email])
        show-error? (rf/subscribe [:email-verification/show-error?])]
    (fn []
      [rc/v-box
       :class "content"
       :children [[:h1 "Check your Email"]
                  [:p.desc
                   [:span "We've sent an email to "]
                   [:span.teal @email]
                   [:span " — please click the link to verify your account, or enter a verification code below:"]]
                  [code-input-view]
                  (when @show-error? [:p.error "Oops! Please check the verification code and try again."])
                  [:p.re-send
                   [:span "Didn't receive the email? "]
                   [:span.teal.link {:on-click #(rf/dispatch [:email-verification/re-send-email])} "Request a new verification email."]]]])))

(defn index
  []
  (rf/dispatch [:set-page-head-title "Check your email"])
  (fn []
    [rc/v-box
     :class "email-verification main"
     :children [[body] [rvs/modal]]]))
