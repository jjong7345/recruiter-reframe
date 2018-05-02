(ns recruit-app.login.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.util.input-view :as iv]
            [domina.events :as events]
            [recruit-app.login.db :as db]
            [recruit-app.modals.forgot-password-success.views :as fp]
            [recruit-app.util.img :as img]))

(defn username-input
  [placeholder]
  [iv/specd-input-view
   :ns "login"
   :type "username"
   :input-type rc/input-text
   :placeholder placeholder
   :spec ::db/email
   :error-msg "Please enter a valid email address"])

(def onboarding-message
  "Renders message to user if coming from onboarding flow"
  [:span.error-msg "Looks like we already have an account for that email,
                 please enter your password to sign in."])

(defn password
  "Renders a password input"
  [& {:keys [model on-change placeholder]}]
  [rc/v-box
   :class "password-holder holder"
   :children [[rc/input-password
               :class "password"
               :model model
               :width "none"
               :placeholder placeholder
               :on-change on-change
               :change-on-blur? false]]])

(defn password-input
  []
  (let [model (rf/subscribe [:login/password])]
    (fn []
      [password
       :model model
       :placeholder "Password"
       :on-change #(rf/dispatch [:login/password-change %])])))

(defn confirm-password-input
  []
  (let [model (rf/subscribe [:login/confirm-password])]
    (fn []
      [password
       :model model
       :placeholder "Confirm Password"
       :on-change #(rf/dispatch [:login/confirm-password-change %])])))

(defn error
  [msg]
  [rc/label
   :class "error-msg"
   :label msg])

(defn error-msg
  []
  (let [msg (rf/subscribe [:login/error-msg])]
    (fn []
      (when (seq @msg) [error @msg]))))

(defn password-error-msg
  "Displays message that passwords don't match when true"
  []
  (let [show-change-password-errors? (rf/subscribe [:login/show-change-password-errors?])
        passwords-match? (rf/subscribe [:login/passwords-match?])
        password-valid? (rf/subscribe [:login/password-valid?])]
    (fn []
      (when @show-change-password-errors?
        [error (cond
                 (not @passwords-match?) "Passwords do not match."
                 (not @password-valid?) "Password must be at least 6 characters, contain one number, one letter and one special character."
                 :else "")]))))

(defn privacy-policy-agreement
  "Displays disclaimer of agreement to terms of use and privacy policy"
  []
  [:div.privacy-policy-agreement
   [:span "By signing in, you agree to Ladders's "]
   [rc/hyperlink-href
    :label "Terms of Use"
    :href "/#/terms-of-use"]
   [:span " and "]
   [rc/hyperlink-href
    :label "Privacy Policy"
    :href "http://www.theladders.com/theladders-privacy"]])

(def secure-site-img
  [:img.secure-site-img {:src (img/url :secure-site-img-url)}])

(def forgot-password-link
  [rc/box
   :align :end
   :child [rc/hyperlink
           :class "forgot-password-link"
           :label "Forgot Password"
           :on-click #(rf/dispatch [:login/forgot-password-click])]])

(defn login-form
  []
  (let [show-onboarding-message? (rf/subscribe [:login/show-onboarding-message?])
        logging-in? (rf/subscribe [:login/logging-in?])]
    (fn []
      [rc/v-box
       :class "login-form"
       :children [[rc/label :class "title" :label "Please sign in"]
                  [iv/input
                   :ns "login"
                   :type "username"
                   :placeholder "Your email address"]
                  [password-input]
                  (when @show-onboarding-message? onboarding-message)
                  [error-msg]
                  forgot-password-link
                  [iv/submit-btn
                   :class "login"
                   :label "Sign In"
                   :submitting? @logging-in?
                   :on-click (iv/wrap-prevent-default
                               #(rf/dispatch [:login/do-login]))]]])))

(defn create-account-form
  []
  (let [email-valid? (rf/subscribe [:login/email-valid?])
        email (rf/subscribe [:login/username])
        creating-account? (rf/subscribe [:login/creating-account?])]
    (fn []
      [rc/v-box
       :class "login-form"
       :children [[rc/label :class "title" :label "Sign Up"]
                  [username-input "Enter your work email"]
                  [iv/submit-btn
                   :class "login btn"
                   :label "Sign Up"
                   :disabled? (not @email-valid?)
                   :submitting? @creating-account?
                   :on-click (iv/wrap-prevent-default
                               #(rf/dispatch [:login/create-account @email]))]]])))

(defn forgot-password-errors
  []
  (let [show-errors? (rf/subscribe [:login/show-forgot-password-errors?])]
    (fn []
      (when @show-errors?
        [:div.error-msg
         [:span "This email address is not in our records. Please enter your correct email address or "]
         [rc/hyperlink
          :label "sign up"
          :on-click #(rf/dispatch [:login/create-account-click])]
         [:span " for an account."]]))))

(defn forgot-password-form
  []
  (let [email-valid? (rf/subscribe [:login/email-valid?])
        sending-forgot-password? (rf/subscribe [:login/sending-forgot-password?])]
    (fn []
      [rc/v-box
       :class "login-form"
       :children [[rc/label :class "title" :label "Forgot Password"]
                  [:span.info "Enter your email address and we'll send you a secure link to create a new password"]
                  [username-input "Your email address"]
                  [forgot-password-errors]
                  [iv/submit-btn
                   :class "login btn"
                   :label "Send"
                   :disabled? (not @email-valid?)
                   :submitting? @sending-forgot-password?
                   :on-click (iv/wrap-prevent-default
                               #(rf/dispatch [:login/send-forgot-password-email]))]]])))

(defn change-password-form
  []
  (let [change-password-valid? (rf/subscribe [:login/change-password-valid?])]
    (fn []
      [rc/v-box
       :class "login-form change-password"
       :children [[rc/label :class "title" :label "Create new password"]
                  [:span.info "Your password must contain at least 1 letter, 1 number, and 1 punctuation mark."]
                  [password-input]
                  [confirm-password-input]
                  [password-error-msg]
                  [rc/button
                   :class "login btn"
                   :label "Create New Password"
                   :disabled? false
                   :on-click (if @change-password-valid?
                               (iv/wrap-prevent-default #(rf/dispatch [:login/change-password]))
                               (iv/wrap-prevent-default #(rf/dispatch [:login/show-change-password-errors?-change true])))]]])))

(defn form
  "Displays correct form based on view"
  []
  (let [show-create-account? (rf/subscribe [:login/show-create-account?])
        show-forgot-password? (rf/subscribe [:login/show-forgot-password?])
        show-change-password? (rf/subscribe [:login/show-change-password?])]
    (fn []
      (cond
        @show-create-account? [create-account-form]
        @show-forgot-password? [forgot-password-form]
        @show-change-password? [change-password-form]
        :else [login-form]))))

(defn form-col
  []
  (let [show-privacy-policy-agreement? (rf/subscribe [:login/show-privacy-policy-agreement?])
        show-secure-site-img? (rf/subscribe [:login/show-secure-site-img?])]
    (fn []
      [rc/v-box
       :class "form-col"
       :children [[:form [form]]
                  [rc/v-box
                   :class "login-disclaimers"
                   :children [(when @show-privacy-policy-agreement? [privacy-policy-agreement])
                              (when @show-secure-site-img? secure-site-img)]]]])))

(defn info-title
  "Renders title text within login info"
  [text]
  [:p {:class "title"} text])

(defn info-text
  "Renders info text within login info"
  [text]
  [:p {:class "info"} text])

(def already-a-member-info
  [:div.info-group
   [info-title "Already a Member?"]
   [rc/hyperlink
    :label "Sign in to your account"
    :on-click #(rf/dispatch [:login/show-login-form])]
   [:span " to start finding the right candidates."]])

(def not-a-member-info
  [:div.info-group
   [info-title " Not a member yet?"]
   [:div.info
    [rc/hyperlink
     :label "Create an account "
     :on-click #(rf/dispatch [:login/create-account-click])]
    [:span " to start finding candidates — it’s fast and easy."]]])

(def privacy-and-security-info
  [:div
   [info-title "Privacy and Security"]
   [info-text "We do not sell or share your personal information, ever."]])

(defn password-info-list-item
  "Displays list item within login info"
  [& {:keys [text example]}]
  [rc/v-box
   :class "password-info-list-item"
   :children [[:div.list-item text]
              (when example [:div.list-item-example example])]])

(def protect-your-privacy
  [:div.protect-your-privacy
   [info-text "Protect your privacy:"]
   [password-info-list-item
    :text "Make it personal instead of simple."
    :example "(e.g., “!NYGiants4Life”)"]
   [password-info-list-item
    :text "Use an exciting or emotional phrase."
    :example "(e.g., “LostMyWallet22!”)"]])

(def avoid-using
  [:div
   [info-text "Avoid using:"]
   [password-info-list-item
    :text "Single words"]
   [password-info-list-item
    :text "First or last names"
    :example "(e.g., “mike1” or “thejones”)"]
   [password-info-list-item
    :text "Commonly used words or phrases"
    :example "(e.g., “abc123” or “test111”)"]
   [password-info-list-item
    :text "Commonly used words with a single character"
    :example "(e.g., “apple1” or “ladders!”)"]
   [info-text [:div
               [:span "Your password provides full access to your account, including your billing details. "]
               [:span.bold "Please do not share it with others."]]]])

(def password-info
  [:div.password-info
   [info-title "Make your password a strong one."]
   protect-your-privacy
   avoid-using])

(defn login-info
  []
  (let [show-already-a-member-info? (rf/subscribe [:login/show-already-a-member-info?])
        show-not-a-member-info? (rf/subscribe [:login/show-not-a-member-info?])
        show-privacy-and-security-info? (rf/subscribe [:login/show-privacy-and-security-info?])
        show-password-info? (rf/subscribe [:login/show-password-info?])]
    (fn []
      [:div {:class "login-info"}
       (when @show-not-a-member-info? not-a-member-info)
       (when @show-already-a-member-info? already-a-member-info)
       (when @show-privacy-and-security-info? privacy-and-security-info)
       (when @show-password-info? password-info)])))

(defn body
  []
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [[rc/h-box
                 :class "content"
                 :children [[form-col]
                            [login-info]]]]]))

(defn index
  []
  (let [is-authenticated? (rf/subscribe [:recruiter/is-authenticated?])]
    (fn []
      (if @is-authenticated?
        (rf/dispatch [:go-to-home])
        [rc/v-box
         :class "login main"
         :children [[body] [fp/modal]]]))))