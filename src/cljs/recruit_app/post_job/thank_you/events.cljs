(ns recruit-app.post-job.thank-you.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.events :as ev]))

(ev/reg-events "post-job.thank-you" ["job-id" "promoted?"])

(defn load-view
  "Fetches suggested candidates if job-id set, otherwise redirects"
  [{:keys [db]} _]
  (let [job-id (get-in db [:post-job.thank-you :job-id])]
    {:ga/page-view ["/post-job/thank-you" {}]
     :dispatch     (if job-id [:jobs/get-jobs]
                              [:post-job/redirect])}))

(defn handle-suggested
  "Associates suggested candidates to db from response"
  [db [_ response]]
  (let [resp (edn/read-string (str response))]
    (assoc-in db [:post-job.thank-you :suggested-candidates] resp)))

(defn fetch-suggested-candidates
  "Calls API to get suggested candidates for job"
  [{:keys [db]} [_ job-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :suggested-candidates job-id)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:post-job.thank-you/handle-suggested]
                   :on-failure      [:post-job.thank-you/handle-suggested]}})

(defn handle-promote-pending-job
  "Parses response from promoting pending job and updates db"
  [{:keys [db]} [_ response]]
  (let [resp (->> response
                  str
                  edn/read-string
                  keywordize-keys)]
    {:db    (-> db
                (assoc-in [:post-job.thank-you :promoted?] (:to-be-promoted resp))
                (assoc-in [:recruiter :pjl-count] (dec (get-in db [:recruiter :pjl-count])))
                (assoc :loading? false))
     :route (str "/post-job/thank-you/" (-> db :post-job.publish :job-id))}))

(defn promote-pending-job
  "Calls API to promote pending job"
  [{:keys [db]} _]
  (when (not (:loading? db))
    (let [job-id (get-in db [:post-job.thank-you :job-id])]
      {:dispatch [:jobs/promote-job job-id :post-job.thank-you/handle-promote-pending-job]})))

(defn clear-db
  "Dissocs :post-job.thank-you from db"
  [db _]
  (dissoc db :post-job.thank-you))

(rf/reg-event-fx
  :post-job.thank-you/load-view
  load-view)

(rf/reg-event-db
  :post-job.thank-you/handle-suggested
  handle-suggested)

(rf/reg-event-fx
  :post-job.thank-you/fetch-suggested-candidates
  fetch-suggested-candidates)

(rf/reg-event-fx
  :post-job.thank-you/handle-promote-pending-job
  handle-promote-pending-job)

(rf/reg-event-fx
  :post-job.thank-you/promote-pending-job
  promote-pending-job)

(rf/reg-event-db
  :post-job.thank-you/clear-db
  clear-db)
