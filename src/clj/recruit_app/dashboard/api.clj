(ns recruit-app.dashboard.api
  (:require [cheshire.core :as json]
            [config.core :refer [env]]
            [clojure.spec.alpha :as s]
            [recruit-app.specs.common :as common]
            [com.theladders.gardner.specs :as job-post-specs]
            [com.theladders.ace-ventura :as ss]
            [com.theladders.hitch.jobseeker :as jsk]
            [recruit-app.dashboard.core :as core]
            [recruit-app.jobs.api :as api]
            [recruit-app.recruiter.api :as r]
            [recruit-app.job :as job]
            [recruit-app.search.core :as search]
            [recruit-app.saved-search.core :as saved-search]))

(defn recruiter-dashboard
  "Retrieves all data needed to populate the recruiter dashboard for a recruiter, returns a map"
  [rec-id]
  (let [jobs (api/read-jobs rec-id)
        active-jobs (filter job/active? jobs)
        applicants (->> active-jobs
                        (map :job_id)
                        (map api/read-job)
                        (map :locations)
                        flatten
                        (map :applicants)
                        flatten)
        views (->> active-jobs
                   (map :job_id)
                   (map api/read-job)
                   (map :locations)
                   flatten
                   (map :views)
                   flatten)
        saved-searches (saved-search/saved-searches rec-id)
        search-usage (search/usage rec-id)
        {:keys [firstname lastname]} (r/recruiter-profile rec-id)]
    {:job-stats              (core/job-stats jobs)
     :engagement             (core/engagement applicants views)
     :applicant-demographics (core/applicant-demographics applicants)
     :usage                  (core/usage search-usage rec-id)
     :job-post-performance   (core/job-post-performance active-jobs)
     :saved-searches-map     (core/saved-searches-map saved-searches)
     :suggested-candidates   (core/suggested-candidates active-jobs)
     :recruiter-id           rec-id
     :recruiter-name         (str firstname " " lastname)}))


(defn recruiter-dashboard-request
  "Retrieves all data needed to populate the recruiter dashboard for a recruiter, returns a string response"
  [rec-id]
  (-> (recruiter-dashboard rec-id)
      json/generate-string))

;; specs

(s/def ::active nat-int?)
(s/def ::expiring-soon nat-int?)
(s/def ::all nat-int?)
(s/def ::job-stats (s/keys :req-un [::active ::expiring-soon ::all]))
(s/def ::applicants-this-week nat-int?)
(s/def ::applicants-this-month nat-int?)
(s/def ::applicants (s/keys :req-un [::applicants-this-week ::applicants-this-month]))
(s/def ::views-this-week nat-int?)
(s/def ::views-this-month nat-int?)
(s/def ::views (s/keys :req-un [::views-this-week ::views-this-month]))
(s/def ::engagement (s/keys :req-un [::applicants ::views]))
(s/def ::searches-this-week nat-int?)
(s/def ::searches-this-month nat-int?)
(s/def ::resume-views-this-week nat-int?)
(s/def ::resume-views-this-month nat-int?)
(s/def ::usage (s/keys :req-un [::searches-this-week ::searches-this-month ::resume-views-this-week ::resume-views-this-month]))

(s/def ::high-performing (s/coll-of ::job-post-specs/result))
(s/def ::poor-performing (s/coll-of ::job-post-specs/result))
(s/def ::job-post-performance (s/keys :req-un [::high-performing ::poor-performing]))

(s/def ::search-total nat-int?)
(s/def ::searches (s/coll-of ::ss/saved-search))
(s/def ::saved-searches (s/keys :req-un [::search-total ::searches]))

(s/def ::suggested-candidates (s/coll-of ::jsk/jobseeker))
(s/def ::recruiter-id ::common/recruiter-id)
(s/def ::recruiter-dashboard (s/keys :req-un [::job-stats ::engagement ::applicant-demographics
                                              ::usage ::job-post-performance ::saved-searches ::suggested-candidates ::recruiter-id]))

(s/fdef recruiter-dashboard-request
        :args (s/cat :recruiter-id ::common/recruiter-id)
        :ret ::recruiter-dashboard)
