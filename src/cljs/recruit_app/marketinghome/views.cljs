(ns recruit-app.marketinghome.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [recruit-app.header.views :as header]
            [recruit-app.footer.views :as footer]
            [recruit-app.util.input-view :as iv]
            [recruit-app.marketinghome.db :as spec]
            [domina.events :as events]
            [clojure.walk :as w]
            [goog.string :as gstring]
            [clojure.string :refer [capitalize]]
            [domina :as dom]
            [goog.history.EventType :as EventType]
            [domina.css :as css]
            [recruit-app.util.uri :as u]
            [recruit-app.util.img :as img]))

(defn render-email-hint
  []
  (let [show-error-message? (rf/subscribe [:marketinghome/show-error-message?])
        show-warning-message? (rf/subscribe [:marketinghome/show-warning-message?])
        show-success-message? (rf/subscribe [:marketinghome/show-success-message?])]
    (fn []
      [rc/box
       :class "email-hint-container"
       :child (cond
                @show-success-message? [rc/h-box
                                        :class "message valid-msg"
                                        :children [[rc/box
                                                    :class "icons"
                                                    :child [:img {:src (img/url :checkmark-solo)}]]
                                                   "Thank you! You'll be verified faster this way."]]
                @show-warning-message? [rc/h-box
                                        :class "message valid-msg-corp"
                                        :children [[rc/box
                                                    :class "icons"
                                                    :child [:img {:src (img/url :exclamation-grey)}]]
                                                   "A corporate email address speeds up approval."]]
                @show-error-message? [rc/h-box
                                      :class "message error-msg"
                                      :children [[rc/box
                                                  :class "icons"
                                                  :child [:img {:src (img/url :exclamation-mark-solo)}]]
                                                 "Your email address is formatted incorrectly."]]
                :else "")])))

(defn username-input
  []
  (let [input-model (rf/subscribe [:marketinghome/email])]
    (fn []
      [rc/v-box
       :class "email-holder holder"
       :style {:flex "1 1 auto"}
       :children [[rc/input-text
                   :class "email"
                   :style {:flex "1 1 auto"}
                   :model input-model
                   :placeholder "Enter your work email address"
                   :width "none"
                   :on-change #(rf/dispatch [:marketinghome/email-change %])
                   :change-on-blur? false]]])))


(defn email-form
  []
  (let [email (rf/subscribe [:marketinghome/email])
        valid? (rf/subscribe [:marketinghome/valid-email-format?])
        submitting? (rf/subscribe [:marketinghome/submitting?])]
    (fn []
      [rc/h-box
       :class "email-form"
       :children [[username-input]
                  [iv/submit-btn
                   :label "Get Started"
                   :class "get-started"
                   :submitting? @submitting?
                   :on-click (if @valid?
                               #(rf/dispatch [:marketinghome/onboarding @email])
                               #())]]])))

(defn divider []
  [rc/box :class "divider" :child ""])

(defn mh-header-img-section []
  (fn []
    [rc/v-box
     :class "mh-header-img"
     :children [[rc/v-box
                 :children [[:h1 "The fastest route to" [:br] " your next $100K+ hire"]
                            [email-form]
                            [render-email-hint]]]]]))

(defn mh-header []
  (fn []
    [rc/v-box
     :class "content"
     :children [[mh-header-img-section]]]))


;; ;; Get Started Title & Description Map
(def get-started-blob
  [{:title       "10MM+"
    :description [:p "Serious Professionals"]}
   {:title       "90%"
    :description [:p "Bachelor’s and Higher"]}
   {:title "$149K"
    :description [:p "Average Income"]}
   {:title "15+"
    :description [:p "Average Years of Experience"]}])

(defn get-started
  [get-started-blob]
  "Get Started"
  (fn []
    [rc/v-box
     :class "container"
     :children [[:h2 "Finding top talent is tough. Why waste time?" [:br]"Hire with Ladders."]
                [divider]
                [rc/h-box
                 :class "feature-row-1"
                 :justify :around
                 :style {:flex-flow "row nowrap"}
                 :children [(for [x (range 0 4)]
                              ^{:key x} [rc/v-box
                               :class "info-item"
                               :children [[:p {:class "title"} (get-in get-started-blob [x :title])]
                                          [:div {:class "caption"} (get-in get-started-blob [x :description])]]])]]]]))

(def infographic-blob
  [{:img "mh-fastest-growing-image"
    :description [:p "The fastest growing in traffic over the last 12 months"]}
   {:img       "mh-demographic-image"
    :description [:p "#1 amongst 25 - 54 year olds"]}
   {:img       "mh-engagement-image"
    :description [:p "#1 in Facebook engagement"]}])

(defn infographic
  [infographic-blob]
  (fn []
    [rc/v-box
     :class "feature-row-1 infographic-row"
     :children [[rc/v-box
                 :class "container"
                 :children [[:h2 "Amongst all leading career sites Ladders is:"]
                            [rc/v-box
                             :class "info-container"
                             :justify :around
                             :style {:flex-flow "row nowrap"}
                             :children [(for [x (range 0 3)]
                                          (let [img (get-in infographic-blob [x :img])
                                                desc (get-in infographic-blob [x :description])]
                                            ^{:key x} [rc/v-box
                                             :class "info-item"
                                             :children [[:p {:class (str "img " (str img))}]
                                                        [:div {:class "img-caption"} desc]]]))]]]]]]))

(defn see-plans []
  (fn []
    [rc/v-box
     :class "container"
     :children [[:h2 "Find, attract, activate, and analyze the $100K+ " [:span {:class "break"} "market with Ladders Recruiter"]]
                [rc/button
                 :class "routing btn"
                 :label "See Subscription Plans"
                 :on-click #(rf/dispatch [:go-to-pricing])]
                [divider]]]))

(def features
  "Define Feature Boxes Title/Description/Images"
  [{:title       "Jobs"
    :description (str "We cut the noise" (gstring/unescapeEntities "&mdash;") "only qualified candidates can view and apply to your jobs. Our large candidate network and sophisticated algorithms ensure the right candidates see your posts.")
    :image       (img/url :promoted-job-listings)
    :style       "30px"
    :alt_tag     "Image of a recruiter's promoted job"
    :width       "411px"
    :height      "251px"}
   {:divider true}
   {:title       "Search"
    :description (str "Use advanced filters to search our 10MM+ candidate network by experience, education, desired salary, and more to find the best contenders. Save searches and use data to maximize your recruiting efforts. Parse resumes, get full contact information and easily connect with members directly from the site.")
    :image       (img/url :recruitersearch)
    :style       "45px"
    :alt_tag     "Image of contact information available to recruiters"
    :width       "360px"
    :height      "auto"}
   {:divider true}
   {:title       "Employer branding"
    :description (str "Get in front of our career-minded audience and promote your employer brand. Share your company culture with members in a geo-targeted recruiting email, on your company page and on Ladders News.")
    :image       (img/url :companybranding)
    :style       "60px"
    :alt_tag     "Example of a recruiter’s account manager"
    :width       "425px"
    :height      "auto"}
   {:divider true}
   {:title       "Insights"
    :description (str "Make smarter decisions based on data, patterns, trends and competitive intel. Get reporting data and insights from your dedicated account manager.")
    :image       (img/url :monthly-analytics-reporting)
    :style       "45px"
    :alt_tag     "Illustration of a recruiter’s job being sydnicated"
    :width       "260px"
    :height      "242px"}
  ])

(defn feature-img [feature]
  [:img.half {:style {:width "100%" :max-width (:width feature) :height (:height feature)} :src (:image feature) :alt (:alt_tag feature)}])

(defn feature-info
  [feature]
  [rc/v-box
   :class "feature-info"
   :children [[:h4 (:title feature)]
              [:p {:style {:width "100%"}} (:description feature)]]])

(defn feature-row [feature img-first]
  (if (:divider feature)
    [rc/box :class "divider" :child ""]
    [rc/h-box
     :class "feature-row"
     :justify :between
     :style {:flex-flow "row wrap"}
     :children (if img-first
                 [[feature-img feature]
                  [feature-info feature]]
                 [[feature-info feature]
                  [feature-img feature]])]))



(defn featured-row
  [features]
  "The Featured Row Function That Will Take Value from 'features' & Render"
  [rc/v-box
   :children [
              [rc/v-box
               :class "featured-row"
               :children (into (mapv (fn [feature img-first] [feature-row feature img-first]) features (cycle [false false true true])))]
              [divider]
              [rc/button
               :class "routing btn"
               :style {:margin-top "40px"}
               :label "See Subscription Plans"
               :on-click #(rf/dispatch [:go-to-pricing])]]])

(defn client-list
  []
  "Clients List"
  [rc/v-box
   :class "client-list"
   :children [[:h2 "Ladders is trusted by"]
              [rc/box
               :class "ladders-clients"
               :child [:img {:src (img/url :trusted-by-logos)
                             :alt "Client logos of Ladders Recruiter"}]]]])



;; Calling The index w/Sub Partials

(defn index
  []
  (rf/dispatch [:set-page-title "LADDERS Recruitment Website - Job Posting & Recruiter Services"])
  (fn []
    [rc/v-box
     :class "marketing-home main"
     :children [[mh-header]
                [get-started get-started-blob]
                [infographic infographic-blob]
                [see-plans]
                [featured-row features]
                [client-list]
                [:div {:class "bottom-form"} [email-form]]]]))
