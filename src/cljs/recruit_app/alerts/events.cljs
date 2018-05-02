(ns recruit-app.alerts.events
  (:require [recruit-app.events :as events]))

(defn add-success
  [_ [_ msg]]
  {:dispatch [:alerts/add-alert :success msg]})

(defn add-error
  [_ [_ msg]]
  {:dispatch [:alerts/add-alert :error msg]})

(defn add-alert
  "Adds given alert msg to specified type"
  [{:keys [db]} [_ type msg]]
  {:db             (update-in db [:alerts type] (comp (partial into []) conj) msg)
   :dispatch-later [{:ms 3000 :dispatch [:alerts/remove-oldest-alert type]}]
   :dispatch       [:scroll-top]})

(defn remove-first-element
  "Returns new vector with first element removed.
  If empty, return empty vector"
  [alerts]
  (if (not-empty alerts)
    (subvec alerts 1)
    []))

(defn remove-oldest-alert
  "Removes first element of vector of given alert type"
  [db [_ type]]
  (update-in db [:alerts type] remove-first-element))

(events/reg-event-fx
  :alerts/add-success
  add-success)

(events/reg-event-fx
  :alerts/add-error
  add-error)

(events/reg-event-fx
  :alerts/add-alert
  add-alert)

(events/reg-event-db
  :alerts/remove-oldest-alert
  remove-oldest-alert)
