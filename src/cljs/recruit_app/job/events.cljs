(ns recruit_app.job.events
  (:require [re-frame.core :as rf]
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

(defn dismiss-candidate
  "Adds dismissal entry to given candidate for job location"
  [db [_ jobseeker-id job-id job-location-id]]
  (update-in db [:jobs :jobs] ju/dismiss-candidate jobseeker-id job-id job-location-id))

(rf/reg-event-db
  ;; Sets active tab in job view
  :job/set-active-tab
  (fn [db [_ tab]]
    (assoc-in db [:job :active-tab] tab)))

(rf/reg-event-db
  ;; Sets active job id
  :job/set-active-job
  (fn [db [_ job-id]]
    (assoc-in db [:job :active-job-id] job-id)))

(rf/reg-event-db
  ;; Sets active job location id
  :job/active-job-loc
  (fn [db [_ job-loc-id]]
    (assoc-in db [:job :active-job-loc-id] job-loc-id)))

(rf/reg-event-fx
  ;; Sets active job id and updates route
  :job/change-active-job
  (fn [{:keys [db]} [_ job-id]]
    {:dispatch [:job/set-active-job job-id]
     :route    (str "/job/" job-id)}))

(rf/reg-event-db
  ;; Sets preview-job type (:post if in post-job view, :active if viewing a job)
  :preview-job/set-type
  (fn [db [_ type]]
    (assoc-in db [:preview-job :type] type)))

(rf/reg-event-db
  ;; Adds job id to fetched so it will not be fetched again
  :job/add-fetched-job
  (fn [db [_ job-id]]
    (update-in db [:job :fetched] ju/add-to-set job-id)))

(rf/reg-event-fx
  ;; Process response from fetching a job
  :job/process-response
  (fn [{:keys [db]} [_ job-id response]]
    (let [job (r/job response)]
      (if job
        {:db         (update-in db [:jobs :jobs] assoc job-id job)
         :dispatch-n [[:job/add-fetched-job job-id]
                      [:job/get-a-pending-promoted job]]}
        {:dispatch [:job/add-fetched-job job-id]}))))

(rf/reg-event-fx
  ;; Processes a fetched promotion data, if valid, triggers db update
  :job/process-a-pending-promoted
  (fn [{:keys [db]} [_ job-id response]]
    (let [promotion-data (-> response str edn/read-string)]
      {:dispatch-n (cond-> []
                           promotion-data (conj [:job/update-a-pending-promoted job-id promotion-data])
                           (-> db :post-job :editing?) (conj [:post-job/populate-form (-> db :post-job :job-id)]))})))

(rf/reg-event-db
  ;; Merges promotion data of a pending job to the db
  :job/update-a-pending-promoted
  (fn [db [_ job-id promotion-data]]
    (assoc-in db [:jobs :jobs job-id :pending-promoted] promotion-data)))

(rf/reg-event-fx
  ;; Calls API to fetch job if not already fetched
  :job/get-job
  (fn [{:keys [db]} [_ job-id]]
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :get-job job-id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:job/process-response job-id]
                     :on-failure      [:http-no-on-failure]}}))

(rf/reg-event-fx
  ;; API call to retrieve promotion info for a pending job
  :job/get-a-pending-promoted
  (fn [_ [_ job]]
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
        {:dispatch      [:job/process-a-pending-promoted job-id nil]}))))

(rf/reg-event-fx
  ;; Dispatches events specific to jobseeker tabs in job view (clearing checked candidates and setting job location)
  :job/set-jobseeker-tab
  (fn [_ [_ job-loc-id]]
    {:dispatch-n [[:job/active-job-loc job-loc-id]
                  [(table/clear-checked-event ::table/job-applicants)]
                  [(table/clear-checked-event ::table/job-viewers)]]}))

(rf/reg-event-fx
  ;; Dispatches events when switching to job page
  :job/load-view
  (fn [{:keys [db]} _]
    (let [job-id (-> db :job :active-job-id)]
      {:dispatch-n [[:preview-job/set-type :active]
                    [:job/get-job job-id]
                    [:job/set-active-job job-id]
                    [:jobs/get-jobs]
                    [:scroll-top]]
       :ga/page-view ["/job" {}]})))

(rf/reg-event-fx
  ;; Dispatches events for previewing job
  :job/prev-for-job
  (fn [{:keys [db]} [_ job-id]]
    {:route      (str "/job/" job-id)
     :dispatch-n [[:job/load-view]
                  [:job/set-active-tab :tab0]]}))

(rf/reg-event-fx
  ;; Dispatches events for viewing applicants for job
  :job/apps-for-job
  (fn [_ [_ job-id job-loc-id]]
    {:route      (str "/job/" job-id "/apps")
     :dispatch-n [[:job/set-active-tab :tab1]
                  [:job/set-jobseeker-tab job-loc-id]]}))

(rf/reg-event-fx
  ;; Dispatches events for viewing viewers of job
  :job/views-for-job
  (fn [_ [_ job-id job-loc-id]]
    {:route      (str "/job/" job-id "/views")
     :dispatch-n [[:job/set-active-tab :tab2]
                  [:job/set-jobseeker-tab job-loc-id]]}))

(rf/reg-event-db
  ;; Removes active-job-id and active-job-loc-id from db
  :job/dissoc-active-job
  (fn [db _]
    (update db :job dissoc :active-job-id :active-job-loc-id)))

(rf/reg-event-fx
  ;; Dispatches events to clear active job
  :job/clear-active-job
  (fn [_ _]
    {:dispatch [:job/dissoc-active-job]}))

(rf/reg-event-fx
  ;; Routes back to jobs page
  :job/back
  (fn [{:keys [db]} _]
    {:route "/jobs"}))

(rf/reg-event-fx
  ;; Event when clicking on candidate
  :job/click-candidate
  (fn [{:keys [db]} [_ id idx]]
    (let [{:keys [active-tab active-job-id active-job-loc-id]} (:job db)
          active-job (-> db :jobs :jobs (get active-job-id))
          active-location (ju/active-location (:locations active-job) active-job-loc-id)
          location-id (:job_location_id active-location)
          route (str "/job/" active-job-id (case active-tab
                                             :tab1 "/apps"
                                             :tab2 "/views") "/" location-id)]
      {:route (str "/candidate/" idx route "?jobseekerId=" id)})))

(rf/reg-event-db
  ;; Show modal for contacting candidates
  :job/show-contact-candidates-modal
  (fn [db _]
    (assoc-in db [:job :show-contact-candidates-modal?] true)))

(rf/reg-event-fx
  ;; Dispatches events to populate email modal and open
  :job/click-email-candidates
  (fn [{:keys [db]} [_ checked-candidates]]
    {:dispatch-n [[:email/set-email-recipients (into [] checked-candidates)]
                  [:email/togg-email-modal]]}))

(rf/reg-event-db
  :job/dismiss-candidate
  dismiss-candidate)
