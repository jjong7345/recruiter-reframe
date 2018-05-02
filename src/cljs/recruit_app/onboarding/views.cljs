(ns recruit-app.onboarding.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.specs.onboarding :as spec]
            [clojure.walk :as w]
            [cemerick.url :as url]
            [cljs.spec.alpha :as s]
            [recruit-app.components.button :as btn]))

(defn password-hint
  []
  (let [show-errors? (rf/subscribe [:onboarding/show-errors?])
        length-valid? (rf/subscribe [:onboarding/password-length-valid?])
        contains-number? (rf/subscribe [:onboarding/password-contains-number?])
        contains-letter? (rf/subscribe [:onboarding/password-contains-letter?])
        contains-special-character? (rf/subscribe [:onboarding/password-contains-special-character?])]
    (fn []
      [:div {:class "password-hint-container"}
       [:h1 {:class "password-lable"} "Passwords must have:"]
       (if @length-valid?
         [:div {:class "valid-msg"} "At least 6 characters"
          [:div {:class "check-ok"}]]
         [:div {:class (if @show-errors? "error-msg" "valid-msg")} "At least 6 characters"])
       (if @contains-number?
         [:div {:class "valid-msg"} "1 number"
          [:div {:class "check-ok"}]]
         [:div {:class (if @show-errors? "error-msg" "valid-msg")} "1 number"])
       (if @contains-letter?
         [:div {:class "valid-msg"} "1 letter"
          [:div {:class "check-ok"}]]
         [:div {:class (if @show-errors? "error-msg" "valid-msg")} "1 letter"])
       (if @contains-special-character?
         [:div {:class "valid-msg"} "1 special character"
          [:div {:class "check-ok"}]]
         [:div {:class (if @show-errors? "error-msg" "valid-msg")} "1 special character"])])))

(defn password-input
  []
  (let [input-model (rf/subscribe [:onboarding/password])
        show-password-hint? (rf/subscribe [:onboarding/show-password-hint?])
        show-password-errors? (rf/subscribe [:onboarding/show-password-errors?])]
    (fn []
      [rc/v-box
       :class "password-holder holder"
       :children [[rc/label :label "Password"]
                  [rc/input-password
                   :class (str "password" (when @show-password-errors? " error-border"))
                   :model input-model
                   :width "none"
                   :on-change #(rf/dispatch [:onboarding/password-change %])
                   :change-on-blur? false]
                  (when (= @show-password-hint? true) [password-hint])]])))

(defn companytype-input
  []
  (let [companytype (rf/subscribe [:onboarding/companytype])
        show-company-type-errors? (rf/subscribe [:onboarding/show-company-type-errors?])]
    (fn []
      [rc/v-box
       :class "holder"
       :children [[rc/label
                   :label "type of company"]
                  [rc/single-dropdown
                   :class (str "company-type dropdown" (when @show-company-type-errors? " error-border-sdd"))
                   :model @companytype
                   :max-height "250px"
                   :on-change #(rf/dispatch [:onboarding/companytype-change %])
                   :choices (dd/companytype)]
                  [iv/error-message
                   "onboarding"
                   @companytype
                   ::spec/companytype
                   "Please select type of company"]]])))

(defn firstname-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "firstname"
   :label "FIRST NAME"
   :spec ::spec/firstname
   :error-msg "Please enter your first name"])

(defn lastname-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "lastname"
   :label "LAST NAME"
   :spec ::spec/lastname
   :error-msg "Please enter your last name"])

(defn phonenumber-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "phonenumber"
   :label "PHONE NUMBER"
   :spec ::spec/phonenumber
   :error-msg "Please enter a valid phone number"])

(defn companyname-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "companyname"
   :label "COMPANY NAME"
   :spec ::spec/companyname
   :error-msg "Company name must have between [2] and [50] characters"])

(defn zipcode-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "zipcode"
   :label "ZIP CODE"
   :spec ::spec/zipcode
   :error-msg "Please enter a valid ZIP code"])

(defn extension-input
  []
  [iv/specd-input-view
   :ns "onboarding"
   :type "extension"
   :label "EXTENSION"
   :spec ::spec/extension
   :error-msg "Please enter a valid ZIP code"])

(defn signup-form
  []
  (let [preapproved? (rf/subscribe [:onboarding/preapproved?])]
    (fn []
      [rc/v-box
       :class "signup-form"
       :size "none"
       :children [[rc/h-box
                   :class "name-inputs"
                   :justify :between
                   :children [[firstname-input]
                              [lastname-input]]]
                  [rc/h-box
                   :class "phone-inputs"
                   :justify :between
                   :children [[phonenumber-input]
                              [extension-input]]]
                  [:span.extra-input-msg "Your phone number will remain private."]
                  (when-not @preapproved? [companyname-input])
                  (when-not @preapproved?
                    [rc/h-box
                     :class "company-inputs"
                     :children [[companytype-input]
                                [zipcode-input]]])
                  [password-input]]])))

(def header [:h1 {:class "title"} "Almost there!"])
(def subheader [:p.subheader "We need some more information to verify your account."])

(defn submit-btn
  []
  (let [show-throbber? (rf/subscribe [:onboarding/show-throbber?])
        form-valid? (rf/subscribe [:onboarding/form-valid?])
        preapproved? (rf/subscribe [:onboarding/preapproved?])]
    (fn []
      [btn/primary-button
       :submitting? @show-throbber?
       :class "continue"
       :label "Continue"
       :on-click #(rf/dispatch
                    (cond
                      (and @form-valid? @preapproved?) [:onboarding/submit-preapproved]
                      @form-valid? [:onboarding/submit-register]
                      :else [:onboarding/submit-failure]))])))

(defn body
  []
  [rc/h-box
   :class "content-holder"
   :children [[rc/v-box
               :class "signup-form-holder"
               :children [header
                          subheader
                          [signup-form]
                          [submit-btn]]]]])

(defn index
  []
  (let [authenticated? (rf/subscribe [:recruiter/is-authenticated?])
        email (rf/subscribe [:onboarding/email])]
    (fn []
      (if (or @authenticated? (not @email))
        (rf/dispatch [:go-to-route "/"])
        [rc/v-box
         :class "signup main"
         :children [[body]]]))))
