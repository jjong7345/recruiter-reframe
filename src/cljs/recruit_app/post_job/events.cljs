(ns recruit-app.post-job.events
  (:require [recruit-app.events :as events]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]
            [recruit-app.util.events :as ev]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.job :as ju]
            [clojure.string :as string]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [cemerick.url :as url]))

(ev/reg-events "post-job" ["job-title" "job-desc" "exp" "role" "min-comp"
                           "max-comp" "industry" "hide-recruiter" "employee"
                           "hide-salary" "bonus" "other" "hide-company" "denom"
                           "promoted?" "job-id" "editing?"])

(defn load-view
  "Logs page view and clears db"
  [_ _]
  {:ga/variation :external
   :ga/page-view ["/post-job" {}]
   :dispatch-n   [[:post-job.publish/clear-db]
                  [:post-job.thank-you/clear-db]]})

(defn loc-auto-pr
  "Location autocomplete"
  [db [_ callback response]]
  (let [resp (r/loc-auto-response response)]
    (callback resp)
    (assoc db :response resp)))

(defn add-loc
  "Clears autocomplete and adds given location to locations"
  [db [_ new-location]]
  (-> db
      (update :post-job dissoc :curr-location)
      (update-in [:post-job :locations] conj new-location)))

(defn company-change
  "Change event for company"
  [db [_ company-name]]
  (assoc-in db [:post-job :company] (:name company-name)))

(defn remove-loc
  "Event for removing a location"
  [db [_ remove-loc]]
  (update-in db [:post-job :locations] (fn [locs] (remove #(= remove-loc (:name %)) locs))))

(defn see-post-job
  "Sets active panel and updates route"
  [{:keys [db]} _]
  {:dispatch [:set-active-panel :post-job-panel]
   :route    (if (-> db :post-job :editing?)
               (str "/post-job/edit/" (-> db :post-job :job-id))
               "/post-job")})

(defn redirect
  "Routes to post job page if not a valid job"
  [{:keys [db]} _]
  (if (and (ju/full-job (:post-job db)))
    {}
    {:route (if (-> db :post-job :editing?)
              (str "/post-job/edit/" (-> db :post-job :job-id))
              "/post-job")}))

(defn goto-managejobs
  "Routes to manage jobs page"
  [_ _]
  {:route "/jobs"})

(defn handle-post-job
  "Response handler when posting a job"
  [{:keys [db]} [_ {:keys [job] :as resp}]]
  (let [job-resp (ju/formatted-job (or job resp))
        {:keys [job-id promoted? editing?]} (:post-job db)
        events [[:post-job.publish/job-id-change (or job-id (:job_id job-resp))]
                [:post-job.publish/editing?-change editing?]
                (if editing?
                  [:jobs/merge-updated-job job-resp]
                  [:jobs/assoc-new-job job-resp])]]
    (if (and editing? promoted?)
      {:dispatch-n (conj events [:post-job/goto-managejobs])}
      {:db         (assoc db :loading? false)
       :dispatch-n (conj events [:post-job/see-publish])})))

(defn post-job
  "Calls API to post job"
  [{:keys [db]} _]
  (when (not (:loading? db))
    (let [recruiter (:recruiter db)
          job-request (ju/post-job-request (:post-job db) recruiter)]
      {:db            (assoc db :loading? true)
       :ra-http-xhrio {:method          (if (:jobId job-request) :put :post)
                       :uri             (u/uri :post-job (:jobId job-request))
                       :params          job-request
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:post-job/handle-post-job]
                       :on-failure      [:post-job/post-job-failure]}})))

(defn see-preview
  "Routes to preview page if job is valid"
  [{:keys [db]} _]
  (let [recruiter (:recruiter db)]
    (if (and (ju/full-job (:post-job db)) (:recruiter-id recruiter) (:email recruiter))
      {:dispatch-n [[:preview-job/set-type :post]
                    [:set-active-panel :post-preview-panel]]
       :route      "/post-job/preview"}
      {:db (assoc-in db [:post-job :show-errors?] true)})))

(defn see-publish
  "Routes to publish page"
  [_ _]
  {:dispatch [:set-active-panel :post-publish-panel]
   :route    "/post-job/publish"})

(defn loc-auto
  "Calls API for location autocomplete"
  [{:keys [db]} [_ query callback]]
  (if (> (count query) 1)
    {:http-xhrio {:method          :get
                  :uri             (u/uri :location-autocomplete (url/url-encode query))
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:post-job/loc-auto-pr callback]
                  :on-failure      [:post-job/loc-auto-pr]}
     :db         (assoc-in db [:post-job :curr-location] query)}
    (do (callback []) {})))

(defn add-com
  "Adds given company"
  [db [_ company-name]]
  (assoc-in db [:post-job :company] (:name company-name)))

(defn com-auto-pr
  "Handles response from company autocomplete"
  [db [_ callback response]]
  (let [resp (edn/read-string (str response))
        result (map #(hash-map :name %) resp)]
    (callback result)
    (assoc db :response result)))

(defn com-auto
  "Calls API for company autocomplete"
  [{:keys [db]} [_ query callback]]
  (if (> (count query) 1)
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :company-autocomplete (url/url-encode query))
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:post-job/com-auto-pr callback]
                     :on-failure      [:post-job/com-auto-pr]}
     :db            (assoc-in db [:post-job :company] query)}
    (do (callback []) {})))

(defn populate-form
  "Copies values from jobs db to post-job form when editing"
  [db [_ job-id]]
  (let [job (-> db
                (get-in [:jobs :jobs job-id])
                (ju/post-job-format)
                (assoc :editing? true))]
    (assoc db :post-job job)))

(defn post-job-failure
  "Display error message to user that request failed"
  [{:keys [db]} _]
  {:db       (assoc db :loading? false)
   :dispatch [:alerts/add-error "Failed To Post Job."]})

(defn clear-form
  "Dissocs post-job from db"
  [db _]
  (dissoc db :post-job))

(events/reg-event-fx
  :post-job/load-view
  load-view)

(events/reg-event-db
  :post-job/loc-auto-pr
  loc-auto-pr)

(events/reg-event-db
  :post-job/add-loc
  add-loc)

(events/reg-event-db
  :post-job/company-change
  company-change)

(events/reg-event-db
  :post-job/remove-loc
  remove-loc)

(events/reg-event-fx
  :post-job/see-post-job
  see-post-job)

(events/reg-event-fx
  :post-job/redirect
  redirect)

(events/reg-event-fx
  :post-job/goto-managejobs
  goto-managejobs)

(events/reg-event-fx
  :post-job/handle-post-job
  handle-post-job)

(events/reg-event-fx
  :post-job/post-job
  post-job)

(events/reg-event-fx
  :post-job/see-preview
  see-preview)

(events/reg-event-fx
  :post-job/see-publish
  see-publish)

(events/reg-event-fx
  :post-job/loc-auto
  loc-auto)

(events/reg-event-db
  :post-job/add-com
  add-com)

(events/reg-event-db
  :post-job/com-auto-pr
  com-auto-pr)

(events/reg-event-fx
  :post-job/com-auto
  com-auto)

(events/reg-event-db
  :post-job/populate-form
  populate-form)

(events/reg-event-fx
  :post-job/post-job-failure
  post-job-failure)

(events/reg-event-db
  :post-job/clear-form
  clear-form)
