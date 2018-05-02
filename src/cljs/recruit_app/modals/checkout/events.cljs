(ns recruit-app.modals.checkout.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [cljs.reader :as edn]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]))

(defn on-page-step-update
  "Logs google analytics events and assocs new page in db"
  [{:keys [db]} [_ new-val]]
  {:ga/event     ["shopify" "load" new-val {}]
   :ga/page-view [(str "/shopify/" new-val) {}]
   :db           (assoc-in db [:checkout :step] new-val)})

(defn show-modal-change
  "Opens checkout modal"
  [{:keys [db]} [_ new-val]]
  {:db (assoc-in db [:checkout :open-modal] new-val)})

(defn promote-publish
  "Opens modal to promote job"
  [{:keys [db]} _]
  (let [job_id (get-in db [:checkout :promote-job-id])
        panel (get-in db [:active-panel])
        event {:dispatch-n [[:checkout/show-modal-change false]
                            (if (= panel :jobs-panel)
                              [:jobs/promote-job job_id]
                              [:post-job.publish/promote-from-publish])]}]
    (if (not (= panel :jobs-panel))
      (conj event {:route      "/post-job/thank-you"})
      event)))

(defn promote-checkout-modal
  "Opens checkout modal for promoting a job"
  [{:keys [db]} [_ & [promote-job-id]]]
  {:dispatch [:checkout/show-modal-change true]
   :db (assoc-in db [:checkout :promote-job-id] promote-job-id)})

(defn modal-url
  "Associates modal url from response into db"
  [db [_ response]]
  (->> response
       str
       (assoc-in db [:checkout :modal-url])))

(defn get-modal-url
  "Call API to retrieve URL for shopify modal"
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :shopify)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:checkout/modal-url]
                   :on-failure      [:checkout/modal-url]}})

(events/reg-event-fx
  :checkout/on-page-step-update
  on-page-step-update)

(events/reg-event-fx
  :checkout/show-modal-change
  show-modal-change)

(events/reg-event-fx
  :checkout/promote-publish
  promote-publish)

(events/reg-event-fx
  :checkout/promote-checkout-modal
  promote-checkout-modal)

(events/reg-event-db
  :checkout/modal-url
  modal-url)

(events/reg-event-fx
  :checkout/get-modal-url
  get-modal-url)
