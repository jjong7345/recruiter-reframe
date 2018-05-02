(ns recruit_app.jobs.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.events :as ev]
            [cljs-time.core :as time]
            [cljs-time.format :as tf]
            [cognitect.transit :as t]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.job :as ju]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "jobs" ["loaded?" "active-tab" "filter" "promote-job-id"
                       "remove-job-id" "purchase-job-id"])

(defn load-view
  "Dispatches call to get jobs"
  [_ _]
  {:dispatch [:jobs/get-jobs]
   :ga/page-view ["/jobs" {}]})

(defn process-response
  "Processes the response from get-jobs"
  [{:keys [db]} [_ raw]]
  (let [jobs (r/jobs raw)]
    {:db         (update-in db [:jobs :jobs] #(merge jobs %))
     :dispatch-n [[:jobs/get-pending-promoted jobs]
                  [:jobs/loaded?-change true]]}))

(defn repost-job-failure
  "Displays alert that repost failed"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Repost Job. Please Try Again."]})

(defn process-pending-promoted
  "If we received promotion data for pending jobs, update db,
  otherwise end processing"
  [_ [_ response]]
  (when-let [promotion-data (-> response str edn/read-string)]
    {:dispatch [:jobs/update-pending-promoted promotion-data]}))

(defn update-pending-promoted
  "merges new pending-promoted data to respective pending jobs in the db"
  [db [_ promotion-data]]
  (let [jobs-map (-> db :jobs :jobs)
        promotion-map (into {} (for [[k v] promotion-data]
                                 [(-> k name edn/read-string) v]))
        promoted-job-ids (keys promotion-map)
        update-fn #(assoc % :pending-promoted (get promotion-map (:job_id %)))
        updated-jobs (reduce #(update %1 %2 update-fn) jobs-map promoted-job-ids)]
    (assoc-in db [:jobs :jobs] updated-jobs)))

(defn get-jobs
  "Calls api to fetch jobs. Will only fetch if not fetching
  and no jobs have been loaded"
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :get-jobs)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:jobs/process-response]
                   :on-failure      [:http-no-on-failure]}})

(defn get-pending-promoted
  "Fetches promotion info for jobs in pending state 'P'"
  [_ [_ jobs]]
  (let [pending-job-ids (map :job_id (filter #(= "P" (:job_status_id %)) (vals jobs)))]
    (when (and pending-job-ids (> (count pending-job-ids) 0))
      {:ra-http-xhrio {:method          :post
                       :uri             (u/uri :pending-promoted-jobs)
                       :params          {:job-ids pending-job-ids}
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:jobs/process-pending-promoted]
                       :on-failure      [:http-no-on-failure]}})))

(defn purchase-promotions
  "Sets purchase-job-id that will trigger Purchase Promotion for Job modal"
  [_ [_ job-id]]
  {:dispatch-n [[:set-active-panel :jobs-panel]
                [:jobs/purchase-job-id-change job-id]]})

(defn promote-job-modal
  "Sets promote-job-id that will trigger Promote Job modal"
  [_ [_ job-id]]
  {:dispatch-n [[:set-active-panel :jobs-panel]
                [:jobs/promote-job-id-change job-id]]})

(defn close-promote-job-modal
  "Sets promote-job-id to nil which will close Promote Job modal"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/promote-job]
                [::modal/close-modal ::modal/purchase-again]
                [:jobs/promote-job-id-change nil]]
   :route      "/jobs"})

(defn close-remove-job-modal
  "Sets remove-job-id to nil which will close Confirm Remove modal"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/remove-job]
                [:jobs/remove-job-id-change nil]]
   :route      "/jobs"})

(defn promote-job-success
  "Handles response from job promotion"
  [_ [_ job-id {:keys [promoted]}]]
  {:dispatch-n (if promoted
                 [[:job/get-job job-id]
                  [:recruiter/decrement-promoted-job-count]
                  [:jobs/close-promote-job-modal]
                  [:alerts/add-success "Successfully Promoted Job!"]]
                 [[:jobs/close-promote-job-modal]
                  [:alerts/add-error "Failed To Promote Job. Please Try Again."]])})

(defn promote-job-failure
  "Closes promote job modal and displays alert"
  [_ _]
  {:dispatch-n [[:jobs/close-promote-job-modal]
                [:alerts/add-error "Failed To Promote Job. Please Try Again."]]})

(defn promote-job
  "Calls to api to promote job"
  [_ [_ job-id on-success]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :promote-job job-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      (if on-success [on-success] [:jobs/promote-job-success job-id])
                   :on-failure      [:jobs/promote-job-failure]}})

(defn edit-job
  "Routes to edit job page"
  [{:keys [db]} [_ job-id]]
  {:route (str "/post-job/edit/" job-id)})

(defn edit
  [{:keys [db]} _]
  (let [job-id (-> db :post-job :job-id)]
    {:dispatch-n (cond-> [[:job/get-job job-id]]
                         (-> db :jobs :jobs) (conj [:post-job/populate-form job-id]))}))

(defn remove-job-success
  "Handles response from removing job"
  [_ [_ job-id]]
  {:dispatch-n [[:jobs/close-remove-job-modal]
                [:job/get-job job-id]
                [:alerts/add-success "Successfully Removed Job"]]
   :route      "/jobs"})

(defn remove-job-failure
  "Handles failure response for removing jobs"
  [_ [_ job-id resp]]
  (cond
    ;item was previously removed from backend (but frontend wasn't aware)
    (= 405 (:status resp)) {:dispatch [:jobs/remove-job-success job-id]}

    ;just close modal otherwise
    :else {:dispatch [:jobs/close-remove-job-modal]
           :route    "/jobs"}))

(defn remove-job
  "Calls to api to remove job"
  [_ [_ job-id]]
  {:ra-http-xhrio {:method          :delete
                   :uri             (u/uri :remove-job job-id)
                   :timeout         7000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:jobs/remove-job-success job-id]
                   :on-failure      [:jobs/remove-job-failure job-id]}})

(defn remove-job-click
  "Sets remove-job-id and opens modal"
  [_ [_ job-id]]
  {:dispatch-n [[:jobs/remove-job-id-change job-id]
                [::modal/open-modal ::modal/remove-job]]})

(defn repost-job-success
  "Handles response from reposting job"
  [{:keys [db]} [_ {:keys [jobId publishDate]}]]
  {:db       (assoc-in db [:jobs :jobs jobId :publication_date] publishDate)
   :dispatch [:jobs/active-tab-change :tab0]})

(defn repost-job
  "Calls to api to repost job"
  [_ [_ job-id]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :repost-job job-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:jobs/repost-job-success]
                   :on-failure      [:jobs/repost-job-failure]}})

(defn assoc-new-job
  "Assocs given job to jobs collection in db"
  [db [_ {:keys [job_id] :as job}]]
  (assoc-in db [:jobs :jobs job_id] job))

(defn- with-applicants-and-views
  "Will assoc applicants and views from given location map if job_location_id
  exists in map"
  [keyed-locations {:keys [job_location_id] :as location}]
  (if-let [existing-location (get keyed-locations job_location_id)]
    (-> location
        (assoc :applicants (:applicants existing-location))
        (assoc :views (:views existing-location)))
    location))

(defn- merge-job
  "Given existing job and updated job, will merge while keeping promotion,
  applicant and view information intact"
  [{:keys [featured locations]} updated-job]
  (let [keyed-locations (zipmap (map :job_location_id locations) locations)]
    (-> updated-job
        (assoc :featured featured)
        (update
          :locations
          #(map (partial with-applicants-and-views keyed-locations) %)))))

(defn merge-updated-job
  "Merges updated job into jobs collection in db"
  [db [_ {:keys [job_id] :as job}]]
  (update-in
    db
    [:jobs :jobs job_id]
    merge-job
    job))

(events/reg-event-fx
  :jobs/load-view
  load-view)

(events/reg-event-fx
  :jobs/process-response
  process-response)

(events/reg-event-fx
  :jobs/process-pending-promoted
  process-pending-promoted)

(events/reg-event-db
  :jobs/update-pending-promoted
  update-pending-promoted)

(events/reg-event-fx
  :jobs/get-jobs
  get-jobs)

(events/reg-event-fx
  :jobs/get-pending-promoted
  get-pending-promoted)

(events/reg-event-fx
  :jobs/purchase-promotions
  purchase-promotions)

(events/reg-event-fx
  :jobs/promote-job-modal
  promote-job-modal)

(events/reg-event-fx
  :jobs/close-promote-job-modal
  close-promote-job-modal)

(events/reg-event-fx
  :jobs/close-remove-job-modal
  close-remove-job-modal)

(events/reg-event-fx
  :jobs/promote-job-success
  promote-job-success)

(events/reg-event-fx
  :jobs/promote-job-failure
  promote-job-failure)

(events/reg-event-fx
  :jobs/promote-job
  promote-job)

(events/reg-event-fx
  :jobs/edit-job
  edit-job)

(events/reg-event-fx
  :jobs/edit
  edit)

(events/reg-event-fx
  :jobs/remove-job-success
  remove-job-success)

(events/reg-event-fx
  :jobs/remove-job-failure
  remove-job-failure)

(events/reg-event-fx
  :jobs/remove-job
  remove-job)

(events/reg-event-fx
  :jobs/remove-job-click
  remove-job-click)

(events/reg-event-fx
  :jobs/repost-job-success
  repost-job-success)

(events/reg-event-fx
  :jobs/repost-job
  repost-job)

(events/reg-event-fx
  :jobs/repost-job-failure
  repost-job-failure)

(events/reg-event-db
  :jobs/assoc-new-job
  assoc-new-job)

(events/reg-event-db
  :jobs/merge-updated-job
  merge-updated-job)
