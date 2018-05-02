(ns recruit-app.recruiter-admin.events
  (:require [recruit-app.util.events :as ev]
            [re-frame.core :as rf]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [recruit-app.util.ajax :as a]
            [recruit-app.util.date :as d]
            [clojure.string :as string]
            [clojure.set :refer [rename-keys]]
            [recruit-app.util.recruiter :as ru]
            [recruit-app.components.table :as table]
            [recruit-app.components.modal :as modal]
            [recruit-app.util.breadcrumb :as b]
            [cljs.reader :as edn]))

(ev/reg-events "recruiter-admin" ["pending-recruiters-map" "editing-recruiter"
                                  "pending-escalated-recruiters-map"
                                  "recently-approved-recruiters-map"
                                  "active-recruiter-id" "show-errors?"
                                  "pending-recruiters-sort-col"
                                  "pending-escalated-recruiters-sort-col"
                                  "pending-recruiters-sort-dir"
                                  "pending-escalated-recruiters-sort-dir"
                                  "admin-note-info-map"
                                  "editing-admin-note"
                                  "active-admin-note"
                                  "breadcrumb"])
(ev/reg-events
  "recruiter-admin"
  "editing-recruiter"
  ["firstname" "lastname" "email" "telephone" "title"
   "street" "city" "state-province" "postal-code"
   "country" "recruiter-website-url" "superuser?"])
(ev/reg-events
  "recruiter-admin"
  "search"
  ["firstname" "lastname" "email" "recruiter-id" "company-id" "company-name"])

(defn clear-db
  "Dissociates recruiter-admin from db"
  [{:keys [db]} _]
  {:db       (dissoc db :recruiter-admin)
   :dispatch [(table/reset-event ::table/recruiter-search)]})

(defn fetch-pending-recruiters-success
  "Associates recruiters to db"
  [_ [_ recruiters]]
  {:dispatch [:recruiter-admin/pending-recruiters-map-change (zipmap (map :subscriber_id recruiters) recruiters)]})

(defn fetch-pending-escalated-recruiters-success
  "Associates pending escalated recruiters to db"
  [_ [_ recruiters]]
  {:dispatch [:recruiter-admin/pending-escalated-recruiters-map-change (zipmap (map :subscriber_id recruiters) recruiters)]})

(defn fetch-pending-recruiters
  "Calls API to get all pending recruiters"
  [_ _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :pending-recruiters)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/fetch-pending-recruiters-success]
                   :on-failure      [:http-no-on-failure]}})

(defn- underscore->lisp
  "Converts key from underscore to lisp"
  [k]
  (keyword (string/replace (name k) #"_" "-")))

(defn- with-lisp-case-keys
  "Recruiter profile keys are underscore cased, this converts to lisp-case"
  [recruiter]
  (let [underscore-keys (keys recruiter)]
    (rename-keys
      recruiter
      (zipmap
        underscore-keys
        (map underscore->lisp underscore-keys)))))

(defn fetch-pending-escalated-recruiters
  "Calls API to get all pending escalated recruiters"
  [_ _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :pending-escalated-recruiters)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/fetch-pending-escalated-recruiters-success]
                   :on-failure      [:http-no-on-failure]}})

(defn fetch-recently-approved-recruiters-success
  "Associates recently fetched recruiters to db"
  [_ [_ recruiters]]
  {:dispatch [:recruiter-admin/recently-approved-recruiters-map-change
              (zipmap (map :subscriber_id recruiters) recruiters)]})

(defn fetch-recently-approved-recruiters
  "API call to retrieve recruiters approved in the past week."
  [_ _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :recently-approved-recruiters)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/fetch-recently-approved-recruiters-success]
                   :on-failure      [:http-no-on-failure]}})

(defn fetch-recruiter-success
  "Dispatches event to set recruiter as active-recruiter"
  [_ [_ response]]
  (let [recruiter (with-lisp-case-keys response)]
    {:dispatch-n [[:recruiter-admin/assoc-recruiter recruiter]
                  [:recruiter-admin/add-to-recently-viewed recruiter]
                  [:recruiter-admin/fetch-admin-note (:recruiter-id recruiter)]]}))

(defn fetch-recruiter-failure
  "Displays alert to user of error"
  [_ _]
  {:dispatch [:alerts/add-error "Error occurred while retrieving recruiter profile"]})

(defn fetch-recruiter
  "Fetches recruiter profile by ID"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :fetch-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/fetch-recruiter-success]
                   :on-failure      [:recruiter-admin/fetch-recruiter-failure]}})

(defn clear-active-recruiter
  "Dissocs active recruiter info from db"
  [db _]
  (update db :recruiter-admin dissoc :active-recruiter :active-recruiter-id))

(defn approve-recruiter
  "Makes request to API to approve recruiter"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :put
                   :uri             (u/uri :approve-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/approve-recruiter-success recruiter-id]
                   :on-failure      [:recruiter-admin/approve-recruiter-failure]}})

(defn approve-recruiter-success
  "Displays alert and fetches recruiter information"
  [_ [_ recruiter-id response]]
  {:dispatch-n [[:alerts/add-success "Recruiter has successfully been approved."]
                [:recruiter-admin/fetch-recruiter recruiter-id]
                [:recruiter-admin/newly-approved-recruiter recruiter-id]]})

(defn approve-recruiter-failure
  "Displays error alert to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Approve Recruiter. Please Try Again."]})

(defn escalate-recruiter
  "Makes request to API to escalate recruiter"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :put
                   :uri             (u/uri :escalate-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/escalate-recruiter-success recruiter-id]
                   :on-failure      [:recruiter-admin/escalate-recruiter-failure]}})

(defn escalate-recruiter-success
  "Displays alert and fetches recruiter information"
  [_ [_ recruiter-id response]]
  {:dispatch-n [[:alerts/add-success "Recruiter has successfully been escalated."]
                [:recruiter-admin/fetch-recruiter recruiter-id]]})

(defn escalate-recruiter-failure
  "Displays error alert to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Escalate Recruiter. Please Try Again."]})

(defn decline-recruiter
  "Makes request to API to decline recruiter"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :put
                   :uri             (u/uri :decline-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/decline-recruiter-success recruiter-id]
                   :on-failure      [:recruiter-admin/decline-recruiter-failure]}})

(defn decline-recruiter-success
  "Displays alert and fetches recruiter information"
  [_ [_ recruiter-id response]]
  {:dispatch-n [[:alerts/add-success "Recruiter has successfully been declined."]
                [:recruiter-admin/fetch-recruiter recruiter-id]]})

(defn decline-recruiter-failure
  "Displays error alert to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Decline Recruiter. Please Try Again."]})

(defn permanently-decline-recruiter
  "Makes request to API to permanently-decline recruiter"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :put
                   :uri             (u/uri :permanently-decline-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/permanently-decline-recruiter-success recruiter-id]
                   :on-failure      [:recruiter-admin/permanently-decline-recruiter-failure]}})

(defn permanently-decline-recruiter-success
  "Displays alert and fetches recruiter information"
  [_ [_ recruiter-id response]]
  {:dispatch-n [[:alerts/add-success "Recruiter has successfully been permanently declined."]
                [:recruiter-admin/fetch-recruiter recruiter-id]]})

(defn permanently-decline-recruiter-failure
  "Displays error alert to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Approve Recruiter. Please Try Again."]})

(defn send-verify-success
  "Handle re-send email success response"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/verification-email]
                [:alerts/add-success "Successfully Sent Email To User!"]]})

(defn send-password-success
  "Handle re-send email success response"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/forgot-password-email]
                [:alerts/add-success "Successfully Sent Email To User!"]]})

(defn send-email-failure
  "Handle email failure response"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/forgot-password-email]
                [::modal/close-modal ::modal/verification-email]
                [:alerts/add-error "Failed To Send Email. Please Try Again."]]})

(defn send-verify-email
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :ra-send-verify (-> db :recruiter-admin :active-recruiter-id))
                   :params          {}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/send-verify-success]
                   :on-failure      [:recruiter-admin/send-email-failure]}})

(defn send-password-email
  [{:keys [db]} [_ email]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :forgot-password)
                   :params          {:email email}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/send-password-success]
                   :on-failure      [:recruiter-admin/send-email-failure]}})

(defn editing-recruiter-role-id-change
  "Associates role map to active recruiter map"
  [db [_ role-id]]
  (assoc-in db [:recruiter-admin :editing-recruiter :recruiter-guest-role :recruiter_guest_role_id] role-id))

(defn editing-recruiter-job-function-id-change
  "Associates role id to active recruiter map"
  [db [_ role-id]]
  (assoc-in db [:recruiter-admin :editing-recruiter :job-function :id] role-id))

(defn editing-recruiter-paid-membership-start-date-change
  "Formats date and associates to paid membership map"
  [db [_ start-date]]
  (assoc-in
    db
    [:recruiter-admin :editing-recruiter :paid-membership :unredacted-start-date]
    (d/unix-timestamp start-date)))

(defn editing-recruiter-paid-membership-end-date-change
  "Formats date and associates to paid membership map"
  [db [_ end-date]]
  (assoc-in
    db
    [:recruiter-admin :editing-recruiter :paid-membership :unredacted-end-date]
    (d/unix-timestamp end-date)))

(defn assoc-recruiter
  "Associates recruiter to recruiters map"
  [db [_ {:keys [recruiter-id] :as recruiter}]]
  (assoc-in db [:recruiter-admin :recruiters recruiter-id] recruiter))

(defn back-to-pending-click
  "Clears active/editing recruiters and goes to pending"
  [{:keys [db]} _]
  {:route    "/recruiter-admin/pending"
   :dispatch [:recruiter-admin/clear-recruiter-profile-data]})

(defn back-to-pending-escalated-click
  "Clears active/editing recruiters and goes to pending escalated"
  [{:keys [db]} _]
  {:route    "/recruiter-admin/pending-escalated"
   :dispatch [:recruiter-admin/clear-recruiter-profile-data]})

(defn back-to-recently-approved-click
  "Clears active/editing recruiters and goes to pending recently approved"
  [_ _]
  {:route    "/recruiter-admin/approved-recently"
   :dispatch [:recruiter-admin/clear-recruiter-profile-data]})

(defn clear-recruiter-profile-data
  "Clears db of data needed for active recruiter profile"
  [db _]
  (update
    db
    :recruiter-admin
    dissoc
    :active-recruiter
    :active-recruiter-id
    :editing-recruiter))

(defn save-recruiter
  "Makes API call to save recruiter changes"
  [_ [_ {:keys [recruiter-id] :as recruiter}]]
  {:dispatch      [:recruiter-admin/show-errors?-change false]
   :ra-http-xhrio {:method          :put
                   :uri             (u/uri :update-recruiter recruiter-id)
                   :params          (select-keys recruiter ru/editable-keys)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/save-recruiter-success recruiter-id]
                   :on-failure      [:recruiter-admin/save-recruiter-failure]}})

(defn fetch-admin-note-success
  "Dispatches event to set admin notes from backend call into db"
  [_ [_ admin-note-info-map]]
  {:dispatch-n [[:recruiter-admin/admin-note-info-map-change admin-note-info-map]
                [:recruiter-admin/active-admin-note-change (:notes admin-note-info-map)]]})

(defn fetch-admin-note
  "Makes API call to retrieve admin note for a recruiter"
  [_ [_ active-recruiter-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :fetch-admin-note active-recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:recruiter-admin/fetch-admin-note-success]
                   :on-failure      [:http-no-on-failure]}})

(defn save-admin-note
  "Makes API call to update admin note changes"
  [{:keys [db]} [_ admin-note]]
  (let [adm-note-id (-> db :recruiter-admin :admin-note-info-map :adm-note-id)
        recruiter-id (-> db :recruiter-admin :active-recruiter-id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (if adm-note-id (u/uri :update-admin-note)
                                                      (u/uri :create-admin-note))
                     :params          {:notes        admin-note
                                       :recruiter-id recruiter-id}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:recruiter-admin/save-admin-note-success recruiter-id]
                     :on-failure      [:recruiter-admin/save-admin-note-failure]}}))

(defn save-recruiter-success
  "Removes editing recruiter from db and displays success"
  [_ [_ recruiter-id]]
  {:dispatch-n [[:recruiter-admin/fetch-recruiter recruiter-id]
                [:recruiter-admin/editing-recruiter-change nil]
                [:alerts/add-success "Successfully updated recruiter!"]]})

(defn save-recruiter-failure
  "Displays error message to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed to update recruiter, please try again."]})

(defn save-admin-note-success
  "Removes editing recruiter from db and displays success"
  [_ [_ recruiter-id]]
  {:dispatch-n [[:recruiter-admin/fetch-admin-note recruiter-id]
                [:recruiter-admin/editing-admin-note-change nil]
                [:alerts/add-success "Successfully updated admin notes!"]]})

(defn save-admin-note-failure
  "Displays error message to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed to update admin notes, please try again."]})

(defn editing-recruiter-superuser-checked?-change
  "Dispatches change event for superuser? flag"
  [_ [_ val]]
  {:dispatch [:recruiter-admin/editing-recruiter-superuser?-change val]})

(defn sort-pending-recruiters
  "Sets sort-col and sort-dir for pending recruiters"
  [_ [_ sort-col sort-dir]]
  {:dispatch-n [[:recruiter-admin/pending-recruiters-sort-col-change sort-col]
                [:recruiter-admin/pending-recruiters-sort-dir-change sort-dir]]})

(defn recruiter-click
  "Sets breadcrumb and routes to recruiter page"
  [{:keys [db]} [_ recruiter-id]]
  {:route    (str "/recruiter-admin/" recruiter-id)
   :dispatch [:recruiter-admin/breadcrumb-change (get b/breadcrumb (:active-panel db))]})

(defn sort-pending-escalated-recruiters
  "Sets sort-col and sort-dir for pending recruiters"
  [_ [_ sort-col sort-dir]]
  {:dispatch-n [[:recruiter-admin/pending-escalated-recruiters-sort-col-change sort-col]
                [:recruiter-admin/pending-escalated-recruiters-sort-dir-change sort-dir]]})

(defn load-recently-viewed
  "Loads recently viewed recruiters from local storage"
  [_ _]
  {:lls ["recently-viewed" [:recruiter-admin :recently-viewed]]})

(defn add-to-recently-viewed
  "Adds given recruiter info to recently viewed recruiters in local storage"
  [{:keys [db]} [_ recruiter]]
  (let [recently-viewed (-> (get-in db [:recruiter-admin :recently-viewed])
                            (conj recruiter)
                            set
                            vec)]
    {:db (assoc-in db [:recruiter-admin :recently-viewed] recently-viewed)
     :ls ["recently-viewed" recently-viewed]}))

(defn newly-approved-recruiter
  "Sends recruiter info to Hubspot for a newly approved recruiter"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :newly-approved-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:http-no-on-success]
                   :on-failure      [:recruiter-admin/newly-approved-recruiter-failure recruiter-id]}})

(defn newly-approved-recruiter-failure
  "Retries the sales lead call if it initially failed"
  [_ [_ recruiter-id]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :newly-approved-recruiter recruiter-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:http-no-on-success]
                   :on-failure      [:http-no-on-failure]}})

(defn clear-recently-viewed
  "Clears recently-viewed local storage"
  [{:keys [db]} _]
  {:db (update db :recruiter-admin dissoc :recently-viewed)
   :ls ["recently-viewed" nil]})

(rf/reg-event-fx
  :recruiter-admin/clear-db
  clear-db)

(rf/reg-event-fx
  :recruiter-admin/fetch-pending-recruiters
  fetch-pending-recruiters)

(rf/reg-event-fx
  :recruiter-admin/fetch-pending-escalated-recruiters
  fetch-pending-escalated-recruiters)

(rf/reg-event-fx
  :recruiter-admin/fetch-recently-approved-recruiters
  fetch-recently-approved-recruiters)

(rf/reg-event-fx
  :recruiter-admin/fetch-pending-recruiters-success
  fetch-pending-recruiters-success)

(rf/reg-event-fx
  :recruiter-admin/fetch-pending-escalated-recruiters-success
  fetch-pending-escalated-recruiters-success)

(rf/reg-event-fx
  :recruiter-admin/fetch-recently-approved-recruiters-success
  fetch-recently-approved-recruiters-success)

(rf/reg-event-fx
  :recruiter-admin/fetch-recruiter
  fetch-recruiter)

(rf/reg-event-fx
  :recruiter-admin/fetch-recruiter-success
  fetch-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/fetch-recruiter-failure
  fetch-recruiter-failure)

(rf/reg-event-db
  :recruiter-admin/clear-active-recruiter
  clear-active-recruiter)

(rf/reg-event-fx
  :recruiter-admin/approve-recruiter
  approve-recruiter)

(rf/reg-event-fx
  :recruiter-admin/approve-recruiter-success
  approve-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/approve-recruiter-failure
  approve-recruiter-failure)

(rf/reg-event-fx
  :recruiter-admin/escalate-recruiter
  escalate-recruiter)

(rf/reg-event-fx
  :recruiter-admin/escalate-recruiter-success
  escalate-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/escalate-recruiter-failure
  escalate-recruiter-failure)

(rf/reg-event-fx
  :recruiter-admin/decline-recruiter
  decline-recruiter)

(rf/reg-event-fx
  :recruiter-admin/decline-recruiter-success
  decline-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/decline-recruiter-failure
  decline-recruiter-failure)

(rf/reg-event-fx
  :recruiter-admin/permanently-decline-recruiter
  permanently-decline-recruiter)

(rf/reg-event-fx
  :recruiter-admin/permanently-decline-recruiter-success
  permanently-decline-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/permanently-decline-recruiter-failure
  permanently-decline-recruiter-failure)

(rf/reg-event-fx
  :recruiter-admin/send-verify-email
  send-verify-email)

(rf/reg-event-fx
  :recruiter-admin/send-password-email
  send-password-email)

(rf/reg-event-fx
  :recruiter-admin/send-verify-success
  send-verify-success)

(rf/reg-event-fx
  :recruiter-admin/send-password-success
  send-password-success)

(rf/reg-event-fx
  :recruiter-admin/send-email-failure
  send-email-failure)

(rf/reg-event-db
  :recruiter-admin/editing-recruiter-role-id-change
  editing-recruiter-role-id-change)

(rf/reg-event-db
  :recruiter-admin/editing-recruiter-job-function-id-change
  editing-recruiter-job-function-id-change)

(rf/reg-event-db
  :recruiter-admin/editing-recruiter-paid-membership-start-date-change
  editing-recruiter-paid-membership-start-date-change)

(rf/reg-event-db
  :recruiter-admin/editing-recruiter-paid-membership-end-date-change
  editing-recruiter-paid-membership-end-date-change)

(rf/reg-event-db
  :recruiter-admin/assoc-recruiter
  assoc-recruiter)

(rf/reg-event-fx
  :recruiter-admin/back-to-pending-click
  back-to-pending-click)

(rf/reg-event-fx
  :recruiter-admin/back-to-pending-escalated-click
  back-to-pending-escalated-click)

(rf/reg-event-fx
  :recruiter-admin/back-to-approved-recently-click
  back-to-recently-approved-click)

(rf/reg-event-db
  :recruiter-admin/clear-recruiter-profile-data
  clear-recruiter-profile-data)

(rf/reg-event-fx
  :recruiter-admin/save-recruiter
  save-recruiter)

(rf/reg-event-fx
  :recruiter-admin/save-recruiter-success
  save-recruiter-success)

(rf/reg-event-fx
  :recruiter-admin/save-recruiter-failure
  save-recruiter-failure)

(rf/reg-event-fx
  :recruiter-admin/editing-recruiter-superuser-checked?-change
  editing-recruiter-superuser-checked?-change)

(rf/reg-event-fx
  :recruiter-admin/sort-pending-recruiters
  sort-pending-recruiters)

(rf/reg-event-fx
  :recruiter-admin/recruiter-click
  recruiter-click)

(rf/reg-event-fx
  :recruiter-admin/sort-pending-escalated-recruiters
  sort-pending-escalated-recruiters)

(rf/reg-event-fx
  :recruiter-admin/fetch-admin-note
  fetch-admin-note)

(rf/reg-event-fx
  :recruiter-admin/fetch-admin-note-success
  fetch-admin-note-success)

(rf/reg-event-fx
  :recruiter-admin/save-admin-note
  save-admin-note)

(rf/reg-event-fx
  :recruiter-admin/save-admin-note-success
  save-admin-note-success)

(rf/reg-event-fx
  :recruiter-admin/save-admin-note-failure
  save-admin-note-failure)

(rf/reg-event-fx
  :recruiter-admin/load-recently-viewed
  load-recently-viewed)

(rf/reg-event-fx
  :recruiter-admin/add-to-recently-viewed
  add-to-recently-viewed)

(rf/reg-event-fx
  :recruiter-admin/clear-recently-viewed
  clear-recently-viewed)

(rf/reg-event-fx
  :recruiter-admin/newly-approved-recruiter
  newly-approved-recruiter)

(rf/reg-event-fx
  :recruiter-admin/newly-approved-recruiter-failure
  newly-approved-recruiter-failure)
