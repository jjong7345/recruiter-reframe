(ns recruit-app.superuser.events
  (:require [recruit-app.util.events :as ev]
            [re-frame.core :as rf]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [recruit-app.util.ajax :as a]
            [recruit-app.util.inventory :as i]
            [recruit-app.util.job :as ju]
            [clojure.set :refer [rename-keys]]
            [recruit-app.util.date :as d]
            [recruit-app.util.date :as dt]))

(ev/reg-events "superuser" ["active-tab" "impersonate-recruiter-id"
                            "show-redact-permissions?" "redact-start-date"
                            "redact-end-date" "inventory-action"
                            "inventory-type" "inventory-quantity"
                            "show-inventory?" "feature-job-start-date"
                            "feature-job-end-date" "repost-job-id"
                            "feature-job-id" "feature-job"])

(defn redact-permissions-recruiter-id-change
  "Toggles option to hide redact permissions and sets new value"
  [{:keys [db]} [_ value]]
  {:db       (assoc-in db [:superuser :redact-permissions-recruiter-id] value)
   :dispatch [:superuser/show-redact-permissions?-change false]})

(defn fetch-redact-permissions-success
  "Dispatches event to assoc permissions to db and toggle to show form"
  [_ [_ recruiter-id permissions]]
  {:dispatch-n [[:superuser/assoc-redact-permissions recruiter-id permissions]
                [:superuser/set-editing-permissions permissions]
                [:superuser/show-redact-permissions?-change true]]})

(defn set-editing-permissions
  "Dispatches change events for redact start date and end date"
  [{:keys [db]} [_ {:keys [unredacted-start-date unredacted-end-date]}]]
  {:dispatch-n [[:superuser/redact-start-date-change unredacted-start-date]
                [:superuser/redact-end-date-change unredacted-end-date]]})

(defn assoc-redact-permissions
  "Saves redact permissions to db"
  [db [_ recruiter-id permissions]]
  (if (empty? permissions)
    db
    (update-in
      db
      [:superuser :redact-permissions]
      assoc
      (js/parseInt recruiter-id)
      permissions)))

(defn fetch-redact-permissions
  "Dispatches http-xhrio request to get redact permissions for recruiter"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :superuser :redact-permissions-recruiter-id js/parseInt)]
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :redact-permissions recruiter-id)
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:superuser/fetch-redact-permissions-success recruiter-id]
                     :on-failure      [:superuser/show-redact-permissions?-change true]}}))

(defn save-redact-permissions-success
  "Saves response data in db and displays success alert"
  [_ [_ recruiter-id permissions]]
  {:dispatch-n [[:superuser/assoc-redact-permissions permissions]
                [:alerts/add-success "Redact Permissions Saved For Recruiter"]]})

(defn save-redact-permissions-failure
  "Dispatches event to display error message to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Save Redact Permissions For Recruiter"]})

(defn save-redact-permissions
  "Dispatches http-xhrio request to save redact permission dates"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :superuser :redact-permissions-recruiter-id js/parseInt)]
    {:dispatch   [:scroll-top]
     :ra-http-xhrio {:method          :put
                     :uri             (u/uri :redact-permissions recruiter-id)
                     :params          {:start-date (-> db :superuser :redact-start-date)
                                       :end-date   (-> db :superuser :redact-end-date)}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:superuser/save-redact-permissions-success recruiter-id]
                     :on-failure      [:superuser/save-redact-permissions-failure]}}))

(defn remove-redact-permissions-success
  "Dissocs permissions in db and displays success alert"
  [{:keys [db]} [_ recruiter-id]]
  {:db       (-> db
                 (update-in [:superuser :redact-permissions] dissoc recruiter-id)
                 (update :superuser dissoc :redact-start-date :redact-end-date))
   :dispatch [:alerts/add-success "Successfully Removed Redact Permissions For Recruiter"]})

(defn remove-redact-permissions-failure
  "Dispatches event to display error message to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Save Redact Permissions For Recruiter"]})

(defn remove-redact-permissions
  "Dispatches http-xhrio request to remove redact permissions"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :superuser :redact-permissions-recruiter-id js/parseInt)]
    {:dispatch   [:scroll-top]
     :ra-http-xhrio {:method          :delete
                     :uri             (u/uri :redact-permissions recruiter-id)
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:superuser/remove-redact-permissions-success recruiter-id]
                     :on-failure      [:superuser/remove-redact-permissions-failure]}}))

(defn assoc-inventory
  "Associates inventory with recruiter-id in db"
  [db [_ recruiter-id inventory]]
  (assoc-in db [:superuser :inventory recruiter-id] inventory))

(defn fetch-inventory-failure
  "Dispatches error alert"
  [_ _]
  {:dispatch-n [[:scroll-top]
                [:alerts/add-error "Failed To Load Inventory For User"]]})

(defn fetch-inventory-success
  "Saves inventory in db"
  [_ [_ recruiter-id inventory]]
  {:dispatch-n [[:superuser/assoc-inventory recruiter-id inventory]
                [:superuser/show-inventory?-change true]]})

(defn fetch-inventory
  "Dispatches http-xhrio request to fetch inventory for recruiter"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :superuser :inventory-recruiter-id js/parseInt)]
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :inventory recruiter-id)
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:superuser/fetch-inventory-success recruiter-id]
                     :on-failure      [:superuser/fetch-inventory-failure]}}))

(defn save-inventory-failure
  "Dispatches error alert"
  [_ _]
  {:dispatch-n [[:scroll-top]
                [:alerts/add-error "Failed To Save Inventory"]]})

(defn save-inventory-success
  "Saves inventory record in db and clears form"
  [_ [_ recruiter-id inventory]]
  {:dispatch-n [[:scroll-top]
                [:superuser/assoc-inventory recruiter-id inventory]
                [:alerts/add-success "Successfully Saved Inventory For Recruiter"]]})

(defn save-inventory
  "Makes http call to API to save inventory"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :superuser :inventory-recruiter-id js/parseInt)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :inventory recruiter-id)
                     :params          (i/save-request (:superuser db))
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:superuser/save-inventory-success recruiter-id]
                     :on-failure      [:superuser/save-inventory-failure]}}))

(defn inventory-recruiter-id-change
  "Toggles option to hide inventory and sets new value"
  [{:keys [db]} [_ value]]
  {:db       (assoc-in db [:superuser :inventory-recruiter-id] value)
   :dispatch [:superuser/show-inventory?-change false]})

(defn- superuser-promotion
  [{:keys [is_feature start_date end_date] :as promotion}]
  (when promotion
    {:feature-job-is-feature? is_feature
     :feature-job-start-date  (d/timestamp start_date)
     :feature-job-end-date    (d/timestamp end_date)}))

(defn set-feature-job-dates
  "Copies dates from current job promotion to superuser feature job dates"
  [db [_ job]]
  (if-let [new-promotion (->> (:featured job)
                           ju/current-promotion
                           superuser-promotion)]
    (update db :superuser merge new-promotion)
    (update db :superuser dissoc :feature-job-is-feature? :feature-job-start-date :feature-job-end-date)))

(defn load-job-success
  "Sets job to db and sets dates"
  [_ [_ job]]
  {:dispatch-n [[:superuser/feature-job-change job]
                [:superuser/set-feature-job-dates job]]})

(defn load-job
  "Loads job for use with feature job tab"
  [{:keys [db]} [_ _]]
  {:ra-http-xhrio {:method          :get
                :uri             (u/uri :superuser-job (-> db :superuser :feature-job-id js/parseInt))
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:superuser/load-job-success]
                :on-failure      [:http-no-on-failure]}})

(defn impersonate-success
  "Dispatch events once impersonate is successful"
  [{:keys [db]} [_ resp]]
  {:reload true})

(defn impersonate-user
  "Attempt to login user as another recruiter"
  [{:keys [db]} _]
  (let [recruiter-id (long (get-in db [:superuser :impersonate-recruiter-id]))]
    {:ra-http-xhrio {:method          :post
                     :uri             "/impersonate"
                     :params          {:recruiter-id recruiter-id}
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:superuser/impersonate-success]
                     :on-failure      [:superuser/impersonate-success]}}))

(defn save-job-promotion-failure
  "Dispatches error alert to user"
  [_ _]
  {:dispatch-n [[:alerts/add-error "Failed To Save Job Promotion"]
                [:scroll-top]]})

(defn save-job-promotion-success
  "Dispatches success alert to user"
  [_ [_ job-id]]
  {:dispatch-n [[:superuser/load-job job-id]
                [:alerts/add-success "Successfully Saved Job Promotion"]
                [:scroll-top]]})

(defn save-job-promotion-request
  "Returns job promotion request from superuser map from db"
  [{:keys [feature-job-id feature-job-start-date feature-job-end-date]}]
  {:start_date (when feature-job-start-date (.getTime feature-job-start-date))
   :end_date   (when feature-job-end-date (.getTime feature-job-end-date))
   :job_id     feature-job-id})

(defn save-job-promotion
  "Dispatches http-xhrio request to save job promotion"
  [{:keys [db]} _]
  (let [job-id (-> db :superuser :feature-job-id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :save-job-promotion job-id)
                     :params          (save-job-promotion-request (:superuser db))
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:superuser/save-job-promotion-success job-id]
                     :on-failure      [:superuser/save-job-promotion-failure]}}))

(defn cancel-job-promotion
  "Dispatches http-xhrio request to cancel job promotion"
  [{:keys [db]} _]
  (let [job-id (-> db :superuser :feature-job-id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :cancel-job-promotion job-id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:superuser/cancel-job-promotion-success job-id]
                     :on-failure      [:superuser/cancel-job-promotion-failure]}}))

(defn cancel-job-promotion-success
  "Fetches job again and posts success message"
  [_ [_ job-id]]
  {:dispatch-n [[:superuser/load-job job-id]
                [:alerts/add-success "Successfully Cancelled Job Promotion"]
                [:scroll-top]]})

(defn cancel-job-promotion-failure
  "Fetches job again and posts success message"
  [_ _]
  {:dispatch-n [[:alerts/add-error "Failed To Cancel Promotion. Please Try Again."]
                [:scroll-top]]})

(defn repost-job-failure
  "Displays alert that repost failed"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Repost Job. Please Try Again."]})

(defn repost-job-success
  "Handles response from reposting job"
  [{:keys [db]} _]
  {:dispatch [:alerts/add-success "Job reposted."]
   :db       (assoc-in db [:superuser :repost-job-id] "")})

(defn repost-job
  "Calls to api to repost job"
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :repost-job (-> db :superuser :repost-job-id js/parseInt))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:superuser/repost-job-success]
                   :on-failure      [:superuser/repost-job-failure]}})

(rf/reg-event-fx
  :superuser/impersonate-success
  impersonate-success)

(rf/reg-event-fx
  :superuser/impersonate-user
  impersonate-user)

(rf/reg-event-fx
  :superuser/redact-permissions-recruiter-id-change
  redact-permissions-recruiter-id-change)

(rf/reg-event-fx
  :superuser/fetch-redact-permissions
  fetch-redact-permissions)

(rf/reg-event-fx
  :superuser/fetch-redact-permissions-success
  fetch-redact-permissions-success)

(rf/reg-event-fx
  :superuser/set-editing-permissions
  set-editing-permissions)

(rf/reg-event-db
  :superuser/assoc-redact-permissions
  assoc-redact-permissions)

(rf/reg-event-fx
  :superuser/save-redact-permissions
  save-redact-permissions)

(rf/reg-event-fx
  :superuser/save-redact-permissions-success
  save-redact-permissions-success)

(rf/reg-event-fx
  :superuser/save-redact-permissions-failure
  save-redact-permissions-failure)

(rf/reg-event-fx
  :superuser/remove-redact-permissions
  remove-redact-permissions)

(rf/reg-event-fx
  :superuser/remove-redact-permissions-success
  remove-redact-permissions-success)

(rf/reg-event-fx
  :superuser/remove-redact-permissions-failure
  remove-redact-permissions-failure)

(rf/reg-event-fx
  :superuser/save-inventory
  save-inventory)

(rf/reg-event-fx
  :superuser/save-inventory-failure
  save-inventory-failure)

(rf/reg-event-fx
  :superuser/save-inventory-success
  save-inventory-success)

(rf/reg-event-fx
  :superuser/fetch-inventory
  fetch-inventory)

(rf/reg-event-fx
  :superuser/fetch-inventory-failure
  fetch-inventory-failure)

(rf/reg-event-fx
  :superuser/fetch-inventory-success
  fetch-inventory-success)

(rf/reg-event-fx
  :superuser/inventory-recruiter-id-change
  inventory-recruiter-id-change)

(rf/reg-event-db
  :superuser/assoc-inventory
  assoc-inventory)

(rf/reg-event-fx
  :superuser/load-job-success
  load-job-success)

(rf/reg-event-fx
  :superuser/load-job
  load-job)

(rf/reg-event-db
  :superuser/set-feature-job-dates
  set-feature-job-dates)

(rf/reg-event-fx
  :superuser/save-job-promotion-failure
  save-job-promotion-failure)

(rf/reg-event-fx
  :superuser/save-job-promotion-success
  save-job-promotion-success)

(rf/reg-event-fx
  :superuser/save-job-promotion
  save-job-promotion)

(rf/reg-event-fx
  :superuser/cancel-job-promotion
  cancel-job-promotion)

(rf/reg-event-fx
  :superuser/cancel-job-promotion-success
  cancel-job-promotion-success)

(rf/reg-event-fx
  :superuser/cancel-job-promotion-failure
  cancel-job-promotion-failure)

(rf/reg-event-fx
  :superuser/repost-job
  repost-job)

(rf/reg-event-fx
  :superuser/repost-job-success
  repost-job-success)

(rf/reg-event-fx
  :superuser/repost-job-failure
  repost-job-failure)


