(ns recruit-app.modals.share-resume.events
  (:require [recruit-app.events :as events]
            [recruit-app.util.events :as ev]
            [recruit-app.util.uri :as u]
            [recruit-app.util.candidate :as c]
            [ajax.core :as ajax]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "share-resume" ["emails" "message" "jobseeker-id" "show-errors?"
                               "resume-version" "job-location-id"])

(defn open-modal
  "Sets data and opens modal"
  [_ [_ jobseeker-id job-location-id resume-version]]
  {:dispatch-n [[:share-resume/jobseeker-id-change jobseeker-id]
                [:share-resume/job-location-id-change job-location-id]
                [:share-resume/resume-version-change resume-version]
                [::modal/open-modal ::modal/share-resume]]})

(defn clear-form
  "Dissocs share resume from db"
  [db _]
  (dissoc db :share-resume))

(defn close-modal
  "Closes share resume modal"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/share-resume]
                [:share-resume/clear-form]]})

(defn share-success
  "Dispatch success alert and close modal"
  [_ _]
  {:dispatch-n [[:alerts/add-success "Email sent"]
                [:share-resume/close-modal]]})

(defn share-failure
  "Dispatch error alert and close modal"
  [_ _]
  {:dispatch-n [[:alerts/add-error "An unexpected error occurred. Email not sent."]
                [:share-resume/close-modal]]})

(defn share
  "Calls API to share resume"
  [{:keys [db]} _]
  {:dispatch   [:share-resume/show-errors?-change false]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :share-resume)
                   :params          (c/share-resume-request (:share-resume db))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:share-resume/share-success]
                   :on-failure      [:share-resume/share-failure]}})

(events/reg-event-fx
  :share-resume/open-modal
  open-modal)

(events/reg-event-db
  :share-resume/clear-form
  clear-form)

(events/reg-event-fx
  :share-resume/close-modal
  close-modal)

(events/reg-event-fx
  :share-resume/share-success
  share-success)

(events/reg-event-fx
  :share-resume/share-failure
  share-failure)

(events/reg-event-fx
  :share-resume/share
  share)
