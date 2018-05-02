(ns recruit-app.components.modal.registry
  (:require [re-frame.core :as rf]
            [recruit-app.events :as events]
            [recruit-app.components.modal :as modal]))

;; Subs

(defn show-modal?
  "Returns whether to show modal for given modal-key"
  [db [_ modal-key]]
  (get-in db [::modal/modal modal-key ::modal/show-modal?] false))

(rf/reg-sub
  ::modal/show-modal?
  show-modal?)

;; Events

(defn open-modal
  "Opens modal for given modal key"
  [db [_ modal-key]]
  (assoc-in db [::modal/modal modal-key ::modal/show-modal?] true))

(defn close-modal
  "Closes modal for given modal key"
  [db [_ modal-key]]
  (assoc-in db [::modal/modal modal-key ::modal/show-modal?] false))

(events/reg-event-db
  ::modal/open-modal
  open-modal)

(events/reg-event-db
  ::modal/close-modal
  close-modal)
