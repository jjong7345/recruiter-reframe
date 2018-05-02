(ns recruit-app.post-job.thank-you.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]))

(subs/reg-subs "post-job.thank-you" [["suggested-candidates" []]
                                     ["job-id" nil] ["promoted?" false]])

(defn job-title
  "Returns job title given job-id and collection of jobs"
  [[job-id jobs] _]
  (get-in jobs [job-id :title] ""))

(defn show-suggested-candidates?
  "Only show candidates table if there are suggested candidates"
  [suggested-candidates _]
  (not-empty suggested-candidates))

(rf/reg-sub
  :post-job.thank-you/job-title
  :<- [:post-job.thank-you/job-id]
  :<- [:jobs/jobs]
  job-title)

(rf/reg-sub
  :post-job.thank-you/show-suggested-candidates?
  :<- [:post-job.thank-you/suggested-candidates]
  show-suggested-candidates?)
