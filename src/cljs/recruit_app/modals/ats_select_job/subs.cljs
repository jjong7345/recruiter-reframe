(ns recruit-app.modals.ats-select-job.subs
  (:require [recruit-app.util.subscription :as subs]
            [re-frame.core :as rf]))

(subs/reg-subs "ats-select-job" [["jobs" []] ["job-id" 0]
                                 ["show-errors?" false]])

(defn- job-option
  "Returns formatted option for ats job"
  [{:keys [shortcode full_title]}]
  {:id    shortcode
   :label full_title})

(defn job-options
  "Returns formatted job options using jobs in db"
  [jobs _]
  (map job-option jobs))

(rf/reg-sub
  :ats-select-job/job-options
  :<- [:ats-select-job/jobs]
  job-options)
