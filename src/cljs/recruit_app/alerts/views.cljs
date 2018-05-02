(ns recruit-app.alerts.views
  (:require [re-com.core :as rc]
            [re-frame.core :as rf]))

(defn alert
  "Creates rc/box with alert message and class of given type"
  [message type]
  [rc/box
   :class (str "ra-alert " type)
   :justify :center
   :child message])

(defn success-alert
  "Returns alert component with success type"
  [message]
  [alert message "success"])

(defn error-alert
  "Returns alert component with error type"
  [message]
  [alert message "error"])

(defn alerts
  []
  (let [success-alerts (rf/subscribe [:alerts/success])
        error-alerts (rf/subscribe [:alerts/error])
        active-panel (rf/subscribe [:active-panel])]
    (fn []
      [rc/v-box
       :class (str "alerts " (name @active-panel) "-alert-bar")
       :justify :center
       :children (into
                   (reduce #(conj %1 [success-alert %2]) [] @success-alerts)
                   (reduce #(conj %1 [error-alert %2]) [] @error-alerts))])))
