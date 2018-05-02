(ns recruit_app.pricing.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [recruit-app.util.events :as re]
            [cljs.reader :as edn]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.components.modal :as modal]))

(defn get-started
  [{:keys [db]} [_ plan]]
  (case plan
    "individual" {:external-route "/shopify/full-access"}
    "enterprise" {:dispatch [:pricing/enterprise-click]}))

(defn go-to-sign-up
  [_ _]
  {:dispatch-n [[:go-to-login]
                [:login/create-account-click]]})

(defn enterprise-click-success
  "Event on successful interested response"
  [{:keys [db]} _]
  {:dispatch [::modal/open-modal ::modal/full-access-success]})

(defn enterprise-click-fail
  "Event on interested failure"
  [_ _]
  {:dispatch [:alerts/add-error "Could not send message, please make sure you have a stable internet connection and try again."]})

(defn enterprise-click
  "Calls API to register enterprise-click"
  [_ _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :enterprise-click)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/text-response-format)
                   :on-success      [:pricing/enterprise-click-success]
                   :on-failure      [:pricing/enterprise-click-fail]}})

(rf/reg-event-fx
  :pricing/get-started
  get-started)

(rf/reg-event-fx
  :pricing/go-to-sign-up
  go-to-sign-up)

(rf/reg-event-fx
  :pricing/enterprise-click-success
  enterprise-click-success)

(rf/reg-event-fx
  :pricing/enterprise-click-fail
  enterprise-click-fail)

(rf/reg-event-fx
  :pricing/enterprise-click
  enterprise-click)
