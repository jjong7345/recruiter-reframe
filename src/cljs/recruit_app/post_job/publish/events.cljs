(ns recruit-app.post-job.publish.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]
            [recruit-app.util.events :as ev]))

(ev/reg-events "post-job.publish" ["editing?" "job-id"])

(defn load-view
  "Redirects if job-id not set"
  [{:keys [db]} _]
  (cond-> {:ga/page-view ["/post-job/publish" {}]}
          (-> db :post-job.publish :job-id not) (assoc :route "/post-job")))

(defn promote-from-publish
  "Promotes pending job and routes to thank you page"
  [{:keys [db]} _]
  (let [{:keys [job-id editing?]} (:post-job.publish db)]
    {:dispatch [:jobs/promote-job
                job-id
                (if editing?
                  :post-job/goto-managejobs
                  :post-job.thank-you/handle-promote-pending-job)]}))

(defn thank-you-passthru
  "If editing, routes to manage-jobs otherwise to thank you page"
  [{:keys [db]} _]
  (let [{:keys [job-id editing?]} (:post-job.publish db)]
    (cond
      editing? {:dispatch [:post-job/goto-managejobs]}
      job-id {:route (str "/post-job/thank-you/" job-id)})))

(defn clear-db
  "Dissocs :post-job.thank-you from db"
  [db _]
  (dissoc db :post-job.publish))

(rf/reg-event-fx
  :post-job.publish/load-view
  load-view)

(rf/reg-event-fx
  :post-job.publish/promote-from-publish
  promote-from-publish)

(rf/reg-event-fx
  :post-job.publish/thank-you-passhthru
  thank-you-passthru)

(rf/reg-event-db
  :post-job.publish/clear-db
  clear-db)
