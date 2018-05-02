(ns recruit-app.referral-hiring.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [domina.events :as events]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.events :as re]
            [recruit-app.modals.referral-hiring.views :as modals]
            [recruit-app.referral-hiring.db :as db]
            [cljs.spec.alpha :as s]
            [recruit-app.util.img :as img]))

(defn title
  []
  (fn []
    [rc/h-box
     :class "header-h1"
     :justify :center
     :children [[:h1 {:class "title" :id "top"} "Free referrals for you"]]]))

(defn learn-more-info
  []
  (fn []
    [rc/v-box
     :class "more-text"
     :children [[:p "Here's how it works:"]
                [:p "We'll ask Ladders members to recommend great people to your company's open positions. You'll get an email with your employee's referral. If you hire that person, you use your company's standard referral bonus. We ask nothing in return."]
                [:p "More pre-qualified candidates for your open positions with no extra costs to you."]]]))

(defn deck
  []
  (let [read-more? (rf/subscribe [:referral-hiring/read-more?])]
    (fn []
      [rc/v-box
       :children [
                  [:p {:class "deck"} "The best hires are often your employees’ referrals, but it’s hard for employees to stay aware of all of your open positions. We’ve created a solution and it’s free. "
                   [:a {:class (if @read-more? "learn-more carat-up" "learn-more carat-down") :on-click #(rf/dispatch [:referral-hiring/toggle-read-more?])} (if @read-more? "Show Less" "Learn More")]]
                  (when @read-more? [rc/v-box
                                     :class "more-text"
                                     :children [[learn-more-info]]])]])))

(defn infographic
  []
  (fn []
    [rc/h-box
     :class "infographic"
     :justify :between
     :children [
                [rc/h-box
                 :class "item enroll"
                 :children [[:span {:class "text"} "Sign up"]
                            [:span {:class "arrow"}]]]
                [rc/h-box
                 :class "item tell"
                 :children [[:span {:class "text"} "We put the" [:br] "word out"]
                            [:span {:class "arrow"}]]]
                [rc/h-box
                 :class "item hires"
                 :children [[:span {:class "text"} "You get" [:br] "free referrals"]]]]]))

(defn referral-image
  []
  (fn []
    [rc/h-box
     :class "image-wrapper"
     :children [[:div {:class "referral-signup-image"}]]]))

(defn on-submit
  []
  (let [referral (rf/subscribe [:referral-hiring/referral])]
    (if (s/valid? ::db/referral @referral)
      (rf/dispatch [:referral-hiring/submit-referral])
      (rf/dispatch [:referral-hiring/show-errors?-change true]))))

(defn referral-submit-btn []
  (fn []
    [rc/v-box
     :children [[rc/button
                 :attr {:id "referral-submit-btn"}
                 :class "referral-btn"
                 :label "Sign Up"
                 :on-click on-submit]]]))

(defn name-input
  []
  (fn []
    [iv/specd-input-view
     :ns "referral-hiring"
     :type "fullname"
     :placeholder ""
     :label "Full Name"
     :spec ::db/fullname
     :error-msg "Please enter your first and last name."]))

(defn email-input
  []
  (fn []
    [iv/specd-input-view
     :ns "referral-hiring"
     :type "email"
     :placeholder ""
     :label "Email"
     :spec ::db/email
     :error-msg "Please enter your email."]))

(defn company-input
  []
  (fn []
    [iv/specd-input-view
     :ns "referral-hiring"
     :type "company"
     :placeholder ""
     :label "Company"
     :spec ::db/company
     :error-msg "Please enter your company name."]))

(defn referral-input
  []
  (let [show-errors? (rf/subscribe [:referral-hiring/show-errors?])]
    (fn []
        [iv/specd-input-view
         :ns "referral-hiring"
         :type "referral"
         :placeholder ""
         :label "Referral Bonus Amount"
         :spec ::db/referral
         :error-msg "Please enter your company's referral bonus."])))

(defn tool-tip
  []
  (let [showing? (reagent/atom false)]
    (fn []
      [rc/popover-anchor-wrapper
       :style {:position "absolute" :left "170px"}
       :showing? showing?
       :position :right-center
       :anchor [:img.info-tool-tip {:src           (img/url :tool-tip-url)
                                    :on-mouse-over (handler-fn (reset! showing? true))
                                    :on-mouse-out  (handler-fn (reset! showing? false))}]
       :popover [rc/popover-content-wrapper
                 :body [rc/v-box
                        :class "roles-tooltip"
                        :children [[:p (str "Write the highest or average referral "
                                            "bonus amount your company uses."
                                            "This is not binding, and any referral bonus amounts "
                                            "will be paid out in accordance with your company's policy.")]]]]])))

(defn referral-form
  []
  (let [showing? (reagent/atom false)
        referral (rf/subscribe [:referral-hiring/referral])]
    (fn []
      [rc/v-box
       :class "referral-form"
       :children [[rc/v-box
                   :children [
                              [:p {:class "form-header"} "Get started now."]
                              [name-input]
                              [email-input]
                              [company-input]
                              [:div {:class "tooltip-wrapper"}
                               [tool-tip]
                               [referral-input]]
                              [referral-submit-btn]]]]])))

(defn form-row
  []
  (fn []
      [rc/h-box
       :class "form-row"
       :justify :between
       :children [[referral-image] [referral-form]]]))

(defn body
  []
  (rf/dispatch [:referral-hiring/set-referral-data])
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [[rc/v-box
                 :class "content"
                 :children [[title] [deck] [infographic] [form-row]]]]]))

(defn index
  []
  (fn []
    [rc/h-box
     :class "referral-hiring main"
     :children [[body] [modals/success-modal]]]))