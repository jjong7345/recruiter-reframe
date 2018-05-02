(ns recruit-app.pricing.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate item-for-id]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.upload :as u]
            [recruit-app.modals.profile-image.views :as pi]
            [recruit-app.util.img :as img]
            [recruit-app.modals.full-access.views :as modal]))

(def pricing-table-map
  {:basic {:unlimited-posts true
           :candidate-matches true
           :unlimited-searches false
           :full-contact-info false
           :automatic-job-feeds false
           :monthly-activity-reports false
           :dedicated-account-manager false
           :promoted-job-listings false
           :employer-branding false
           :team-analytics false
           :volume-discount false}
   :individual {:unlimited-posts true
                :candidate-matches true
                :unlimited-searches true
                :full-contact-info true
                :automatic-job-feeds true
                :monthly-activity-reports true
                :dedicated-account-manager true
                :promoted-job-listings false
                :employer-branding false
                :team-analytics false
                :volume-discount false}
   :enterprise {:unlimited-posts true
                :candidate-matches true
                :unlimited-searches true
                :full-contact-info true
                :automatic-job-feeds true
                :monthly-activity-reports true
                :dedicated-account-manager true
                :promoted-job-listings true
                :employer-branding true
                :team-analytics true
                :volume-discount true}})

(defn check
  [keys]
  (fn [keys]
    (if (get-in pricing-table-map keys)
      [:img {:src (img/url :slice)}]
      [:img {:src (img/url :slice-grey)}])))

(defn table-row
  [title key]
  (fn [title key]
    [:tr
     [:th title]
     [:td [check [:basic key]]]
     [:td [check [:individual key]]]
     [:td [check [:enterprise key]]]]))

(defn authenticated-user-btn
  [plan]
  (let [current-plan (rf/subscribe [:pricing/plan])]
    (fn [plan]
      (let [is-current-plan? (= plan @current-plan)]
        [rc/button
         :class (str "btn" (when is-current-plan? " current-plan"))
         :label (str (if is-current-plan?
                       "Current Plan"
                       "Get Started"))
         :on-click (when (not is-current-plan?)
                     #(rf/dispatch [:pricing/get-started plan]))]))))

(defn unauthenticated-user-btn
  []
  (fn []
    [rc/button
     :class "btn"
     :label "Get Started"
     :on-click #(rf/dispatch [:pricing/go-to-sign-up])]))

(defn get-started-btn
  [plan]
  (let [is-authenticated? (rf/subscribe [:recruiter/is-authenticated?])]
    (fn [plan]
      (if @is-authenticated?
        [authenticated-user-btn plan]
        [unauthenticated-user-btn]))))

(defn pricing-table
  []
  (fn []
    [rc/v-box
     :class "pricing-table"
     :children [[:table
                  [:tbody
                   [:tr
                    [:th.cat-title "Recruiter Access Solutions"]
                    [:td {:class "title-cell basic"}
                     [:h2 "Basic"]
                     [:div "Free"]
                     [:p "Allows you to post jobs and see limited resumes without contact information."]]
                    [:td {:class "title-cell individual"}
                     [:h2 "Individual"]
                     [:div "$4,850 / Year"]
                     [:p "Single seat license allows you to post jobs and see full contact information."]]
                    [:td {:class "title-cell enterprise"}
                     [:h2 "Enterprise"]
                     [:div "Custom Pricing"]
                     [:p "Tailored plan for multiple licenses. Maximizes candidate reach, analyzes team performance and elevates your brand."]]]
                   [table-row "Unlimited Posts" :unlimited-posts]
                   [table-row "Candidate Matches" :candidate-matches]
                   [table-row "Unlimited Searches" :unlimited-searches]
                   [table-row "Full Contact Information" :full-contact-info]
                   [table-row "Automatic Job Feeds" :automatic-job-feeds]
                   [table-row "Monthly Activity Reports" :monthly-activity-reports]
                   [table-row "Dedicated Account Manager" :dedicated-account-manager]
                   [table-row "Promoted Job Listings" :promoted-job-listings]
                   [table-row "Employer Branding" :employer-branding]
                   [table-row "Team Analytics" :team-analytics]
                   [table-row "Volume Discount" :volume-discount]
                   [:tr.btn-container
                    [:th]
                    [:td.plan
                     [get-started-btn "basic"]]
                    [:td.plan
                     [get-started-btn "individual"]]
                    [:td.plan
                     [get-started-btn "enterprise"]]]]]]]))

(defn plan-list
  []
  (fn [plan-key]
    [rc/v-box
     :class "plan-detail"
     :children [[:ul
                 [:li [check [plan-key :unlimited-posts]] "Unlimited Posts"]
                 [:li [check [plan-key :candidate-matches]] "Candidate Matches"]
                 [:li [check [plan-key :unlimited-searches]] "Unlimited Searches"]
                 [:li [check [plan-key :full-contact-info]] "Full Contact Information"]
                 [:li [check [plan-key :automatic-job-feeds]] "Automatic Job Feeds"]
                 [:li [check [plan-key :monthly-activity-reports]] "Monthly Activity Reports"]
                 [:li [check [plan-key :dedicated-account-manager]] "Dedicated Account Manager"]
                 [:li [check [plan-key :promoted-job-listings]] "Dedicated Account Manager"]
                 [:li [check [plan-key :employer-branding]] "Employer Branding"]
                 [:li [check [plan-key :team-analytics]] "Team Analytics"]
                 [:li [check [plan-key :volume-discount]] "Volume Discount"]]]]))

(defn basic-mobile-view
  []
  (fn []
    [rc/v-box
     :class "basic-mobile-view"
     :children [[:div {:class "title-cell basic"}
                 [:h2 "Basic"]
                 [:div "Free"]
                 [:p "Allows you to post jobs and see limited resumes without contact information."]]
                [plan-list :basic]
                [:div.btn-container
                 [get-started-btn "basic"]]]]))

(defn individual-mobile-view
  []
  (fn []
    [rc/v-box
     :class "individual-mobile-view"
     :children [[:div {:class "title-cell individual"}
                 [:h2 "Individual"]
                 [:div "$4,850 / Year"]
                 [:p "Single seat license allows you to post jobs and see full contact information."]]
                [plan-list :individual]
                [:div.btn-container
                 [get-started-btn "individual"]]]]))

(defn enterprise-mobile-view
  []
  (fn []
    [rc/v-box
     :class "enterprise-mobile-view"
     :children [[:div {:class "title-cell enterprise"}
                 [:h2 "Enterprise"]
                 [:div "Custom Pricing"]
                 [:p "Tailored plan for multiple licenses. Maximizes candidate reach, analyzes team performance and elevates your brand."]]
                [plan-list :enterprise]
                [:div.btn-container
                 [get-started-btn "enterprise"]]]]))

(defn pricing-mobile-view
  []
  (fn []
    [rc/v-box
     :class "mobile-view"
     :children [[:h1.car-title "Recruiter Access Solutions"]
                [basic-mobile-view]
                [individual-mobile-view]
                [enterprise-mobile-view]]]))

(defn body
  []
  (fn []
    [rc/v-box
     :class "content-holder"
     :children [[:h1 "Select Your Plan"]
                [pricing-table]
                [pricing-mobile-view]]]))

(defn index
  []
  (fn []
    [rc/v-box
     :class "pricing main"
     :children [[body] [modal/thank]]]))