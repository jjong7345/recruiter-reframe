(ns recruit-app.post-job.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [re-com.util :refer [enumerate]]
            [recruit-app.util.typeahead :refer [typeahead]]
            [cljs.reader :refer [read-string]]
            [cljs.spec.alpha :as s]
            [reagent.core :as reagent]
            [recruit-app.post-job.db :as db]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.job :as ju]
            [recruit-app.util.quill :as quill]
            [recruit-app.util.number_format :as nf]
            [clojure.string :as cs]
            [recruit-app.util.img :as img]
            [recruit-app.components.loading :as l]))

(def min-max (partial iv/min-max-slider "post-job"))
(def exp-tabs
  [{:id 6 :label "Not Specified"}
   {:id 1 :label "Less than 5"}
   {:id 2 :label "5-7"}
   {:id 3 :label "8-10"}
   {:id 4 :label "11-15"}
   {:id 5 :label "15+"}])

(def denomination-tabs
  [{:id "%"  :label "%" :tab-string "%"}
   {:id "$"  :label "$" :tab-string "$"}])

(defn exp-picker []
  (let [exp (rf/subscribe [:post-job/exp])]
    [rc/v-box
     :class "exp-picker"
     :children [[rc/label :class "input-label" :label "Years of Experience"]
                [rc/horizontal-bar-tabs
                 :model exp
                 :tabs exp-tabs
                 :on-change #(rf/dispatch [:post-job/exp-change %])]]]))

(defn bonus-picker []
  (let [denom (rf/subscribe [:post-job/denom])
        digits? #"^\$?(?:\d*)?\d*\%?K?$"]
    (fn []
      [rc/v-box
       :class "denomination-tabs"
       :children [[rc/h-box
                   :justify :between
                   :children [[rc/label :class "input-label exterior" :label "Bonus/Commission"]
                              [rc/label :class "input-label exterior" :label "(Optional)"]]]
                  [rc/h-box
                   :children [[rc/horizontal-bar-tabs
                               :model denom
                               :tabs denomination-tabs
                               :on-change #(rf/dispatch [:post-job/denom-change %])]
                              [iv/input :ns "post-job" :type "bonus" :label "" :info "" :placeholder "Enter bonus" :validation digits?]
                              ]]]])))

(defn location-element [location]
  (fn [location]
    [rc/h-box
     :class "location-tag"
     :children [[rc/label :class "loc" :label (:name location)]
                [rc/button :label "x" :on-click #(rf/dispatch [:post-job/remove-loc (:name location)])]]]))

(defn location-picker
  [ns info input-type]
  (let [location (rf/subscribe [:post-job/location])
        locations (rf/subscribe [:post-job/locations])
        show-errors? (rf/subscribe [:post-job/show-errors?])]
    (fn []
      [rc/h-box
       :width "none"
       :class "location-holder holder"
       :children [[rc/v-box
                   :children [[rc/h-box
                               :justify :between
                               :children [[rc/label :class "input-label" :label "Add Location (City, or Zip Code)"]
                                          [rc/label :class "sub-info" :label "Add multiple locations"]]]
                              [rc/typeahead
                               :class (str "location" (when (and @show-errors? (not (s/valid? ::db/locations @locations))) " error"))
                               :placeholder "e.g. New York, NY"
                               :data-source #(rf/dispatch [:post-job/loc-auto %1 %2])
                               :on-change #(rf/dispatch [:post-job/add-loc %])
                               :model location
                               :width "none"
                               :suggestion-to-string #(:name %)
                               :render-suggestion #(:name %)
                               :change-on-blur? true]
                              [iv/error-message "post-job" @locations ::db/locations "Please enter a valid city and state."]]]
                  (when (seq @locations) [rc/v-box
                                          :class "selected-locations"
                                          :children [[rc/label :class "input-label" :label "HIRING LOCATION(S)"]
                                                     [rc/h-box :class "location-tags" :style {:flex-flow "row wrap"} :children (mapv #(vector location-element %) @locations)]]])]])))



(defn min-comp-dropdown []
  [iv/drpdn-view
   :ns "post-job"
   :type "min-comp"
   :choices (dd/salaries "Select min")
   :spec ::db/id
   :error-msg [:div {:style {:width "138px"}} "Please select the minimum salary"]])

(defn max-comp-dropdown []
  [iv/drpdn-view
   :ns "post-job"
   :type "max-comp"
   :choices (dd/salaries "Select max")
   :spec ::db/id
   :error-msg [:div {:style {:width "138px"}} "Please select the \n maximum salary"]])

(defn industry-dropdown []
  [iv/drpdn-view
   :ns "post-job"
   :type "industry"
   :label "Industry"
   :choices (dd/industry)
   :spec ::db/id
   :error-msg "Company Industry needs to be selected."])

(defn employee-dropdown []
  [iv/drpdn-view
   :ns "post-job"
   :type "employee"
   :label "Number of employees"
   :choices (dd/employees)
   :spec ::db/id
   :error-msg "Number of Employees needs to be selected."])

(defn job-info []
  (let [job-desc (rf/subscribe [:post-job/job-desc])
        showing? (reagent/atom false)
        show-errors? (rf/subscribe [:post-job/show-errors?])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :children [[rc/box :class "no-comm"
                               :child "Only full-time positions with a base salary, or contracts with a length of 12+ months."]
                              [rc/popover-anchor-wrapper
                               :showing? showing?
                               :position :right-center
                               :anchor [:img.info-tool-tip {:src           (img/url :tool-tip-url)
                                                            :on-mouse-over (handler-fn (reset! showing? true))
                                                            :on-mouse-out  (handler-fn (reset! showing? false))}]
                               :popover [rc/popover-content-wrapper
                                         :body [rc/v-box
                                                :class "roles-tooltip"
                                                :children [[:p.title "Are there any types of roles I cannot post? "]
                                                           [:p (str "We do not permit any commision only, financial advisor, "
                                                                    "part-time/contract roles less than 12 months, "
                                                                    "or jobs with less than $80K in total compensation onto the site.")]]]]]]]
                  [iv/specd-input-view
                   :ns "post-job"
                   :type "job-title"
                   :placeholder "e.g. Sales Manager"
                   :label "Job title"
                   :spec ::db/job.title
                   :error-msg "This Job Title thing seems important. You're going to want to put something here."]
                  [rc/v-box :class (str "quill-holder" (when (and @show-errors? (not (s/valid? ::db/fullDescription @job-desc))) " error"))
                   :children [[rc/h-box
                               :justify :between
                               :children [[rc/label :class "input-label" :label "JOB DESCRIPTION"]
                                          [rc/label :class "info" :label "No contact info please"]]]
                              [quill/editor
                               {:id           "job-desc"
                                :content      @job-desc
                                :selection    nil
                                :on-change-fn #(rf/dispatch [:post-job/job-desc-change %2])}]
                              [iv/error-message "post-job" @job-desc ::db/fullDescription "Applicants will want a few more details. This section must contain at least 100 words."]]]
                  [rc/v-box
                   :class "input-holder"
                   :style {:width "1000px"}
                   :children [[exp-picker]]]]])))

(defn comp-info []
  (let [min-comp (rf/subscribe [:post-job/min-comp])
        max-comp (rf/subscribe [:post-job/max-comp])
        min-total-comp (rf/subscribe [:post-job/min-total-comp])
        max-total-comp (rf/subscribe [:post-job/max-total-comp])
        hide-salary? (rf/subscribe [:post-job/hide-salary])
        bonus (rf/subscribe [:post-job/bonus-string])
        other (rf/subscribe [:post-job/other])
        show-errors? (rf/subscribe [:post-job/show-errors?])
        compensation-not-valid? (rf/subscribe [:post-job/compensation-not-valid?])
        salary-label (rf/subscribe [:post-job/salary-label])
        base-salary-label (rf/subscribe [:post-job/base-salary-label])]
    (fn []
      [rc/v-box
       :class "comp-holder"
       :children [[rc/label
                   :class "title"
                   :label "Total compensation at least $80K"]
                  [rc/h-box
                   :children [[rc/v-box
                               :class "entry-holder"
                               :children [[rc/h-box
                                           :class "salary-labels"
                                           :justify :between
                                           :children [[rc/label :class "input-label" :label "Estimated salary range"]]]
                                          [rc/h-box :class "comp-range" :justify :between
                                           :children [[min-comp-dropdown] [rc/label :label "TO"] [max-comp-dropdown]]]
                                          [rc/h-box :class "error-msg" :justify :between
                                           :children [(if (and @hide-salary? (= @min-comp 0) (not @show-errors?))
                                                        [:div {:style {:width "138px"}} "Please select the \n minimum salary"])
                                                      (if (and @hide-salary? (= @max-comp 0) (not @show-errors?))
                                                        [:div {:style {:width "138px" :margin-left "auto"}} "Please select the \n maximum salary"])]]
                                          (when (and (> @min-comp 0) (> @max-comp 0) (> @min-comp @max-comp)) [rc/label :class "error-msg" :label "Min salary must be less than max salary"])
                                          [rc/v-box
                                           :children [[bonus-picker]
                                                      [iv/input :ns "post-job" :type "other" :label "OTHER - OPTIONAL" :info "Commission, Equity, etc." :placeholder "e.g. 0.05% Equity"]]]
                                          [iv/checkbox
                                           :model hide-salary?
                                           :label "Hide salary from jobseekers"
                                           :name "show-salary"
                                           :on-change (fn [%] (rf/dispatch [:post-job/hide-salary-change %]))]
                                          (if (and @hide-salary? (or (= @min-comp 0) (= @max-comp 0)))
                                            [rc/label :class "error-msg" :label "We won't show jobseekers the salary range for this job, \nbut it's still required."])]]
                              [rc/v-box
                               :class "total-comp"
                               :children [(when @compensation-not-valid?
                                            [rc/label :class "error-msg" :label (str "Total compensation must be at least $80K. "
                                                                                     "Please adjust the minimum salary or the bonus/commission amount. ")])
                                          [rc/h-box
                                           :justify :between
                                           :children [[rc/label :class "comp-label" :label "Base Salary Range:"]
                                                      [rc/label :class "comp-info" :label (cond @hide-salary? "DOE"
                                                                                                (= @max-total-comp 0) (nf/remove-cents (str (nf/number-conversion @min-total-comp)))
                                                                                                (and (> @min-comp 0) (> @max-comp 0)) @base-salary-label
                                                                                                :else "---")]]]
                                          [rc/h-box
                                           :justify :between
                                           :children [[rc/label :class "comp-label" :label "Bonus/Commission:"]
                                                      [rc/label :class "comp-info" :label (ju/display-bonus @bonus "---")]]]
                                          [rc/h-box
                                           :justify :between
                                           :children [[rc/label :class "comp-label" :label "Other:"]
                                                      [rc/label :class "comp-info" :label (or (seq @other) "---")]]]
                                          [rc/label :class "amount-sub" :label "Total Compensation"]
                                          [rc/label :class "amount" :label @salary-label]]]]]]])))

(defn recruiter-info
  []
  (let [rec-firstname (rf/subscribe [:recruiter/firstname])
        rec-lastname (rf/subscribe [:recruiter/lastname])
        profile-img (rf/subscribe [:recruiter/profile-img])
        hide-rec (rf/subscribe [:post-job/hide-recruiter])
        has-photo (rf/subscribe [:recruiter/has-photo?])]
    (fn []
      [rc/v-box
       :class "recruiter-holder holder"
       :children [[rc/h-box
                   :class "rec-profile"
                   :justify :between
                   :children [[rc/label :class "input-label" :label "Recruiter profile"]]]
                  [rc/h-box
                   :class "rec-holder"
                   :children [(if @has-photo
                                [:img {:src      @profile-img
                                       :on-error #(rf/dispatch [:recruiter/has-photo?-change false])}]
                                [:img {:src (img/url :default-profile-img)}])
                              [rc/label
                               :class "name"
                               :label (str @rec-firstname " " @rec-lastname (when @hide-rec " - NOT SHOWN"))]]]
                  [iv/checkbox
                   :model hide-rec
                   :name "show-recruiter"
                   :label "Hide my recruiter profile from job seekers"
                   :on-change (fn [%] (rf/dispatch [:post-job/hide-recruiter-change %]))]]])))

(defn company-name
  []
  (let [hide-company (rf/subscribe [:post-job/hide-company])
        company (rf/subscribe [:post-job/company])
        show-errors? (rf/subscribe [:post-job/show-errors?])]
    (fn []
      [rc/v-box
       :class "company-name-holder"
       :children [[rc/h-box
                   :children [[rc/label :class "input-label" :label "COMPANY"]]]
                  [typeahead
                   :value (if @hide-company "Confidential Company" @company)
                   :class (str "company" (when (and @show-errors? (not (s/valid? ::db/company.name @company))) " error"))
                   :placeholder "Please enter a company name"
                   :data-source  #(rf/dispatch [:post-job/com-auto %1 %2])
                   :on-change #(rf/dispatch [:post-job/add-com %])
                   :rigid? true
                   :width "none"
                   :suggestion-to-string #(:name %)
                   :render-suggestion #(:name %)
                   :change-on-blur? true
                   :disabled? (if @hide-company true false)]
                  [iv/error-message "post-job" @company ::db/company.name "Please ensure your company name is between 2 and 60 characters."]
                  [iv/checkbox
                   :model hide-company
                   :name "show-company"
                   :label "Hide company name from job seekers"
                   :on-change (fn [%] (rf/dispatch [:post-job/hide-company-change %]))]]])))

(defn company-info []
  (fn []
    [rc/v-box
     :class "company-holder"
     :children [[rc/label :class "title" :label "Company Details"]
                [rc/h-box
                 :children [[company-name]
                            [recruiter-info]]]
                [rc/h-box
                 :children [[industry-dropdown]
                            [employee-dropdown]]]
                [location-picker]]]))

(defn proceede-btn []
  (fn []
    (let [valid-job (rf/subscribe [:post-job/valid-job])
          show-errors? (rf/subscribe [:post-job/show-errors?])]
      [rc/v-box
       :children [[rc/button
                   :attr {:id "view-preview-btn"}
                   :class "preview-btn"
                   :label "Proceed to Preview"
                   :on-click #(rf/dispatch [:post-job/see-preview])]
                  (when (and @show-errors? (not @valid-job))
                    [rc/label
                     :class "error-msg"
                     :label "Not all required information has been provided"])]])))

(defn body
  []
  (fn []
    [rc/v-box
     :class "content-holder"
     :children [[rc/h-box
                 :class "progress-status"
                 :children
                 [[:div.bar]
                  [:div.arrow]]]
                [rc/h-box
                 :class "title-wrapper"
                 :children [[rc/h-box
                             :class "title-content"
                             :justify :between
                             :children [[rc/label
                                         :class "title"
                                         :label "Create a Job Post"]
                                        [proceede-btn]]]]]
                [rc/v-box
                 :class "content"
                 :justify :start
                 :children [[job-info]
                            [:div.divider]
                            [comp-info]
                            [:div.divider]
                            [company-info]]]
                [rc/h-box
                 :class "title-wrapper preview-holder"
                 :children [[rc/h-box
                             :class "title-content"
                             :justify :end
                             :children [[proceede-btn]]]]]]]))

(defn index []
  (let [editing? (rf/subscribe [:post-job/editing?])
        page-loaded? (rf/subscribe [:post-job/page-loaded?])]
    (rf/dispatch [:post-job/load-view])
    (fn []
      (when @editing?
        (rf/dispatch [:jobs/edit]))
      [rc/v-box
       :class "post-job main"
       :children [(if @page-loaded?
                    [body]
                    [l/loading-page])]])))