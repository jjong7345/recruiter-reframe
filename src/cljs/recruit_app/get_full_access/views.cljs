(ns recruit-app.get-full-access.views
  (:require [re-frame.core :as rf]
            [re-com.core :as re
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [reagent.core :as r]
            [cljs.spec.alpha :as s]
            [recruit-app.get-full-access.specs :as spec]
            [cljs-time.core :as time]
            [cljs-time.format :as date-format]
            [goog.string :as str-util]
            [recruit-app.alerts.views :as alerts]
            [recruit-app.util.uri :as u]
            [recruit-app.util.img :as img]
            [recruit-app.components.typography :as type]
            [recruit-app.components.modal :as modal]
            [recruit-app.modals.full-access.views :as fa-modal]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.button :as btn]
            [recruit-app.components.form :as form]
            [recruit-app.components.layout :as layout]))

(def headline-text "Get Full Access")
(def sub-headline-text "Our new product suite helps you find the right candidates in less time.")
(def info-title-text "Full Access includes:")
(def bullets [
              "Dedicated Account Manager"
              "Unlimited Search and Post"
              "Full Contact Information"
              "Promoted Job Listings"
              "Automatic XML Job Feeds"
              "Referral Hiring"
              "Monthly Analytics Reporting"])
(def success-message "Thank you for your interest in Ladders. An Account Executive will be in touch with you shortly.")
(def error-msg-text "Value is required")
(def terms-of-use-url "/termsofuse")
(def privacy-url "http://www.theladders.com/theladders-privacy")
(def form-rows [
                [:first-name :last-name]
                [:email :phone-number]
                [:company :title]
                [:comments]])
(def form-elements {:first-name   {:key   "first-name"
                                   :label "First Name"
                                   :type  :text}
                    :last-name    {:key   "last-name"
                                   :label "Last Name"
                                   :type  :text}
                    :email        {:key   "email"
                                   :label "Email Address"
                                   :type  :text}
                    :phone-number {:key   "phone-number"
                                   :label "Phone Number"
                                   :type  :text}
                    :company      {:key   "company"
                                   :label "Company"
                                   :type  :text}
                    :title        {:key   "title"
                                   :label "Title"
                                   :type  :text}
                    :comments     {:key   "comments"
                                   :label "Optional Comments"
                                   :type  :textarea}})
(defn logo []
      (fn []
          [:a {:href "/" :class "home-icon"} [:img {:class "logo" :src (img/url :ladders-recruiter-logo)}]]))

(defn header
      []
      (fn []
          [re/h-box
           :class "header"
           :height "50px"
           :children [[re/h-box
                       :class "header-content"
                       :children [ [logo] ]]]]))

(defn headline
  []
  [re/v-box
   :class "headline-holder"
   :children [[:div {:class "headline"} headline-text]]])

(defn sub-headline
  []
  [re/v-box
   :class "sub-headline-holder"
   :children [[:div {:class "sub-headline"} sub-headline-text]]])

(defn on-change-input
  [key val]
  (rf/dispatch [(keyword "get-full-access" (str key "-change")) val]))

(defn render-text
  [key]
  (let [input-model (rf/subscribe [(keyword "get-full-access" (str key))])]
    [re/input-text
     :model @input-model
     :on-change (partial on-change-input key)
     :change-on-blur? false]))

(defn render-textarea
  [key full-width?]
  (let [input-model (rf/subscribe [(keyword "get-full-access" (str key))])]
    [re/input-textarea
     :model @input-model
     :on-change (partial on-change-input key)
     :change-on-blur? false
     :width (if full-width? "100%" "250px")
     :rows 4]))

(defn render-label
  [label]
  [re/label
   :class "form-label"
   :label label])

(defn is-valid?
  [key]
  (let [input-model (rf/subscribe [(keyword "get-full-access" (str key))])
        model-spec (keyword "recruit-app.get-full-access.specs" (str key))]
    (s/valid? model-spec @input-model)))

(defn render-error
  [key]
  [:span {:class "error-msg"} error-msg-text])

(defn render-element
  [element element-count]
  (let [full-width? (= element-count 1)
        show-errors? (rf/subscribe [:get-full-access/show-errors?])]
    [re/v-box
     :class (str "form-element" (when full-width? " full-width"))
     :children [(render-label (:label element))
                (case (:type element)
                  :text (render-text (:key element))
                  :textarea (render-textarea (:key element) full-width?))
                (when (and @show-errors? ((complement is-valid?) (:key element))) (render-error (:key element)))]]))

(defn render-row
  [row]
  (let [element-count (count row)]
    [re/h-box
     :class "form-row"
     :children (into [] (map #(render-element (get form-elements (keyword %)) element-count) row))]))

(defn on-submit
  []
  (let [contact (rf/subscribe [:get-full-access])]
    (if (s/valid? ::spec/contact @contact)
      (rf/dispatch [:get-full-access/submit])
      (rf/dispatch [:get-full-access/show-errors?-change true]))))

(defn submit-btn
  []
  [layout/column
   :class "submit-btn"
   :padding 0
   :children [[btn/primary-transaction-button-large
               :class "submit-btn"
               :label "Contact Sales"
               :on-click #(on-submit)]]])

(defn contact-sales-form
  []
  [layout/column
   :padding 20
   :class "contact-sales-form"
   :children (conj (into [] (map render-row form-rows)) [submit-btn])])

(defn info-title
  []
  [layout/column
   :class "info-title"
   :children [[:div info-title-text]]])

(defn divider
  []
  [:span {:class "info-divider"}])

(defn info-bullet
  [text]
  [:li [:span text]])

(defn bullet-list
  []
  (into [] (conj (map #(info-bullet %) bullets) :ul)))

(defn full-access-info
  []
  [re/v-box
   :class "full-access-info"
   :children [[info-title] [divider] [bullet-list]]])

(defn form-box
  []
  [re/h-box
    :class "form-box"
    :children [[contact-sales-form] [full-access-info]]])

(defn legal-text
  ([text]
    [:div {:class "legal-text"} text])
  ([text href]
    [:a {:class "legal-text" :href href} text]))

(defn legal-divider
  []
  (fn [] [:div {:class "legal-divider"} "|"]))

(defn current-year
  []
  (let [formatter (date-format/formatter "yyyy")]
       (date-format/unparse formatter (time/now))))

(defn legal-holder
  []
  [layout/column
   :class "legal-holder"
   :children [[layout/row
               :justify :between
               :children [(legal-text "Terms of Use" terms-of-use-url)
                          [legal-divider]
                          (legal-text "Privacy" privacy-url)
                          [legal-divider]
                          (legal-text (str (str-util/unescapeEntities "&copy; ") (current-year) " Ladders Recruiter"))]]]])

(defn legal
  []
  [re/h-box
   :class "legal"
   :children [[legal-holder]]])

(defn body
  []
  (fn []
    [layout/column
     :class "body"
     :children [[headline] [sub-headline] [form-box] [legal]]]))

(defn confirmation-modal
  []
  (let [modal [modal/modal
               :modal-key ::modal/full-access-confirmation
               :title "Thank you"
               :body [[type/modal-copy "An Account Executive will be in touch with you shortly."]]
               :action {:label    "Start a new search"
                        :on-click #(rf/dispatch [:get-full-access/on-confirmation-modal-btn-click])}]]
    (rf/dispatch [::modal/open-modal ::modal/full-access-confirmation])
    modal))

(defn confirmation-alert-bar
  []
  (let [show-confirmation-alert-bar? (rf/subscribe [:get-full-access/show-confirmation-alert-bar?])]
    (fn []
      (when @show-confirmation-alert-bar?
        [alerts/alert [:div
                       [:div {:class "alert-message"} "Thank you for your interest in Ladders. An Account Executive will be in touch with you shortly."]
                       [:span {:class    "close-alert-bar"
                               :on-click #(rf/dispatch [:get-full-access/show-confirmation-alert-bar?-change false])} "X"]] "alert-bar"]))))

(defn confirmation-message
  []
  (let [recruiter-id (rf/subscribe [:recruiter/recruiter-id])]
    (fn []
      (if @recruiter-id
        [confirmation-modal]
        [confirmation-alert-bar]))))

(defn index
  []
  (let [confirmation-page? (rf/subscribe [:get-full-access/confirmation-page?])]
   (fn []
       [layout/column
        :class "get-full-access main content-holder"
        :children [[header]
                   (when @confirmation-page? [confirmation-message])
                   [body]
                   [fa-modal/thank]]])))
