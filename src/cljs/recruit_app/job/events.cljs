(ns recruit_app.job.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.events :as ev]
            [clojure.set :refer [union difference]]
            [recruit-app.util.job :as ju]
            [clojure.walk :refer [keywordize-keys]]
            [cognitect.transit :as t]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [recruit-app.components.table :as table]))

(defn set-active-tab
  "Sets active tab in job view"
  [db [_ tab]]
  (assoc-in db [:job :active-tab] tab))

(defn set-active-job
  "Sets active job id"
  [db [_ job-id]]
  (assoc-in db [:job :active-job-id] job-id))

(defn active-job-loc
  "Sets active job location id"
  [db [_ job-loc-id]]
  (assoc-in db [:job :active-job-loc-id] job-loc-id))

(defn change-active-job
  "Sets active job id and updates route"
  [{:keys [db]} [_ job-id]]
  {:dispatch [:job/set-active-job job-id]
   :route    (str "/job/" job-id)})

(defn set-type
  "Sets preview-job type (:post if in post-job view, :active if viewing a job)"
  [db [_ type]]
  (assoc-in db [:preview-job :type] type))

(defn add-fetched-job
  "Adds job id to fetched so it will not be fetched again"
  [db [_ job-id]]
  (update-in db [:job :fetched] ju/add-to-set job-id))

(defn process-response
  "Process response from fetching a job"
  [{:keys [db]} [_ job-id response]]
  (let [job (r/job response)]
    (if job
      {:db         (update-in db [:jobs :jobs] assoc job-id job)
       :dispatch-n [[:job/add-fetched-job job-id]
                    [:job/get-a-pending-promoted job]]}
      {:dispatch [:job/add-fetched-job job-id]})))

(defn process-a-pending-promoted
  "Processes a fetched promotion data, if valid, triggers db update"
  [{:keys [db]} [_ job-id response]]
  (let [promotion-data (-> response str edn/read-string)]
    {:dispatch-n (cond-> []
                         promotion-data (conj [:job/update-a-pending-promoted job-id promotion-data])
                         (-> db :post-job :editing?) (conj [:post-job/populate-form (-> db :post-job :job-id)]))}))

(defn update-a-pending-promoted
  "Merges promotion data of a pending job to the db"
  [db [_ job-id promotion-data]]
  (assoc-in db [:jobs :jobs job-id :pending-promoted] promotion-data))

(defn get-job
  "Calls API to fetch job if not already fetched"
  [{:keys [db]} [_ job-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :get-job job-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:job/process-response job-id]
                   :on-failure      [:http-no-on-failure]}})

(defn get-a-pending-promoted
  "API call to retrieve promotion info for a pending job"
  [_ [_ job]]
  (let [job-id (:job_id job)
        job-status (:job_status_id job)]
    (if (= "P" job-status)
      {:ra-http-xhrio {:method          :post
                       :uri             (u/uri :pending-promoted-job)
                       :params          {:job-id job-id}
                       :timeout         5000
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:job/process-a-pending-promoted job-id]
                       :on-failure      [:job/process-a-pending-promoted job-id nil]}}
      {:dispatch      [:job/process-a-pending-promoted job-id nil]})))

(defn set-jobseeker-tab
  "Dispatches events specific to jobseeker tabs in job view (clearing checked candidates and setting job location)"
  [_ [_ job-loc-id]]
  {:dispatch-n [[:job/active-job-loc job-loc-id]
                [(table/clear-checked-event ::table/job-applicants)]
                [(table/clear-checked-event ::table/job-viewers)]]})

(defn load-view
  "Dispatches events when switching to job page"
  [{:keys [db]} _]
  (let [job-id (-> db :job :active-job-id)]
    {:dispatch-n [[:preview-job/set-type :active]
                  [:job/get-job job-id]
                  [:job/set-active-job job-id]
                  [:jobs/get-jobs]
                  [:scroll-top]]
     :ga/page-view ["/job" {}]}))

(defn prev-for-job
  "Dispatches events for previewing job"
  [{:keys [db]} [_ job-id]]
  {:route      (str "/job/" job-id)
   :dispatch-n [[:job/load-view]
                [:job/set-active-tab :tab0]]})

(defn apps-for-job
  "Dispatches events for viewing applicants for job"
  [_ [_ job-id job-loc-id]]
  {:route      (str "/job/" job-id "/apps")
   :dispatch-n [[:job/set-active-tab :tab1]
                [:job/set-jobseeker-tab job-loc-id]]})

(defn views-for-job
  "Dispatches events for viewing viewers of job"
  [_ [_ job-id job-loc-id]]
  {:route      (str "/job/" job-id "/views")
   :dispatch-n [[:job/set-active-tab :tab2]
                [:job/set-jobseeker-tab job-loc-id]]})

(defn dissoc-active-job
  "Removes active-job-id and active-job-loc-id from db"
  [db _]
  (update db :job dissoc :active-job-id :active-job-loc-id))

(defn clear-active-job
  "Dispatches events to clear active job"
  [_ _]
  {:dispatch [:job/dissoc-active-job]})

(defn back
  "Routes back to jobs page"
  [{:keys [db]} _]
  {:route "/jobs"})

(defn click-candidate
  "Event when clicking on candidate"
  [{:keys [db]} [_ id idx]]
  (let [{:keys [active-tab active-job-id active-job-loc-id]} (:job db)
        active-job (-> db :jobs :jobs (get active-job-id))
        active-location (ju/active-location (:locations active-job) active-job-loc-id)
        location-id (:job_location_id active-location)
        route (str "/job/" active-job-id (case active-tab
                                           :tab1 "/apps"
                                           :tab2 "/views") "/" location-id)]
    {:route (str "/candidate/" idx route "?jobseekerId=" id)}))

(defn show-contact-candidates-modal
  "Show modal for contacting candidates"
  [db _]
  (assoc-in db [:job :show-contact-candidates-modal?] true))

(defn click-email-candidates
  "Dispatches events to populate email modal and open"
  [{:keys [db]} [_ checked-candidates]]
  {:dispatch-n [[:email/set-email-recipients (into [] checked-candidates)]
                [:email/togg-email-modal]]})

(defn dismiss-candidate
  "Adds dismissal entry to given candidate for job location"
  [db [_ jobseeker-id job-id job-location-id]]
  (update-in db [:jobs :jobs] ju/dismiss-candidate jobseeker-id job-id job-location-id))

(events/reg-event-db
  :job/set-active-tab
  set-active-tab)

(events/reg-event-db
  :job/set-active-job
  set-active-job)

(events/reg-event-db
  :job/active-job-loc
  active-job-loc)

(events/reg-event-fx
  :job/change-active-job
  change-active-job)

(events/reg-event-db
  :preview-job/set-type
  set-type)

(events/reg-event-db
  :job/add-fetched-job
  add-fetched-job)

(events/reg-event-fx
  :job/process-response
  process-response)

(events/reg-event-fx
  :job/process-a-pending-promoted
  process-a-pending-promoted)

(events/reg-event-db
  :job/update-a-pending-promoted
  update-a-pending-promoted)

(events/reg-event-fx
  :job/get-job
  get-job)

(events/reg-event-fx
  :job/get-a-pending-promoted
  get-a-pending-promoted)

(events/reg-event-fx
  :job/set-jobseeker-tab
  set-jobseeker-tab)

(events/reg-event-fx
  :job/load-view
  load-view)

(events/reg-event-fx
  :job/prev-for-job
  prev-for-job)

(events/reg-event-fx
  :job/apps-for-job
  apps-for-job)

(events/reg-event-fx
  :job/views-for-job
  views-for-job)

(events/reg-event-db
  :job/dissoc-active-job
  dissoc-active-job)

(events/reg-event-fx
  :job/clear-active-job
  clear-active-job)

(events/reg-event-fx
  :job/back
  back)

(events/reg-event-fx
  :job/click-candidate
  click-candidate)

(events/reg-event-db
  :job/show-contact-candidates-modal
  show-contact-candidates-modal)

(events/reg-event-fx
  :job/click-email-candidates
  click-email-candidates)

(events/reg-event-db
  :job/dismiss-candidate
  dismiss-candidate)
