(ns recruit-app.superuser.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [recruit-app.util.input-view :as iv]
            [goog.date.Date :as d]
            [recruit-app.util.dropdowns :as dd]
            [cljs.spec.alpha :as s]
            [recruit-app.superuser.db :as db]
            [cljs-time.format :as f]
            [goog.string :as gs]
            [recruit-app.util.job :as ju]
            [recruit-app.util.date :as dt]
            [recruit-app.config :as config]
            [recruit-app.components.security :as sec]))

(def tab-options
  "This is the model for the tabs"
  [{:id :change-user :label "Change User"}
   {:id :redact-permissions :label "Redact Permissions"}
   {:id :feature-job :label "Job Promotions"}
   {:id :sales :label "Sales"}])

(defn tabs
  []
  (let [tab-id (rf/subscribe [:superuser/active-tab])]
    (fn []
      [rc/horizontal-tabs
       :model tab-id
       :tabs tab-options
       :on-change #(rf/dispatch [:superuser/active-tab-change %])])))

(defn repost-job-form
  []
  (let [job-id (rf/subscribe [:superuser/repost-job-id])]
    [rc/h-box
     :justify :between
     :align :end
     :class "superuser-form inline-form"
     :children [[iv/input
                 :ns "superuser"
                 :type "repost-job-id"
                 :label "Refresh publication date - Job ID"]
                [rc/button
                 :label "Refresh"
                 :class "btn"
                 :on-click #(rf/dispatch [:superuser/repost-job job-id])]]]))

(defn change-user-form
  []
  [rc/h-box
   :justify :between
   :align :end
   :class "superuser-form inline-form"
   :children [[iv/actionable-on-enter-key
               [iv/input
                :ns "superuser"
                :type "impersonate-recruiter-id"
                :label "Subscriber ID"]
               #(rf/dispatch [:superuser/impersonate-user])]
              [rc/button
               :label "Login"
               :class "btn"
               :on-click #(rf/dispatch [:superuser/impersonate-user])]]])

(defn change-user
  []
  [rc/v-box
   :align :center
   :class "tab-body"
   :children [[:span "As a SuperUser, you have the ability to co-browse the
                      Ladders website as the customer will see it."]
              [:span "Please enter Subscriber ID below."]
              [change-user-form]]])

(defn- valid-dates?
  "If both start-date and end-date are given, valid if start date is before end"
  [start-date end-date]
  (or (not start-date)
      (not end-date)
      (> 0 (d/compare start-date end-date))))

(defn on-start-date-change
  "Validates that start date is before end date and dispatches correct event"
  [end-date start-date]
  (if (valid-dates? start-date end-date)
    (rf/dispatch [:superuser/redact-start-date-change (dt/unix-timestamp start-date)])
    (rf/dispatch [:alerts/add-error "Start Date Must Be Before End Date"])))

(defn on-end-date-change
  "Validates that end date is before end date and dispatches correct event"
  [start-date end-date]
  (if (valid-dates? start-date end-date)
    (rf/dispatch [:superuser/redact-end-date-change (dt/unix-timestamp end-date)])
    (rf/dispatch [:alerts/add-error "End Date Must Be After Start Date"])))

(defn redact-datepickers
  "Renders a start date and end date input"
  []
  (let [start-date (rf/subscribe [:superuser/active-redact-start-date])
        end-date (rf/subscribe [:superuser/active-redact-end-date])]
    (fn []
      [rc/h-box
       :justify :between
       :children [[iv/date-picker
                   :label "Start Date"
                   :model start-date
                   :on-change (partial on-start-date-change @end-date)]
                  [iv/date-picker
                   :label "End Date"
                   :model end-date
                   :on-change (partial on-end-date-change @start-date)]]])))

(defn redact-submit
  "Renders submit button for redaction permission form"
  []
  [rc/button
   :label "Submit"
   :on-click #(rf/dispatch [:superuser/save-redact-permissions])])

(defn remove-redact-permissions-btn
  "Renders button to remove redact permissions"
  []
  [rc/button
   :label "Remove Permissions"
   :on-click #(rf/dispatch [:superuser/remove-redact-permissions])])

(def unlimited-disclaimer
  "Disclaimer telling superuser that this user has legacy privileges"
  [:span.unlimited-disclaimer
   "Note: This user has a legacy privilege that allows them to view unredacted resumes
    with no start or end date. Adding a start or end date will override this privilege
    and clicking Remove Permissions will remove it."])

(defn edit-permissions-form
  "Renders redact datepickers and submit button"
  []
  (let [show-disclaimer? (rf/subscribe [:superuser/has-unlimited-redaction-permission?])]
    (fn []
      [rc/v-box
       :class "edit-permissions-form"
       :children [(when @show-disclaimer? unlimited-disclaimer)
                  [redact-datepickers]
                  [rc/h-box
                   :justify :between
                   :children [[redact-submit]
                              [remove-redact-permissions-btn]]]]])))

(defn redact-permissions-form
  []
  (let [show-permissions? (rf/subscribe [:superuser/show-redact-permissions?])]
    (fn []
      [rc/v-box
       :class "superuser-form redact-permissions-form"
       :children [[rc/h-box
                   :justify :between
                   :align :end
                   :class "inline-form"
                   :children [[iv/actionable-on-enter-key
                               [iv/input
                                :ns "superuser"
                                :type "redact-permissions-recruiter-id"
                                :label "Recruiter ID"]]
                              [rc/button
                               :label "Manage"
                               :class "manage-btn"
                               :on-click #(rf/dispatch [:superuser/fetch-redact-permissions])]]]
                  (when @show-permissions?
                    [edit-permissions-form])]])))

(defn redact-permissions
  []
  [rc/v-box
   :align :center
   :class "tab-body"
   :children [[:span "Set a recruiter to view unredacted resumes"]
              [redact-permissions-form]]])

(defn job-info-cell
  "Returns an inline cell with label and info"
  [& {:keys [label value]}]
  [rc/h-box
   :justify :between
   :children [[rc/label
               :label label]
              [:span value]]])

(defn feature-job-dates
  []
  (let [feature-job-start-date (rf/subscribe [:superuser/feature-job-start-date])
        feature-job-end-date (rf/subscribe [:superuser/feature-job-end-date])]
    (fn []
      [rc/h-box
       :class "feature-job-dates"
       :justify :between
       :children [[iv/date-picker
                   :label "Start Date"
                   :model @feature-job-start-date
                   :on-change #(rf/dispatch [:superuser/feature-job-start-date-change %])]
                  [iv/date-picker
                   :label "End Date"
                   :model @feature-job-end-date
                   :on-change #(rf/dispatch [:superuser/feature-job-end-date-change %])]]])))

(defn submit-job-promotion-btn
  "Renders submit button for job promotion form"
  []
  [rc/button
   :label "Save"
   :on-click #(rf/dispatch [:superuser/save-job-promotion])])

(defn cancel-job-promotion-btn
  "Renders cancel button for job promotion form"
  []
  [rc/button
   :label "Cancel Promotion"
   :on-click #(rf/dispatch [:superuser/cancel-job-promotion])])

(defn promotion-form
  []
  (let [show-cancel-promotion? (rf/subscribe [:superuser/show-cancel-promotion?])]
    (fn []
      [rc/v-box
       :class "promotion-form"
       :justify :between
       :children [[feature-job-dates]
                  [rc/h-box
                   :justify :between
                   :children [[submit-job-promotion-btn]
                              (when @show-cancel-promotion?
                                [cancel-job-promotion-btn])]]]])))

(defn feature-job-form
  "Renders job info and form to edit feature job"
  []
  (let [job (rf/subscribe [:superuser/feature-job])]
    (fn []
      (when @job
        [rc/v-box
         :class "feature-job-form"
         :children [[job-info-cell
                     :label "Posted:"
                     :value (ju/posted-str @job)]
                    [job-info-cell
                     :label "Expires:"
                     :value (ju/expire-string @job)]
                    [job-info-cell
                     :label "Title:"
                     :value (:title @job)]
                    [job-info-cell
                     :label "Company:"
                     :value (:company_name @job)]
                    [job-info-cell
                     :label "Compensation:"
                     :value (-> @job :compensation_salary gs/unescapeEntities)]
                    [job-info-cell
                     :label "Status:"
                     :value (:job_status @job)]
                    [promotion-form]]]))))

(defn feature-job-panel
  []
  (fn []
    [rc/v-box
     :class "superuser-form"
     :children [[rc/h-box
                 :justify :between
                 :align :end
                 :class "inline-form"
                 :children [[iv/actionable-on-enter-key
                             [iv/input
                              :ns "superuser"
                              :type "feature-job-id"
                              :label "Job ID"]]
                            [rc/button
                             :label "Load"
                             :class "manage-btn"
                             :on-click #(rf/dispatch [:superuser/load-job])]]]
                [feature-job-form]]]))

(defn feature-job
  []
  [rc/v-box
   :align :center
   :class "tab-body"
   :children [[:span "As a SuperUser, you have the ability to manage job promotions."]
              [feature-job-panel]
              (when config/show-admin-refresh?
                [repost-job-form])]])

(defn inventory-recruiter-id-input
  "Renders recruiter-id input and Load button"
  []
  [rc/h-box
   :justify :center
   :align :end
   :class "inline-form"
   :children [[iv/input
               :ns "superuser"
               :type "inventory-recruiter-id"
               :label "Recruiter ID"]
              [rc/button
               :label "Load"
               :class "load-inventory-btn"
               :on-click #(rf/dispatch [:superuser/fetch-inventory])]]])

(defn- parsed-date
  "Parses inventory record insert date to datetime object"
  ([string]
   (parsed-date string (f/formatters :date-time-no-ms)))
  ([string formatter]
   (try
     (f/parse formatter string)
     (catch js/Error e (println e)))))

(defn- formatted-date
  "Returns inventory record insert time formatted to M/dd/YY"
  [string]
  (when-let [parsed (or (parsed-date string)
                        (parsed-date string (f/formatter "YYYY-MM-dd")))]
    (f/unparse (f/formatter "M/dd/YY") parsed)))

(defn inventory-history-item
  [{:keys [type qty insert-date action for]}]
  [:tr
   [:td.action action]
   [:td.type type]
   [:td.qty qty]
   [:td.insert-date (formatted-date insert-date)]
   [:td.use-for for]])

(defn inventory-table
  [& rows]
  (reduce
    conj
    [:tbody.inventory-table [:tr
                             [:th.action "Action"]
                             [:th.type "Type"]
                             [:th.qty "Quantity"]
                             [:th.insert-date "Insert Date"]
                             [:th.use-for "For"]]]
    rows))

(defn inventory-history-list
  "Renders list of inventory history for recruiter"
  []
  (let [inventory-history (rf/subscribe [:superuser/active-inventory-list])]
    (fn []
      [rc/box
       :class "inventory-history-list"
       :child [inventory-table
               (map (partial vector inventory-history-item) @inventory-history)]])))

(defn load-inventory-form
  "Renders recruiter ID input and inventory list"
  []
  (let [show-inventory? (rf/subscribe [:superuser/show-inventory?])]
    (fn []
      [rc/v-box
       :class "superuser-form load-inventory-form"
       :children [[inventory-recruiter-id-input]
                  (when @show-inventory? [inventory-history-list])]])))

(defn inventory-action-dropdown
  "Renders dropdown to choose action to take on recruiter inventory"
  []
  (let [action (rf/subscribe [:superuser/inventory-action])]
    (fn []
      [rc/v-box
       :class "inventory-dropdown-holder"
       :children [[rc/label
                   :label "Action"]
                  [rc/single-dropdown
                   :model action
                   :class "inventory-dropdown"
                   :on-change #(rf/dispatch [:superuser/inventory-action-change %])
                   :choices dd/inventory-actions]]])))

(defn inventory-type-dropdown
  "Renders dropdown to choose type of inventory to take action on"
  []
  (let [type (rf/subscribe [:superuser/inventory-type])]
    (fn []
      [rc/v-box
       :class "inventory-dropdown-holder"
       :children [[rc/label
                   :label "Type"]
                  [rc/single-dropdown
                   :model type
                   :class "inventory-dropdown"
                   :on-change #(rf/dispatch [:superuser/inventory-type-change %])
                   :choices dd/inventory-types]]])))

(defn inventory-quantity-input
  "Renders input for quantity of inventory to use/purchase"
  []
  [iv/input
   :ns "superuser"
   :type "inventory-quantity"
   :label "Quantity"])

(defn save-inventory-btn
  "Renders button to save inventory"
  []
  (let [inventory-form (rf/subscribe [:superuser/inventory-form])]
    (fn []
      [rc/button
       :label "Save"
       :disabled? (not (s/valid? ::db/inventory-form @inventory-form))
       :on-click #(rf/dispatch [:superuser/save-inventory])])))

(defn inventory-action-form
  []
  [rc/v-box
   :class "superuser-form inventory-action-form"
   :children [[inventory-action-dropdown]
              [inventory-type-dropdown]
              [inventory-quantity-input]
              [save-inventory-btn]]])

(defn sales-inventory-form
  []
  (let [show-actions? (rf/subscribe [:superuser/show-inventory?])]
    (fn []
      [rc/h-box
       :justify :center
       :class "sales-inventory-form"
       :children [[load-inventory-form]
                  (when @show-actions?
                    [inventory-action-form])]])))

(defn sales
  []
  [rc/v-box
   :align :center
   :class "tab-body"
   :children [[:span "View/Update Inventory For Recruiter"]
              [sales-inventory-form]]])

(defn body
  []
  (let [selected-tab (rf/subscribe [:superuser/active-tab])]
    (fn []
      [rc/box
       :class "content"
       :child (condp = @selected-tab
                :change-user [change-user]
                :redact-permissions [redact-permissions]
                :feature-job [feature-job]
                :sales [sales])])))

(defn header
  []
  [rc/v-box
   :class "title-container"
   :children [[rc/v-box
               :class "title-box"
               :children [[rc/label
                           :class "title"
                           :label "Superuser"]
                          [:p
                           {:class "subtitle"}
                           "As a superuser, you have access to impersonate users, edit redact permissions,
                           edit job promotions, and add or remove sales inventory for recruiters."]
                          [tabs]]]]])

(defn index
  []
  [sec/superuser-content
   [rc/v-box
    :class "superuser main"
    :children [[header] [body]]]])
