(ns recruit-app.modals.share-resume.events
  (:require [re-frame.core :as rf]
            [recruit-app.util.events :as ev]
            [recruit-app.util.uri :as u]
            [recruit-app.util.candidate :as c]
            [ajax.core :as ajax]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "share-resume" ["emails" "message" "jobseeker-id" "show-errors?"
                               "resume-version" "job-location-id"])

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

(rf/reg-event-fx
  :share-resume/open-modal
  (fn [_ [_ jobseeker-id job-location-id resume-version]]
    {:dispatch-n [[:share-resume/jobseeker-id-change jobseeker-id]
                  [:share-resume/job-location-id-change job-location-id]
                  [:share-resume/resume-version-change resume-version]
                  [::modal/open-modal ::modal/share-resume]]}))

(rf/reg-event-db
  :share-resume/clear-form
  (fn [db _]
    (dissoc db :share-resume)))

(rf/reg-event-fx
  :share-resume/close-modal
  (fn [_ _]
    {:dispatch-n [[::modal/close-modal ::modal/share-resume]
                  [:share-resume/clear-form]]}))

(rf/reg-event-fx
  :share-resume/share-success
  share-success)

(rf/reg-event-fx
  :share-resume/share-failure
  share-failure)

(rf/reg-event-fx
  :share-resume/share
  (fn [{:keys [db]} _]
    {:dispatch   [:share-resume/show-errors?-change false]
     :ra-http-xhrio {:method          :post
                     :uri             (u/uri :share-resume)
                     :params          (c/share-resume-request (:share-resume db))
                     :format          (ajax/json-request-format)
                     :response-format (ajax/raw-response-format)
                     :on-success      [:share-resume/share-success]
                     :on-failure      [:share-resume/share-failure]}}))
