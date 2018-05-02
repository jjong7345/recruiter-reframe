(ns recruit-app.get-full-access.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [day8.re-frame.async-flow-fx]
            [recruit-app.util.uri :as u]
            [recruit-app.util.events :as ev]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "get-full-access" ["first-name" "last-name" "email" "phone-number" "company" "title" "comments" "show-errors?"
                                  "confirmation-page?" "show-confirmation-alert-bar?"])

(defn process-response
  [_ _]
  {:dispatch-n [[:get-full-access/clear-db]
                [::modal/open-modal ::modal/full-access-success]]})

(defn submit
  [{:keys [db]} _]
  (let [contact (get-in db [:get-full-access])]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :get-full-access)
                     :params          contact
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:get-full-access/process-response]
                     :on-failure      [:get-full-access/process-response]}}))

(defn clear-db
  [db _]
  (dissoc db :get-full-access))

(defn on-confirmation-modal-btn-click
  "Event when button in confirmation modal is clicked"
  [_ _]
  {:route (u/uri :search)})

(rf/reg-event-fx
  :get-full-access/process-response
  process-response)

(rf/reg-event-fx
  :get-full-access/submit
  submit)

(rf/reg-event-db
  :get-full-access/clear-db
  clear-db)

(rf/reg-event-fx
  :get-full-access/on-confirmation-modal-btn-click
  on-confirmation-modal-btn-click)
