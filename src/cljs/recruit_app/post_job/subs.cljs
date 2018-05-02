(ns recruit-app.post-job.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as rs]
            [clojure.string :as string]
            [recruit-app.util.job :as ju]
            [recruit-app.util.number_format :as nf]))

(def post-job-subs [["job-title" ""] ["job-desc" ""] ["exp" 6] ["role" 0]
                    ["min-comp" 0] ["max-comp" 0] ["bonus" ""] ["other" ""]
                    ["company" ""] ["hide-company" false] ["industry" 0]
                    ["hide-recruiter" false] ["employee" 0]
                    ["hide-salary" false] ["location" ""]["locations" []]
                    ["show-errors?" false] ["promoted" false] ["editing?" false]
                    ["denom" "%"] ["job-id" nil]])

(rs/reg-subs "post-job" post-job-subs)

(defn min-total-comp
  "Calculates minimum compensation and bonus"
  [[min-comp bonus denom-string] _]
  (ju/comp-with-bonus-post min-comp bonus denom-string))

(defn max-total-comp
  "Calculates maximum compensation and bonus"
  [[max-comp bonus denom-string] _]
  (ju/comp-with-bonus-post max-comp bonus denom-string))

(defn max-compensation-view
  "Returns min total comp if there is no max total comp"
  [[min-total-comp max-total-comp] _]
  (if (= max-total-comp 0)
    min-total-comp
    max-total-comp))

(defn compensation-not-valid?
  "Checks for valid compensation"
  [[min-comp max-total-comp] _]
  (and (> min-comp 0) (< max-total-comp 80)))

(defn salary-label
  "Returns the correct Salary label, DOE or the salary range"
  [[hide-salary min-total-comp max-compensation-view] _]
  (if hide-salary "DOE"
                   (nf/remove-cents (str (nf/number-conversion min-total-comp) " - " (nf/number-conversion max-compensation-view)))))

(defn base-salary-label
  "Returns the correct Base salary label"
  [[hide-salary min-comp max-comp] _]
  (nf/remove-cents (str (nf/number-conversion (* min-comp 1000)) " - " (nf/number-conversion (* max-comp 1000)))))

(defn bonus-string
  "Formats and returns bonus"
  [[bonus denom-string] _]
  (let [has-denom? (or (string/includes? bonus "%") (string/includes? bonus "$"))]
    (cond
      (and (or (= denom-string "%") (= denom-string "$")) has-denom?) (str bonus)
      (and (= denom-string "%") (not has-denom?)) (str bonus denom-string)
      (and (= denom-string "$") (not has-denom?)) (str denom-string bonus "K"))))

(defn page-loaded?
  "If editing a job, page not considered loaded until job is fetched"
  [[editing? job-id fetched] _]
  (or (not editing?) (contains? fetched job-id)))

(rf/reg-sub
  :post-job/bonus-string
  :<- [:post-job/bonus]
  :<- [:post-job/denom]
  bonus-string)

(rf/reg-sub
  :post-job/min-total-comp
  :<- [:post-job/min-comp]
  :<- [:post-job/bonus-string]
  :<- [:post-job/denom]
  min-total-comp)

(rf/reg-sub
  :post-job/max-total-comp
  :<- [:post-job/max-comp]
  :<- [:post-job/bonus-string]
  :<- [:post-job/denom]
  max-total-comp)

(rf/reg-sub
  :post-job/exp-string
  :<- [:post-job/exp]
  (fn [exp _]
    (ju/exp-name exp)))

(rf/reg-sub
  :post-job/full-job
  (fn [{:keys [post-job]} _]
    post-job))

(rf/reg-sub
  :post-job/max-compensation-view
  :<- [:post-job/min-total-comp]
  :<- [:post-job/max-total-comp]
  max-compensation-view)

(rf/reg-sub
  :post-job/compensation-not-valid?
  :<- [:post-job/min-comp]
  :<- [:post-job/max-total-comp]
  compensation-not-valid?)

(rf/reg-sub
  :post-job/salary-label
  :<- [:post-job/hide-salary]
  :<- [:post-job/min-total-comp]
  :<- [:post-job/max-compensation-view]
  salary-label)

(rf/reg-sub
  :post-job/base-salary-label
  :<- [:post-job/hide-salary]
  :<- [:post-job/min-comp]
  :<- [:post-job/max-comp]
  base-salary-label)

(rf/reg-sub
  :post-job/page-loaded?
  :<- [:post-job/editing?]
  :<- [:post-job/job-id]
  :<- [:job/fetched]
  page-loaded?)


(defn value-or-default
  "Associates default to job if not set"
  [job [k default]]
  (let [key (keyword k)]
    (if (contains? job key)
      job
      (assoc job key default))))

(defn with-default-values
  "Iterates through post-job subs to add defaults if not set"
  [job]
  (reduce value-or-default job post-job-subs))

(rf/reg-sub
  :post-job/job
  :<- [:post-job/full-job]
  :<- [:post-job/exp-string]
  :<- [:post-job/min-total-comp]
  :<- [:post-job/max-total-comp]
  (fn [[job exp-string min-total-comp max-total-comp] _]
    (-> job
        (with-default-values)
        (merge {:min-total-comp min-total-comp
                :max-total-comp max-total-comp
                :exp-string     exp-string}))))

(rf/reg-sub
  :post-job/valid-job
  :<- [:post-job/job]
  (fn [job _]
    (ju/full-job job)))