(ns recruit-app.modals.candidate-notes.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.util.events :as ev]
            [cognitect.transit :as t]
            [cljs.reader :as edn]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "candidate-notes" ["active-id" "notes"])
(ev/reg-toggle-event "candidate-notes" "is-saving?")

(rf/reg-event-fx
  :candidate-notes/open-modal
  (fn [{:keys [db]} [_ secure-id]]
    {:dispatch-n [[:candidate-notes/active-id-change secure-id]
                  [:candidate-notes/notes-change (or (get-in db [:candidate-notes :candidate-notes secure-id :note-content]) "")]
                  [::modal/open-modal ::modal/candidate-notes]]}))

(rf/reg-event-fx
  :candidate-notes/close-modal
  (fn [_ _]
    {:dispatch-n [[:candidate-notes/active-id-change]
                  [:candidate-notes/notes-change ""]]}))

(rf/reg-event-db
  :candidate-notes/set-candidate-notes
  (fn [db [_ secure-id response]]
    (assoc-in db [:candidate-notes :candidate-notes secure-id] response)))

(rf/reg-event-fx
  :candidate-notes/fetch-for-candidate
  (fn [{:keys [db]} [_ secure-id]]
    (when (and secure-id (not (get-in db [:candidate-notes :candidate-notes secure-id])))
      {:ra-http-xhrio {:method          :get
                       :uri             (u/uri :fetch-candidate-notes secure-id)
                       :timeout         5000
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:candidate-notes/set-candidate-notes secure-id]
                       :on-failure      [:http-no-on-failure]}})))

(rf/reg-event-fx
  :candidate-notes/handle-save-notes-response
  (fn [{:keys [db]} [_ response]]
    (let [secure-id (-> db :candidate-notes :active-id)]
      {:dispatch-n [[:candidate-notes/toggle-is-saving?]
                    [:candidate-notes/set-candidate-notes secure-id response]
                    [::modal/close-modal ::modal/candidate-notes]
                    [:candidate-notes/close-modal]]})))

(rf/reg-event-fx
  :candidate-notes/save-notes
  (fn [{:keys [db]} _]
    (let [{:keys [active-id notes]} (:candidate-notes db)]
      {:dispatch   [:candidate-notes/toggle-is-saving?]
       :ra-http-xhrio {:method          :put
                       :uri             (u/uri :save-candidate-notes active-id)
                       :params          {:notes notes}
                       :timeout         5000
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:candidate-notes/handle-save-notes-response]
                       :on-failure      [:candidate-notes/handle-save-notes-response]}})))
