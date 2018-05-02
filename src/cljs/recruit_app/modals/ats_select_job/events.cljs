(ns recruit-app.modals.ats-select-job.events
  (:require [recruit-app.util.events :as ev]
            [re-frame.core :as rf]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "ats-select-job" ["secure-id" "jobs" "show-errors?"])

(defn open-modal
  "Toggles show-modal? and sets secure-id"
  [_ [_ secure-id]]
  {:dispatch-n [[::modal/open-modal ::modal/ats-select-job]
                [:ats-select-job/secure-id-change secure-id]]})

(defn load-job-options-success
  "Assocs response to job options in db"
  [_ [_ jobs]]
  {:dispatch [:ats-select-job/jobs-change jobs]})

(defn load-job-options
  "Calls API to retrieve job options for ATS"
  [_ _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :ats-jobs)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:ats-select-job/load-job-options-success]
                   :on-failure      [:http-no-on-failure]}})

(defn job-id-change
  "Toggles errors and sets job-id in db"
  [{:keys [db]} [_ job-id]]
  {:db       (assoc-in db [:ats-select-job :job-id] job-id)
   :dispatch [:ats-select-job/show-errors?-change false]})

(defn submit
  "Dispatches event to export to ATS"
  [{:keys [db]} _]
  (let [{:keys [secure-id job-id]} (:ats-select-job db)]
    {:dispatch-n [[:candidates/export-to-ats secure-id job-id]
                  [::modal/close-modal ::modal/ats-select-job]]}))

(rf/reg-event-fx
  :ats-select-job/open-modal
  open-modal)

(rf/reg-event-fx
  :ats-select-job/load-job-options
  load-job-options)

(rf/reg-event-fx
  :ats-select-job/load-job-options-success
  load-job-options-success)

(rf/reg-event-fx
  :ats-select-job/job-id-change
  job-id-change)

(rf/reg-event-fx
  :ats-select-job/submit
  submit)
