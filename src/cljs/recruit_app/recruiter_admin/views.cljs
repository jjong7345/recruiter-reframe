(ns recruit-app.recruiter-admin.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.box :as b]
            [recruit-app.util.date :as d]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.button :as btn]
            [recruit-app.components.header :as h]
            [recruit-app.components.table :as table]
            [recruit-app.components.form :as form]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.modal :as modal]
            [recruit-app.specs.common :as common]
            [recruit-app.specs.recruiter-admin :as specs]
            [recruit-app.components.security :as sec]
            [recruit-app.modals.teams.views :as teams-modals]
            [cljs-time.coerce :as c]
            [recruit-app.util.sort :as su]
            [recruit-app.util.uri :as u]
            [recruit-app.components.typography :as type]))

(defn basic-profile-info
  "Displays recruiter-id, status, last login, and date created"
  []
  (let [recruiter-id (rf/subscribe [:recruiter-admin/active-recruiter-id])
        status (rf/subscribe [:recruiter-admin/active-recruiter-profile-status])
        subscribe-date (rf/subscribe [:recruiter-admin/active-recruiter-subscribe-date-display])]
    (fn []
      [layout/column
       :padding 0
       :children [[form/inline-display
                   :label "Recruiter ID"
                   :value @recruiter-id]
                  [form/inline-display
                   :label "Status"
                   :value @status]
                  [form/inline-display
                   :label "Date Created"
                   :value @subscribe-date]]])))

(defn profile-actions
  "Displays actions that can be taken on a recruiter"
  []
  (let [recruiter-id (rf/subscribe [:recruiter-admin/active-recruiter-id])
        recruiter (rf/subscribe [:recruiter-admin/active-recruiter])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding-top 18
                   :padding-bottom 6
                   :children [[layout/col-left
                               :children [[btn/primary-dashboard-button
                                           :label "Edit"
                                           :on-click #(rf/dispatch [:recruiter-admin/editing-recruiter-change @recruiter])]]]
                              [layout/col-right
                               :children [[btn/secondary-dashboard-button
                                           :label "Approve"
                                           :on-click #(rf/dispatch [:recruiter-admin/approve-recruiter @recruiter-id])]]]]]
                  [layout/row
                   :padding 6
                   :children [[layout/col-left
                               :children [[btn/secondary-dashboard-button
                                           :label "Escalate"
                                           :on-click #(rf/dispatch [:recruiter-admin/escalate-recruiter @recruiter-id])]]]
                              [layout/col-right
                               :children [[btn/secondary-dashboard-button
                                           :label "Decline"
                                           :on-click #(rf/dispatch [:recruiter-admin/decline-recruiter @recruiter-id])]]]]]
                  [layout/row-bottom
                   :padding-top 6
                   :justify :center
                   :children [[btn/secondary-dashboard-button
                               :label "Permanently Decline"
                               :on-click #(rf/dispatch [:recruiter-admin/permanently-decline-recruiter @recruiter-id])]]]]])))

(defn edit-profile-actions
  "Renders buttons to cancel or save changes to profile being edited"
  []
  (let [recruiter (rf/subscribe [:recruiter-admin/editing-recruiter])
        valid? (rf/subscribe [:recruiter-admin/form-valid?])]
    (fn []
      [layout/row
       :padding-top 24
       :align :center
       :children [[layout/col-left
                   :class "col-xs-4"
                   :align :center
                   :children [[link/hyperlink
                               :label "Cancel"
                               :on-click #(rf/dispatch [:recruiter-admin/editing-recruiter-change nil])]]]
                  [layout/col-right
                   :class "col-xs-8"
                   :align :center
                   :children [[btn/secondary-dashboard-button
                               :label "Save Changes"
                               :on-click #(rf/dispatch (if @valid?
                                                         [:recruiter-admin/save-recruiter @recruiter]
                                                         [:recruiter-admin/show-errors?-change true]))]]]]])))

(defn basic-profile-info-box
  "Displays basic profile info and actions"
  []
  (let [editing? (rf/subscribe [:recruiter-admin/editing?])]
    (fn []
      [layout/column
       :padding 0
       :children [[basic-profile-info]
                  (if @editing?
                    [edit-profile-actions]
                    [profile-actions])]])))

(defn basic-admin-note-info
  "Displays updated by, updated on, and an internal admin note"
  []
  (let [admin-note-info-map (rf/subscribe [:recruiter-admin/admin-note-info-map])
        updated-by (rf/subscribe [:recruiter-admin/admin-note-updated-by])]
    (fn []
      [layout/column
       :padding 0
       :children [[form/inline-display
                   :label "Updated by"
                   :value @updated-by]
                  [form/inline-display
                   :label "Updated on"
                   :value (:insert-time @admin-note-info-map)]]])))

(defn admin-note-actions
  "Displays action to edit admin notes"
  []
  (let [active-admin-notes (rf/subscribe [:recruiter-admin/active-admin-note])]
    (fn []
      [layout/column
       :padding 0
       :children [[form/input-textarea
                   :rows 5
                   :disabled? true
                   :ns "recruiter-admin"
                   :type "active-admin-note"
                   :spec ::common/admin-notes]
                  [layout/row
                   :children [[layout/column
                               :padding 0
                               :class "col-xs-4"
                               :children [[link/hyperlink
                                           :label "Edit"
                                           :on-click #(rf/dispatch [:recruiter-admin/editing-admin-note-change @active-admin-notes])]]]]]]])))

(defn edit-admin-note-actions
  "Displays action to Save Changes or Cancel for updating admin notes"
  []
  (let [editing-notes (rf/subscribe [:recruiter-admin/editing-admin-note])]
    (fn []
      [layout/column
       :padding 0
       :children [[form/input-textarea
                   :rows 5
                   :disabled? false
                   :ns "recruiter-admin"
                   :type "editing-admin-note"
                   :spec ::common/admin-notes]
                  [layout/row
                   :align :center
                   :children [[layout/column
                               :padding 0
                               :class "col-xs-4"
                               :children [[link/hyperlink
                                           :label "Cancel"
                                           :on-click #(rf/dispatch [:recruiter-admin/editing-admin-note-change nil])]]]
                              [layout/column
                               :padding 0
                               :class "col-xs-8"
                               :children [[btn/secondary-dashboard-button
                                           :label "Save Changes"
                                           :on-click #(rf/dispatch [:recruiter-admin/save-admin-note @editing-notes])]]]]]]])))

(defn admin-note-info-box
  "Displays admin notes info and actions"
  []
  (let [editing? (rf/subscribe [:recruiter-admin/editing-admin-note?])]
    (fn []
      [layout/column
       :padding 0
       :children [[basic-admin-note-info]
                  (if @editing?
                    [edit-admin-note-actions]
                    [admin-note-actions])]])))

(defn features-view
  "Displays information about start/end date of paid membership"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding 13
               :children [[h/info-header "Features"]]]
              [form/checkbox
               :ns "recruiter-admin"
               :type "active-recruiter-superuser-checked?"
               :label "Super User"
               :disabled? true]]])

(defn paid-membership-view
  "Displays information about start/end date of paid membership"
  []
  (let [start-date (rf/subscribe [:recruiter-admin/active-recruiter-paid-membership-start-date])
        end-date (rf/subscribe [:recruiter-admin/active-recruiter-paid-membership-end-date])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding 13
                   :children [[h/info-header "Paid Membership"]]]
                  [layout/row
                   :padding 0
                   :children [[layout/col-left
                               :children [[form/inline-display
                                           :label "Start Date"
                                           :value (d/formatted-date :date (c/from-long @start-date))]]]
                              [layout/col-right
                               :children [[form/inline-display
                                           :label "End Date"
                                           :value (d/formatted-date :date (c/from-long @end-date))]]]]]]])))

(defn send-password-btn
  "Send current recruiter password email"
  []
  [btn/secondary-dashboard-button
   :label "Password Email"
   :on-click #(rf/dispatch [::modal/open-modal ::modal/forgot-password-email])])

(defn send-verification-btn
  "Send current recruiter verification email"
  []
  [btn/secondary-dashboard-button
   :label "Verification Email"
   :on-click #(rf/dispatch [::modal/open-modal ::modal/verification-email])])

(defn send-email-box
  "Displays send email actions"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding 6
               :children [[send-verification-btn]]]
              [layout/row-bottom
               :padding 6
               :children [[send-password-btn]]]]])

(defn full-profile-info
  "Displays full view of recruiter-profile"
  []
  (let [name (rf/subscribe [:recruiter-admin/active-recruiter-name])
        email (rf/subscribe [:recruiter-admin/active-recruiter-email])
        telephone (rf/subscribe [:recruiter-admin/active-recruiter-telephone])
        company-name (rf/subscribe [:recruiter-admin/active-recruiter-company-name])
        website-url (rf/subscribe [:recruiter-admin/active-recruiter-recruiter-website-url])
        title (rf/subscribe [:recruiter-admin/active-recruiter-title])
        job-function-name (rf/subscribe [:recruiter-admin/active-recruiter-job-function-name])
        role-name (rf/subscribe [:recruiter-admin/active-recruiter-role-name])
        street (rf/subscribe [:recruiter-admin/active-recruiter-street])
        city (rf/subscribe [:recruiter-admin/active-recruiter-city])
        state-province (rf/subscribe [:recruiter-admin/active-recruiter-state-province])
        postal-code (rf/subscribe [:recruiter-admin/active-recruiter-postal-code])
        country (rf/subscribe [:recruiter-admin/active-recruiter-country])]
    (fn []
      [layout/column
       :padding 0
       :children [[form/inline-display
                   :label "Name"
                   :value @name]
                  [form/inline-display
                   :label "Email"
                   :value @email]
                  [form/inline-display
                   :label "Phone"
                   :value @telephone]
                  [form/inline-display
                   :label "Company"
                   :value @company-name]
                  [form/inline-display
                   :label "Company Website"
                   :value @website-url]
                  [form/inline-display
                   :label "Title"
                   :value @title]
                  [form/inline-display
                   :label "Function"
                   :value @job-function-name]
                  [form/inline-display
                   :label "Role"
                   :value @role-name]
                  [form/inline-display
                   :label "Street Address"
                   :value @street]
                  [form/inline-display
                   :label "City"
                   :value @city]
                  [form/inline-display
                   :label "State"
                   :value @state-province]
                  [form/inline-display
                   :label "Postal Code"
                   :value @postal-code]
                  [form/inline-display
                   :label "Country"
                   :value @country]
                  [paid-membership-view]
                  [features-view]]])))

(defn paid-membership-edit
  "Displays edit fields for updating paid membership dates"
  []
  (let [start-date (rf/subscribe [:recruiter-admin/editing-recruiter-paid-membership-start-date])
        end-date (rf/subscribe [:recruiter-admin/editing-recruiter-paid-membership-end-date])]
    (fn []
      [layout/column
       :padding 0
       :children [[h/form-header "Paid Membership"]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-6"
                               :children [[form/datepicker-dropdown
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-paid-membership-start-date"
                                           :label "Start Date"
                                           :selectable-fn #(or (not @end-date) (< (d/unix-timestamp %) (d/unix-timestamp @end-date)))]]]
                              [layout/col-right
                               :class "col-xs-6"
                               :children [[form/datepicker-dropdown
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-paid-membership-end-date"
                                           :label "End Date"
                                           :selectable-fn #(or (not @start-date) (< (d/unix-timestamp @start-date) (d/unix-timestamp %)))]]]]]]])))

(defn features-edit
  "Displays edit fields for updating features (superuser and email subscriptions)"
  []
  [layout/col-left
   :class "col-xs-12"
   :children [[h/form-header "Features"]
              [form/checkbox
               :ns "recruiter-admin"
               :type "editing-recruiter-superuser-checked?"
               :label "Super User"]]])

(defn profile-edit
  "Displays profile edit view for recruiter"
  []
  (let [function (rf/subscribe [:recruiter-admin/editing-recruiter-job-function])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :children [[layout/col-left
                               :class "col-xs-6"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-firstname"
                                           :label "First Name"
                                           :spec ::specs/firstname
                                           :error-msg "Please enter a valid first name"]]]
                              [layout/col-right
                               :class "col-xs-6"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-lastname"
                                           :label "Last Name"
                                           :spec ::specs/lastname
                                           :error-msg "Please enter a valid last name"]]]]]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-8"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-email"
                                           :label "Email"
                                           :spec ::common/email
                                           :error-msg "Please enter a valid email address"]]]
                              [layout/col-right
                               :class "col-xs-4"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-telephone"
                                           :label "Phone"
                                           :spec ::specs/telephone
                                           :error-msg "Please enter a valid phone number"]]]]]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-8"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-recruiter-website-url"
                                           :label "Company Website"]]]]]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-6"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-title"
                                           :label "Title"]]]
                              [layout/col-right
                               :class "col-xs-6"
                               :children [[form/single-dropdown
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-job-function-id"
                                           :label "Function"
                                           :choices (dd/function)]]]]]
                  [layout/row
                   :children [[form/single-dropdown
                               :ns "recruiter-admin"
                               :type "editing-recruiter-role-id"
                               :label "Role"
                               :choices (dd/role @function)]]]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-6"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-street"
                                           :label "Street Address"]]]
                              [layout/col-right
                               :class "col-xs-6"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-city"
                                           :label "City"]]]]]
                  [layout/row
                   :children [[layout/col-left
                               :class "col-xs-8"
                               :children [[form/single-dropdown
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-state-province"
                                           :label "State"
                                           :choices (dd/states)]]]
                              [layout/col-right
                               :class "col-xs-4"
                               :children [[form/input-text
                                           :ns "recruiter-admin"
                                           :type "editing-recruiter-postal-code"
                                           :label "Postal Code"
                                           :spec ::specs/postal-code
                                           :error-msg "Please enter a valid zip code"]]]]]
                  [layout/row
                   :children [[form/single-dropdown
                               :ns "recruiter-admin"
                               :type "editing-recruiter-country"
                               :label "Country"
                               :choices (dd/countries)]]]
                  [paid-membership-edit]
                  [features-edit]]])))

(defn recruiter-profile-view
  "Displays a recruiter's profile with actions that can be taken"
  []
  (let [editing? (rf/subscribe [:recruiter-admin/editing?])
        breadcrumb-label (rf/subscribe [:recruiter-admin/breadcrumb-label])
        breadcrumb-on-click (rf/subscribe [:recruiter-admin/breadcrumb-on-click])
        email (rf/subscribe [:recruiter-admin/active-recruiter-email])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding-top 8
                   :padding-bottom 12
                   :children [[link/breadcrumb-hyperlink
                               :label @breadcrumb-label
                               :on-click @breadcrumb-on-click]]]
                  [layout/row
                   :padding 0
                   :align :start
                   :children [[layout/col-left
                               :class "col-xs-3"
                               :children [[layout/row-top
                                           :children [[b/box
                                                       :label "Recruiter Profile"
                                                       :body [basic-profile-info-box]]]]
                                          [layout/row-bottom
                                           :children [[b/box
                                                       :label "Send Emails"
                                                       :body [send-email-box]]]]]]
                              [layout/column
                               :class "col-xs-6"
                               :children [[b/box
                                           :label "Recruiter Details"
                                           :body [(if @editing?
                                                    profile-edit
                                                    full-profile-info)]]]]
                              [layout/col-right
                               :class "col-xs-3"
                               :children [[b/box
                                           :label "Internal Note"
                                           :body [admin-note-info-box]]]]]]
                  [modal/modal
                   :modal-key ::modal/verification-email
                   :title "Send Verify Email?"
                   :body [[type/modal-copy "Click below to send verify email"]]
                   :action {:label    "Send"
                            :on-click #(rf/dispatch [:recruiter-admin/send-verify-email])}]
                  [modal/modal
                   :modal-key ::modal/forgot-password-email
                   :title "Send Forgot Password Email?"
                   :body [[type/modal-copy "Click below to send forgot password email"]]
                   :action {:label    "Send"
                            :on-click #(rf/dispatch [:recruiter-admin/send-password-email @email])}]]])))

(defn recently-viewed-link
  "Renders link to view a recently viewed recruiter"
  [{:keys [firstname lastname recruiter-id]}]
  [layout/row
   :padding 6
   :children [[link/hyperlink
               :label (str firstname " " lastname)
               :on-click #(rf/dispatch [:recruiter-admin/recruiter-click recruiter-id])]]])

(defn recently-viewed-recruiters
  []
  (let [recruiters (rf/subscribe [:recruiter-admin/recently-viewed])]
    (fn []
      [layout/column
       :padding 0
       :children (conj
                   (mapv (partial vector recently-viewed-link) @recruiters)
                   (when (not-empty @recruiters)
                     [layout/row
                      :justify :center
                      :children [[btn/secondary-dashboard-button
                                  :label "Clear"
                                  :on-click #(rf/dispatch [:recruiter-admin/clear-recently-viewed])]]]))])))

(defn search-recruiters-form
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :children [[layout/col-left
                           :class "col-xs-4"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-recruiter-id"
                                       :label "Recruiter ID"]]]
                          [layout/col-right
                           :class "col-xs-8"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-email"
                                       :label "Email"]]]]]
              [layout/row
               :children [[layout/col-left
                           :class "col-xs-6"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-firstname"
                                       :label "First Name"]]]
                          [layout/col-right
                           :class "col-xs-6"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-lastname"
                                       :label "Last Name"]]]]]
              [layout/row-bottom
               :children [[layout/col-left
                           :class "col-xs-4"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-company-id"
                                       :label "Company ID"]]]
                          [layout/col-right
                           :class "col-xs-8"
                           :children [[form/input-text
                                       :ns "recruiter-admin"
                                       :type "search-company-name"
                                       :label "Company Name"]]]]]
              [layout/row
               :padding-top 24
               :padding-bottom 6
               :justify :center
               :children [[btn/primary-transaction-button
                           :label "Search"
                           :on-click #(rf/dispatch [:go-to-route "/recruiter-admin/search-results"])]]]]])

(defn queue-links
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding 6
               :children [[link/hyperlink
                           :label "Pending profiles"
                           :on-click #(rf/dispatch [:recruiter-admin/back-to-pending-click])]]]
              [layout/row
               :padding 6
               :children [[link/hyperlink
                           :label "Escalated profiles"
                           :on-click #(rf/dispatch [:recruiter-admin/back-to-pending-escalated-click])]]]
              [layout/row
               :padding 6
               :children [[link/hyperlink
                           :label "Recently approved profiles"
                           :on-click #(rf/dispatch [:recruiter-admin/back-to-approved-recently-click])]]]]])

(defn recruiter-teams-actions
  "Renders links to recruiter teams pages"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding 6
               :children [[link/hyperlink
                           :label "Create new team"
                           :on-click #(rf/dispatch [::modal/open-modal ::modal/create-team])]]]
              [layout/row
               :padding 6
               :children [[link/hyperlink-href
                           :label "View all teams"
                           :href "/#/teams"]]]]])

(defn search-page
  []
  [layout/column
   :padding 0
   :children [[layout/row-bottom
               :padding-top 30
               :justify :between
               :align :start
               :children [[layout/col-left
                           :class "col-xs-3"
                           :children [[layout/row-top
                                       :children [[b/box
                                                   :label "Recruiter Profiles"
                                                   :body [queue-links]]]]
                                      [layout/row-bottom
                                       :children [[b/box
                                                   :label "Recruiter Teams"
                                                   :body [recruiter-teams-actions]]]]]]
                          [layout/column
                           :class "col-xs-6"
                           :children [[b/box
                                       :label "Search Recruiters"
                                       :body [search-recruiters-form]]]]
                          [layout/col-right
                           :class "col-xs-3"
                           :children [[b/box
                                       :label "Recently Viewed"
                                       :body [recently-viewed-recruiters]]]]]]]])

(defn row-data
  "Returns a vector of row data for pending recruiters"
  [_ {:keys [firstname lastname company_name email telephone subscribe_date
             subscriber_id]}]
  [[link/table-cell-hyperlink
    :label (str firstname " " lastname)
    :on-click #(rf/dispatch [:recruiter-admin/recruiter-click subscriber_id])]
   company_name
   email
   telephone
   (d/formatted-date :date-and-time (d/subscribe-date-time subscribe_date))])

(defn- approval-label
  "return respective display label for "
  [creator-id]
  (case creator-id
    200 "Manual"
    210 "Auto"
    ""))

(defn recently-approved-row-data
  "Returns a vector of row data for pending recruiters"
  [_ {:keys [firstname lastname company_name email telephone
             subscriber_id creator_id approval_time]}]
  [[link/table-cell-hyperlink
    :label (str firstname " " lastname)
    :on-click #(rf/dispatch [:recruiter-admin/recruiter-click subscriber_id])]
   company_name
   email
   telephone
   (d/formatted-date :date-and-time (d/db-date-time approval_time))
   (approval-label creator_id)])

(def recruiter-table-headers
  [{:label "Name"
    :sort  {:name (su/sort-fn :lastname compare)}
    :width 16.8}
   {:label "Company"
    :sort  {:company (su/sort-fn :company_name compare)}
    :width 16.5}
   {:label "Email"
    :sort  {:email (su/sort-fn :email compare)}
    :width 32.3}
   {:label "Phone"
    :width 19.4}
   {:label "Date Created"
    :sort  {:date-created (su/sort-fn (comp d/subscribe-date-time :subscribe_date) su/before?)}
    :width 15}])

(def recently-approved-recruiters-table-header
  [{:label "Name"
    :sort  {:name (su/sort-fn :lastname compare)}
    :width 16.8}
   {:label "Company"
    :sort  {:company (su/sort-fn :company_name compare)}
    :width 15.5}
   {:label "Email"
    :sort  {:email (su/sort-fn :email compare)}
    :width 17.3}
   {:label "Phone"
    :width 10.4}
   {:label "Date Approved"
    :sort {:date-approved (su/sort-fn (comp d/db-date-time :approval_time) su/before?)}
    :width 13}
   {:label "Approval"
    :sort {:approval-mode (su/sort-fn (comp approval-label :creator_id) compare)}
    :width 13.5}])

(defn pending-queue
  "Renders table of pending recruiters"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding-top 8
               :padding-bottom 12
               :children [[link/breadcrumb-hyperlink
                           :label "< Back to Main"
                           :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])]]]
              [layout/row-top
               :padding-bottom 12
               :children [[h/header-3 "Pending Recruiters"]]]
              [table/frontend-pagination-table
               :table-key ::table/pending-recruiters
               :headers recruiter-table-headers
               :row-data-fn row-data
               :initial-sort-col :date-created
               :initial-sort-dir :desc
               :show-top-statistics? true
               :data-sub :recruiter-admin/pending-recruiters]]])

(defn pending-escalated-queue
  "Renders table of pending escalated recruiters"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding-top 8
               :padding-bottom 12
               :children [[link/breadcrumb-hyperlink
                           :label "< Back to Main"
                           :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])]]]
              [layout/row-top
               :padding-bottom 12
               :children [[h/header-3 "Pending Escalated Recruiters"]]]
              [table/frontend-pagination-table
               :table-key ::table/pending-escalated-recruiters
               :headers recruiter-table-headers
               :row-data-fn row-data
               :initial-sort-col :date-created
               :initial-sort-dir :desc
               :show-top-statistics? true
               :data-sub :recruiter-admin/pending-escalated-recruiters]]])

(defn recently-approved-queue
  "Renders table of recently approved recruiters"
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding-top 8
               :padding-bottom 12
               :children [[link/breadcrumb-hyperlink
                           :label "< Back to Main"
                           :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])]]]
              [layout/row-top
               :padding-bottom 12
               :children [[h/header-3 "Recently Approved Recruiters"]]]
              [table/frontend-pagination-table
               :table-key ::table/recently-approved-recruiters
               :headers recently-approved-recruiters-table-header
               :row-data-fn recently-approved-row-data
               :initial-sort-col :date-approved
               :initial-sort-dir :desc
               :show-top-statistics? true
               :data-sub :recruiter-admin/recently-approved-recruiters]]])

(defn search-results
  "Renders table of search results"
  []
  (let [search-params (rf/subscribe [:recruiter-admin/search-params])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding-top 8
                   :padding-bottom 12
                   :children [[link/breadcrumb-hyperlink
                               :label "< Back to Main"
                               :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])]]]
                  [layout/row-top
                   :children [[h/header-3 "Search Results"]]]
                  [table/backend-pagination-table
                   :table-key ::table/recruiter-search
                   :fetch-url (u/uri :recruiter-search)
                   :fetch-params search-params
                   :headers (map #(dissoc % :sort) recruiter-table-headers)
                   :show-top-statistics? true
                   :row-data-fn row-data]]])))

(defn body
  "Renders body of page"
  [body-content]
  (rf/dispatch [:recruiter-admin/load-recently-viewed])
  [layout/column
   :padding 0
   :class "content"
   :children [body-content]])

(defn header
  []
  [layout/column
   :padding 0
   :class "title-container"
   :children [[layout/column
               :padding 0
               :class "title-box"
               :children [[h/header-1 "Recruiter Admin"]]]]])

(defn- index
  [body-content]
  [sec/superuser-content
   [layout/column
    :padding 0
    :class "recruiter-admin main"
    :children [[header]
               [body body-content]
               [teams-modals/create-team-modal]]]])

(defn search-panel
  "Renders index with search page view"
  []
  [index [search-page]])

(defn search-results-panel
  "Renders index with search page view"
  []
  [index [search-results]])

(defn pending-queue-panel
  "Renders index with pending queue view"
  []
  (rf/dispatch [:recruiter-admin/fetch-pending-recruiters])
  [index [pending-queue]])

(defn pending-escalated-queue-panel
  "Renders index with pending escalated queue view"
  []
  (rf/dispatch [:recruiter-admin/fetch-pending-escalated-recruiters])
  [index [pending-escalated-queue]])

(defn recently-approved-recruiters-panel
  "Renders index with recently approved recruiters view."
  []
  (rf/dispatch [:recruiter-admin/fetch-recently-approved-recruiters])
  [index [recently-approved-queue]])

(defn recruiter-profile-panel
  "Renders index with profile view"
  []
  (let [active-recruiter-id (rf/subscribe [:recruiter-admin/active-recruiter-id])]
    (fn []
      (when @active-recruiter-id
        (rf/dispatch [:recruiter-admin/fetch-recruiter @active-recruiter-id]))
      [index [recruiter-profile-view]])))
