(ns recruit-app.modals.checkout.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [cljs.reader :as edn]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]))

(rf/reg-event-fx
  :checkout/on-page-step-update
  (fn [{:keys [db]} [_ new-val]]
    {:ga/event     ["shopify" "load" new-val {}]
     :ga/page-view [(str "/shopify/" new-val) {}]
     :db           (assoc-in db [:checkout :step] new-val)}))

(rf/reg-event-fx
  :checkout/show-modal-change
  (fn [{:keys [db]} [_ new-val]]
    {:db (assoc-in db [:checkout :open-modal] new-val)}))

(rf/reg-event-fx
  :checkout/promote-publish
  (fn [{:keys [db]} _]
    (let [job_id (get-in db [:checkout :promote-job-id])
          panel (get-in db [:active-panel])
          event {:dispatch-n [[:checkout/show-modal-change false]
                              (if (= panel :jobs-panel)
                                [:jobs/promote-job job_id]
                                [:post-job.publish/promote-from-publish])]}]
     (if (not (= panel :jobs-panel))
       (conj event {:route      "/post-job/thank-you"})
       event))))

(rf/reg-event-fx
  :checkout/promote-checkout-modal
  (fn [{:keys [db]} [_ & [promote-job-id]]]
    {:dispatch [:checkout/show-modal-change true]
     :db (assoc-in db [:checkout :promote-job-id] promote-job-id)}))

(rf/reg-event-db
  :checkout/modal-url
  (fn [db [_ response]]
    (->> response
         str
         (assoc-in db [:checkout :modal-url]))))

(rf/reg-event-fx
  :checkout/get-modal-url
  (fn [{:keys [db]} _]
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :shopify)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/raw-response-format)
                     :on-success      [:checkout/modal-url]
                     :on-failure      [:checkout/modal-url]}}))
