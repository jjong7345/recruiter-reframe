(ns recruit-app.post-job.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]
            [recruit-app.util.events :as re]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.job :as ju]
            [clojure.string :as string]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [cemerick.url :as url]))

(re/reg-events "post-job" ["job-title" "job-desc" "exp" "role" "min-comp"
                           "max-comp" "industry" "hide-recruiter" "employee"
                           "hide-salary" "bonus" "other" "hide-company" "denom"
                           "promoted?" "job-id" "editing?"])

(defn post-job-failure
  "Display error message to user that request failed"
  [{:keys [db]} _]
  {:db       (assoc db :loading? false)
   :dispatch [:alerts/add-error "Failed To Post Job."]})

(defn clear-form
  "Dissocs post-job from db"
  [db _]
  (dissoc db :post-job))

(rf/reg-event-fx
  :post-job/load-view
  (fn [_ _]
    {:ga/variation :external
     :ga/page-view ["/post-job" {}]
     :dispatch-n [[:post-job.publish/clear-db]
                  [:post-job.thank-you/clear-db]]}))

(rf/reg-event-db
  :post-job/loc-auto-pr
  (fn [db [_ callback response]]
    (let [resp (r/loc-auto-response response)]
      (callback resp)
      (assoc db :response resp))))

(rf/reg-event-db
  :post-job/add-loc
  (fn [db [_ new-location]]
    (-> db (update :post-job dissoc :curr-location)
        (update-in [:post-job :locations] conj new-location))))


(rf/reg-event-db
  :post-job/company-change
  (fn [db [_ company-name]]
    (assoc-in db [:post-job :company] (:name company-name))))

(rf/reg-event-db
  :post-job/remove-loc
  (fn [db [_ remove-loc]]
    (update-in db [:post-job :locations] (fn [locs] (remove #(= remove-loc (:name %)) locs)))))

(rf/reg-event-fx
  :post-job/see-post-job
  (fn [{:keys [db]} _]
    {:dispatch [:set-active-panel :post-job-panel]
     :route    (if (-> db :post-job :editing?)
                 (str "/post-job/edit/" (-> db :post-job :job-id))
                 "/post-job")}))

(rf/reg-event-fx
  :post-job/redirect
  (fn [{:keys [db]} _]
    (if (and (ju/full-job (:post-job db)))
      {}
      {:route (if (-> db :post-job :editing?)
                (str "/post-job/edit/" (-> db :post-job :job-id))
                "/post-job")})))

(rf/reg-event-fx
  :post-job/goto-managejobs
  (fn [_ _]
    {:route "/jobs"}))

(rf/reg-event-fx
  :post-job/handle-post-job
  (fn [{:keys [db]} [_ {:keys [job] :as resp}]]
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
         :dispatch-n (conj events [:post-job/see-publish])}))))

(rf/reg-event-fx
  :post-job/post-job
  (fn [{:keys [db]} _]
    (when (not (:loading? db))
      (let [recruiter (:recruiter db)
            job-request (ju/post-job-request (:post-job db) recruiter)]
        {:db         (assoc db :loading? true)
         :ra-http-xhrio {:method          (if (:jobId job-request) :put :post)
                         :uri             (u/uri :post-job (:jobId job-request))
                         :params          job-request
                         :format          (ajax/json-request-format)
                         :response-format (ajax/json-response-format {:keywords? true})
                         :on-success      [:post-job/handle-post-job]
                         :on-failure      [:post-job/post-job-failure]}}))))

(rf/reg-event-fx
  :post-job/see-preview
  (fn [{:keys [db]} _]
    (let [recruiter (:recruiter db)]
      (if (and (ju/full-job (:post-job db)) (:recruiter-id recruiter) (:email recruiter))
        {:dispatch-n [[:preview-job/set-type :post]
                      [:set-active-panel :post-preview-panel]]
         :route      "/post-job/preview"}
        {:db (assoc-in db [:post-job :show-errors?] true)}))))

(rf/reg-event-fx
  :post-job/see-publish
  (fn [_ _]
    {:dispatch [:set-active-panel :post-publish-panel]
     :route    "/post-job/publish"}))

(rf/reg-event-fx
  :post-job/loc-auto
  (fn [{:keys [db]} [_ query callback]]
    (if (> (count query) 1)
      {:http-xhrio {:method          :get
                    :uri             (u/uri :location-autocomplete (url/url-encode query))
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:post-job/loc-auto-pr callback]
                    :on-failure      [:post-job/loc-auto-pr]}
       :db         (assoc-in db [:post-job :curr-location] query)}
      (do (callback []) {}))))

(rf/reg-event-db
  :post-job/add-com
  (fn [db [_ company-name]]

    (assoc-in db [:post-job :company] (:name company-name))))

(rf/reg-event-db
  :post-job/com-auto-pr
  (fn [db [_ callback response]]
    (let [resp (edn/read-string (str response))
          result (map #(hash-map :name %) resp)]
      (callback result)
      (assoc db :response result))))

(rf/reg-event-fx
  :post-job/com-auto
  (fn [{:keys [db]} [_ query callback]]
    (if (> (count query) 1)
      {:ra-http-xhrio {:method          :get
                       :uri             (u/uri :company-autocomplete (url/url-encode query))
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:post-job/com-auto-pr callback]
                       :on-failure      [:post-job/com-auto-pr]}
       :db            (assoc-in db [:post-job :company] query)}
      (do (callback []) {}))))

(rf/reg-event-db
  :post-job/populate-form
  (fn [db [_ job-id]]
    (let [job (-> db
                  (get-in [:jobs :jobs job-id])
                  (ju/post-job-format)
                  (assoc :editing? true))]
      (assoc db :post-job job))))

(rf/reg-event-fx
  :post-job/post-job-failure
  post-job-failure)

(rf/reg-event-db
  :post-job/clear-form
  clear-form)
