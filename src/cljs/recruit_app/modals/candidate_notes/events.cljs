(ns recruit-app.modals.candidate-notes.events
  (:require [recruit-app.events :as events]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.util.events :as ev]
            [cognitect.transit :as t]
            [cljs.reader :as edn]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "candidate-notes" ["active-id" "notes"])
(ev/reg-toggle-event "candidate-notes" "is-saving?")

(defn open-modal
  "Calls change events to populate db and open modal"
  [{:keys [db]} [_ secure-id]]
  {:dispatch-n [[:candidate-notes/active-id-change secure-id]
                [:candidate-notes/notes-change (or (get-in db [:candidate-notes :candidate-notes secure-id :note-content]) "")]
                [::modal/open-modal ::modal/candidate-notes]]})

(defn close-modal
  "Clears data from db"
  [_ _]
  {:dispatch-n [[:candidate-notes/active-id-change]
                [:candidate-notes/notes-change ""]]})

(defn set-candidate-notes
  "Sets candidate notes in db for given secure-id"
  [db [_ secure-id response]]
  (assoc-in db [:candidate-notes :candidate-notes secure-id] response))

(defn fetch-for-candidate
  "Calls API to get notes for given candidate"
  [{:keys [db]} [_ secure-id]]
  (when (and secure-id (not (get-in db [:candidate-notes :candidate-notes secure-id])))
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :fetch-candidate-notes secure-id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:candidate-notes/set-candidate-notes secure-id]
                     :on-failure      [:http-no-on-failure]}}))

(defn handle-save-notes-response
  "Assocs data into db and closes modal"
  [{:keys [db]} [_ response]]
  (let [secure-id (-> db :candidate-notes :active-id)]
    {:dispatch-n [[:candidate-notes/toggle-is-saving?]
                  [:candidate-notes/set-candidate-notes secure-id response]
                  [::modal/close-modal ::modal/candidate-notes]
                  [:candidate-notes/close-modal]]}))

(defn save-notes
  "Calls API to save notes for candidate"
  [{:keys [db]} _]
  (let [{:keys [active-id notes]} (:candidate-notes db)]
    {:dispatch   [:candidate-notes/toggle-is-saving?]
     :ra-http-xhrio {:method          :put
                     :uri             (u/uri :save-candidate-notes active-id)
                     :params          {:notes notes}
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:candidate-notes/handle-save-notes-response]
                     :on-failure      [:candidate-notes/handle-save-notes-response]}}))

(events/reg-event-fx
  :candidate-notes/open-modal
  open-modal)

(events/reg-event-fx
  :candidate-notes/close-modal
  close-modal)

(events/reg-event-db
  :candidate-notes/set-candidate-notes
  set-candidate-notes)

(events/reg-event-fx
  :candidate-notes/fetch-for-candidate
  fetch-for-candidate)

(events/reg-event-fx
  :candidate-notes/handle-save-notes-response
  handle-save-notes-response)

(events/reg-event-fx
  :candidate-notes/save-notes
  save-notes)
