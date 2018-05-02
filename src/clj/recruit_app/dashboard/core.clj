(ns recruit-app.dashboard.core
  (:require [config.core :refer [env]]
            [recruit-app.jobs.demographics :as demo]
            [recruit-app.job :as job]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [recruit-app.kafka.query.interaction :as i]
            [recruit-app.util.http :as h]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [cheshire.core :as json]
            [recruit-app.suggested-candidates.api :as suggested]))

(defn- applicants
  "Returns applicants given collection of locations"
  [locations]
  (map (comp flatten :applicants) locations))

(defn approved-or-filled?
  "Checks the job_status of the job for the ALL Jobs count, should include Active and Expired and Filled jobs. Active is Approved but not Expired,
  whereas Expired is Approved with a publication date > 56 days from now but their underlying status is Approved. Filled is a status when job posting
  was successfully closed."
  [job]
  (or (job/approved? job) (job/filled? job)))

(defn all-jobs
  "Retrieve all jobs that are Approved or Expired or Filled"
  [jobs]
  (filter approved-or-filled? jobs))

(defn- within-n-days?
  "Returns whether a given date string is within n days of now"
  [date-string n]
  (when date-string
    (try (time/after?
           (f/parse (f/formatters :date-time-no-ms) date-string)
           (time/minus (time/now) (time/days n)))
         (catch IllegalArgumentException e
           (time/after?
             (f/parse (f/formatters :date-time) date-string)
             (time/minus (time/now) (time/days n)))))))

(defn within-30-days?
  "Returns whether given date string is within 30 days of now"
  [date-string]
  (within-n-days? date-string 30))

(defn within-7-days?
  "Returns whether given date string is within 30 days of now"
  [date-string]
  (within-n-days? date-string 7))

(defn applicants-within-time-interval
  "Retrieve all applicants who applied to this job within a specified interval"
  [applicants time-interval]
  (filter (comp time-interval :apply_time) applicants))

(defn views-within-time-interval
  "Retrieve all applicants who viewed this job within a specified interval"
  [views time-interval]
  (filter (comp time-interval :view_time) views))

(defn- datetime-within-n-days?
  "Returns whether given datetime is within given amount of days"
  [days datetime]
  (time/before? (time/minus (time/now) (time/days days)) datetime))

(defn searches-within-n-days
  "Takes map keys by datetime of counts per day and returns count within given days"
  [usage days]
  (->> usage
       (filter (comp (partial datetime-within-n-days? days) c/from-string name first))
       (map second)
       (reduce +)))

(defn resume-views-within-time-interval
  "Retrieve all resume views by a recruiter within a given time interval"
  [rec-id num-days]
  (filter (fn [[_ ts]] (> ts (c/to-long (time/minus (time/now) (time/days num-days)))))
          (i/ri-with-timestamp rec-id :viewed)))

(defn- jobs-url
  "Returns jobs url of given type from config"
  [type]
  (-> env :services :jobs (get type)))

(defn read-job
  "Calls API to fetch job by ID"
  [job-id]
  (->> {:job-id job-id}
       json/generate-string
       (h/post (jobs-url :read))))

(defn applies
  "Compute how many applicants applied to a job"
  [job]
  (->> (read-job (:job_id job))
       :locations
       (mapcat :applicants)))

(defn jobs-with-applicant-range
  "Retrieve all jobs where number of applicants is subject to a given range"
  [jobs applicant-count operator]
  (filter #(when (operator (count (applies %)) applicant-count) %) jobs))

(defn high-performing-jobs
  "Retrieve all jobs where number of applicants is >= 5 and return the top 2"
  [jobs]
  (take 2 (jobs-with-applicant-range jobs 5 >=)))

(defn poor-performing-jobs
  "Retrieve all jobs where number of applicants is < 5 and return the top 2"
  [jobs]
  (take 2 (jobs-with-applicant-range jobs 5 <)))

(defn after?
  "Returns true if first arg is not null and after second arg"
  [a b]
  (let [date-a (f/parse (f/formatters :date-time-no-ms) a)
        date-b (f/parse (f/formatters :date-time-no-ms) b)]
    (if date-a
      (if date-b (t/after? date-a date-b) true)
      false)))

(defn most-recent-job
  "Returns history sorted by start date for experiences list"
  [active-jobs]
  (->> active-jobs
       (sort-by
         #(:publication_date %) after?)
       first))

(defn job-stats
  "Creates the job stats"
  [jobs]
  (let [active-jobs (filter job/active? jobs)]
    {:active        (count active-jobs)
     :expiring-soon (->> (filter job/expiring-within-7-days? jobs)
                         count)
     :all           (count (all-jobs jobs))}))

(defn engagement
  "Creates the engagement stats"
  [applicants views]
  {:applicants {:applicants-this-week  (count (applicants-within-time-interval applicants within-7-days?))
                :applicants-this-month (count (applicants-within-time-interval applicants within-30-days?))}
   :views      {:views-this-week  (count (views-within-time-interval views within-7-days?))
                :views-this-month (count (views-within-time-interval views within-30-days?))}})

(defn applicant-demographics
  "Creates the applicant demographics stats"
  [applicants]
  (->> applicants
       set
       (reduce demo/with-applicant-demographic {})))

(defn usage
  "Creates the usage stats"
  [search-usage rec-id]
  {:searches-this-week      (searches-within-n-days search-usage 7)
   :searches-this-month     (searches-within-n-days search-usage 30)
   :resume-views-this-week  (count (resume-views-within-time-interval rec-id 7))
   :resume-views-this-month (count (resume-views-within-time-interval rec-id 30))})

(defn job-post-performance
  "Creates the job-post-performance stats"
  [active-jobs]
  {:high-performing (high-performing-jobs active-jobs)
   :poor-performing (poor-performing-jobs active-jobs)})

(defn after-date?
  "Returns true if first arg is not null and after second arg"
  [a b]
  (if a
    (if b (t/after? a b) true)
    false))

(defn saved-searches-map
  "Creates the saved-searches stats"
  [saved-searches]
  {:search-total (count saved-searches)
   :searches     (->> saved-searches
                      (sort-by (comp (partial f/parse (f/formatters :date-time)) :update-time) after-date?)
                      (take 2))})

(defn suggested-candidates
  "Creates the suggested candidates stats"
  [active-jobs]
  {:most-recent-job    (most-recent-job active-jobs)
   :top-two-candidates (->> (most-recent-job active-jobs)
                            :job_id
                            suggested/suggested
                            (take 2))})