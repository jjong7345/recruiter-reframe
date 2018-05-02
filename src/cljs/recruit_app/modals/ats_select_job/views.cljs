(ns recruit-app.modals.ats-select-job.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.util.input-view :as iv]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.form :as form]))

(defn modal
  "Renders modal for user to select job for exporting candidate to ATS"
  []
  (let [job-options (rf/subscribe [:ats-select-job/job-options])
        job-id (rf/subscribe [:ats-select-job/job-id])
        show-errors? (rf/subscribe [:ats-select-job/show-errors?])]
    (rf/dispatch [:ats-select-job/load-job-options])
    (fn []
      [modal/modal
       :modal-key ::modal/ats-select-job
       :title "Select Job"
       :body [[layout/column
               :padding 0
               :children [[layout/column
                           :padding 36
                           :children [[form/single-dropdown
                                       :choices @job-options
                                       :model job-id
                                       :on-change #(rf/dispatch [:ats-select-job/job-id-change %])]]]
                          (when @show-errors?
                            [form/input-error "Please select a job."])]]]
       :action {:label    "Export"
                :on-click #(rf/dispatch (if-not (= 0 @job-id)
                                          [:ats-select-job/submit]
                                          [:ats-select-job/show-errors?-change true]))}])))
