(ns recruit-app.jobs.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [goog.string :as gs]
            [cljs-time.format :as f]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [recruit-app.util.subscription :as subs]
            [clojure.string :as string]
            [recruit-app.util.job :as ju]
            [recruit-app.util.sort :as s]
            [recruit-app.job :as job]))

(subs/reg-subs "jobs" [["active-tab" :tab0] ["filter" ""] ["promote-job-id" nil]
                       ["purchase-job-id" nil] ["remove-job-id" nil]
                       ["preview-job-id" nil] ["jobs" []] ["loaded?" false]])

(defn job
  "Returns job by id from jobs map"
  [[job-id jobs] _]
  (get jobs job-id))

(defn empty-or-include
  [a b]
  (or (string/blank? a)
      (string/includes?
        (string/lower-case b)
        (string/lower-case a))))

(defn filtered-jobs
  "Returns jobs that have given fltr text in the title"
  [jobs fltr]
  (filter #(empty-or-include fltr (:title %)) jobs))

(defn jobs
  "Returns jobs filtered by given filter"
  [[jobs filter] _]
  (-> jobs vals vec (filtered-jobs filter)))

(defn filtered
  "Calls filter-fn on jobs"
  [filter-fn jobs]
  (filter filter-fn jobs))

(rf/reg-sub
  :jobs/all
  :<- [:jobs/jobs]
  :<- [:jobs/filter]
  jobs)

(rf/reg-sub
  :jobs/active
  :<- [:jobs/all]
  (partial filtered job/active?))

(rf/reg-sub
  :jobs/pending
  :<- [:jobs/all]
  (partial filtered job/pending?))

(rf/reg-sub
  :jobs/rejected
  :<- [:jobs/all]
  (partial filtered job/rejected?))

(rf/reg-sub
  :jobs/removed
  :<- [:jobs/all]
  (partial filtered job/removed?))

(rf/reg-sub
  ;; Returns job for Promote Job modal
  :jobs/promote-job
  :<- [:jobs/promote-job-id]
  :<- [:jobs/jobs]
  job)

(rf/reg-sub
  ;; Returns job for Purchase Promotion For Job modal
  :jobs/purchase-job
  :<- [:jobs/purchase-job-id]
  :<- [:jobs/jobs]
  job)

(rf/reg-sub
  ;; Returns job for Confirm Remove modal
  :jobs/remove-job
  :<- [:jobs/remove-job-id]
  :<- [:jobs/jobs]
  job)
