(ns recruit-app.util.job
  (:require [cljs.spec.alpha :as s]
            [recruit-app.post-job.db :as pj-db]
            [clojure.string :as string]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.number_format :as nf]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.string :as gs]
            [clojure.set :refer [union]]
            [recruit-app.util.date :as d]
            [recruit-app.util.sort :as sort]))

(defn comp-with-bonus
  "Calculate the total comp from base-comp and bonus for db"
  [base-comp bonus]
  (Math/round
    (cond
      (string/includes? bonus "%") (* base-comp (+ 1 (* (/ 1 100) (string/replace bonus "%" ""))))
      (string/includes? bonus "$") (+ base-comp (* (int (string/replace (string/replace bonus "$" "") "K" ""))))
      :else (* base-comp (+ 1 (* (/ 1 100) (int bonus)))))))

(defn comp-with-bonus-post
  "Calculate the total comp from base-comp and bonus with a display of thousands for preview page"
  [base-comp bonus denom]
  (Math/round
    (cond
      (string/includes? bonus "%") (* base-comp (+ 1 (* (/ 1 100) (string/replace bonus "%" ""))) 1000)
      (string/includes? bonus "$") (+ (* base-comp 1000) (* (int (string/replace (string/replace bonus "K" "") "$" "")) 1000))
      :else (* (* base-comp (+ 1 (* (/ 1 100) (int bonus))) 1000)))))

(defn display-bonus
  "Format a % then display bonus unformatted
   -- If bonus is empty display default if supplied or empty string"
  [bonus default]
  (cond
    (string/includes? bonus "%") bonus
    (< 0 (int bonus)) (str bonus)
    (and (string/includes? bonus "$") (string/includes? bonus "K")) bonus
    (string/includes? bonus "$") (str (nf/remove-cents (nf/convert-currency? (nf/currency-format (Math/floor (string/replace bonus "$" ""))))) "K")
    :else default))

(defn display-dollars
  "Returns string formatted as dollars"
  [num]
  (str (nf/remove-cents (nf/number-conversion num))))

(defn salary-preview-string
  "Returns string of salary given min and max"
  [min-comp max-comp]
  (str (if (zero? min-comp) (str (display-dollars 40) "K") (str (display-dollars min-comp) "K")) " - " (if (zero? max-comp) (str (display-dollars min-comp) "K") (str (display-dollars max-comp) "K"))))

(defn base-salary-preview-string
  "Returns string of salary given min and max"
  [min-comp max-comp]
  (str (if (zero? min-comp) (display-dollars 40) (display-dollars min-comp)) " - " (if (zero? max-comp) (display-dollars min-comp) (display-dollars max-comp))))

(defn format-bonus
  "Formats bonus display and returns as a string"
  [bonus denom-string]
  (cond
    (string/includes? bonus "K") bonus
    (= denom-string "%") (str bonus denom-string)
    (= denom-string "$") (str denom-string bonus "K")))

(defn salary-details-string
  "Returns string of salary details (e.g. base, bonus, other)"
  [{:keys [min-comp max-comp bonus denom other]}]
  (let [bonus (display-bonus bonus "")
        base (salary-preview-string min-comp max-comp)]
    (cond
      (and (seq bonus) (seq other)) (str " (" base " base + " (format-bonus bonus denom) ", " other ")")
      (seq bonus) (str " (" base " base + " (format-bonus bonus denom) ")")
      (seq other) (str " (" other ")")
      :else "")))

(defn salary-range-min
  "Convert min salary to salary band min"
  [min-comp]
  (cond (> min-comp 250) 250000
        (> min-comp 200) 200000
        (> min-comp 150) 150000
        (> min-comp 100) 100000
        (> min-comp 80) 80000
        (> min-comp 60) 60000
        :else 0))

(defn salary-range-max
  "Convert max salary to salary band max"
  [max-comp]
  (cond (> max-comp 250) 2147483647
        (> max-comp 200) 250000
        (> max-comp 150) 200000
        (> max-comp 100) 150000
        (> max-comp 80) 100000
        (> max-comp 60) 80000
        :else 60000))

(defn company-size
  "Convert company size id to name"
  [size-id]
  (case size-id
    1 "1-100"
    2 "101-1,000"
    3 "1,001-10,000"
    4 "10,000+"
    ""))

(defn exp-name
  "Convert experience id to name"
  [exp]
  (case exp
    6 "Not Specified"
    1 "Less than 5"
    2 "5-7"
    3 "8-10"
    4 "11-15"
    5 "15+"
    "Not Specified"))

(defn industry-name
  "Convert industry id to name"
  [id]
  (some #(when (= (:id %) id) (:label %)) (dd/industry)))

(defn full-job
  "Returns true if all required fields for a job are valid"
  [job]
  (and (s/valid? ::pj-db/job.title (:job-title job))
       (s/valid? ::pj-db/fullDescription (:job-desc job))
       (s/valid? ::pj-db/salary (str (:min-comp job)))
       (s/valid? ::pj-db/salary (str (:max-comp job)))
       (<= (:min-comp job) (:max-comp job))
       (>= (comp-with-bonus (:max-comp job) (:bonus job "")) 80)
       (or (s/valid? ::pj-db/company.name (:company job))
           (:hide-company job))
       (s/valid? ::pj-db/id (:industry job))
       (s/valid? ::pj-db/id (:employee job))
       (s/valid? ::pj-db/locations (:locations job))))

(defn post-job-request
  [{:keys [job-id job-title job-desc locations hide-recruiter hide-company hide-salary company employee exp industry
           min-comp max-comp bonus denom other]
    :or   {job-title "" denom "%" bonus "" other "" exp 6}}
   {:keys [recruiter-id email]}]
  (let [company (if (or hide-company (not company)) "Confidential Company" company)
        total-comp (str "$" (comp-with-bonus min-comp bonus) "K to " (comp-with-bonus max-comp bonus) "K")]
    {:jobStatus                    nil
     :jobFunctionId                ""
     :hiringCompanyIndustryName    (industry-name industry)
     :yearsExperienceId            exp
     :targetSalaryRangeMinimum     (salary-range-min min-comp)
     :hiringCompanyName            (gs/unescapeEntities company)
     :autoRepost                   true
     :linkToRecruiterCompany       true
     :reposted                     false
     :applicationName              email
     :hiringCompanySizeDescription (company-size employee)
     :hiringCompanyIndustryId      industry
     :compensationSalary           (if hide-salary "--" (str "$" min-comp "K to $" max-comp "K"))
     :targetHiringAlertMessage     ""
     :title                        job-title
     :hiringCompanySizeId          employee
     :locations                    (mapv (fn [location] {:text (:name location)
                                                         :type "bySpecificLocation"}) locations)
     :targetHiringAlertEnabled     false
     :fullDescription              job-desc
     :exclusiveToLadders           false
     :targetSalaryRangeMaximum     (salary-range-max max-comp)
     :compensationOther            other
     :yearsExperienceName          (exp-name exp)
     :specialtyName                ""
     :specialtyId                  ""
     :jobFunctionName              ""
     :jobPrivacy                   {:recruiterAnonymous      hide-recruiter
                                    :companyNameConfidential hide-company}
     :jobId                        job-id
     :compensationBonus            (if (= denom "$") (str denom bonus "K") bonus)
     :compensationTotal            (if hide-salary "--" total-comp)
     :recruiterId                  (js/parseInt recruiter-id)
     :promoted                     false}))

(defn job-location-index
  "Searches jobs vector for job with given id and returns index"
  [locations job-location-id]
  (->> locations
       (keep-indexed (fn [idx location] (when (= job-location-id (:job_location_id location)) idx)))
       first))

(defn flatten-js-ids
  [location]
  (-> location
      (update :views (comp (partial into []) (partial map :jobSeekerId) set))
      (update :applicants (comp (partial into []) (partial map :jobSeekerId) set))))

(defn sanitize-job
  "Iterates through locations and flattens ids to vector"
  [job]
  (update job :locations (partial map flatten-js-ids)))

(defn unique-jobseekers
  [location]
  (-> location
      (update :views (comp (partial into []) (partial remove #(nil? (:jobSeekerId %))) set))
      (update :applicants (comp (partial into []) (partial remove #(nil? (:jobSeekerId %))) set))))

(defn with-unique-jobseekers
  "Updates views and applicants for job to unique set"
  [job]
  (update job :locations (partial map unique-jobseekers)))

(defn add-or-remove
  "Checks if id is in collection and adds or removes"
  [id coll]
  (if (contains? coll id)
    (->> coll (remove #{id}) set)
    (conj coll id)))

(defn update-checked-candidates
  "Adds or removes jobseeker from coll. Returns new set if nil."
  [jobseeker candidates]
  (if candidates
    (add-or-remove jobseeker candidates)
    #{jobseeker}))

(defn add-to-set
  "Returns set with given value added. If coll is nil, returns set with just given value."
  [coll val]
  (if coll (conj coll val) #{val}))

(defn datestring->long
  [date-string]
  (c/to-long (f/parse (f/formatters :date-time-no-ms) date-string)))

(defn current-promotion
  "Returns current active promotion from featured vector"
  [featured]
  (let [active-features (filter :is_feature featured)]
    (when (not-empty active-features)
      (apply max-key #(datestring->long (:end_date %)) active-features))))

(defn formatted-location
  "Formats locations for preview"
  [{:keys [location] :as full-location}]
  (assoc full-location :name location))

(def salary-regex #"(&#x24;|\$)?(\d+)k?")

(defn last-int-value
  "Returns int value of last member of sequence"
  [matches]
  (-> matches
      last
      js/parseInt))

(defn target-salary
  "Parses compensation string into min and max comp"
  [comp-string]
  (when comp-string
    (->> comp-string
         (re-seq salary-regex)
         (map last-int-value)
         (zipmap [:min-comp :max-comp]))))

(defn post-job-format
  "Returns job formatted for preview"
  [{:keys [job_id title full_desc company_name compensation_salary locations
           compensation_bonus compensation_other is_confidential is_anonymous years_experience
           work_experience_id industry_id company_size_id pending-promoted featured job_status
           publication_date entry_date]
    :or   {compensation_bonus "" compensation_other ""}}]
  (let [compensation_bonus (gs/unescapeEntities compensation_bonus)
        compensation_other (gs/unescapeEntities compensation_other)
        compensation_bonus_denom (cond (string/includes? compensation_bonus "%") "%"
                                       (string/includes? compensation_bonus "$") "$"
                                       :else "%")
        {:keys [min-comp max-comp]} (target-salary compensation_salary)
        min-total-comp (comp-with-bonus-post min-comp compensation_bonus compensation_bonus_denom)
        max-total-comp (comp-with-bonus-post (if (nil? max-comp) min-comp max-comp) compensation_bonus compensation_bonus_denom)]
    {:job-id         job_id
     :job-title      title
     :job-desc       full_desc
     :locations      (map formatted-location locations)
     :company        company_name
     :hide-company   is_confidential
     :hide-recruiter is_anonymous
     :hide-salary    (= "--" compensation_salary)
     :employee       company_size_id
     :exp            (or work_experience_id 6)
     :min-comp       (or min-comp 0)
     :max-comp       (or max-comp min-comp)
     :industry       industry_id
     :min-total-comp min-total-comp
     :max-total-comp max-total-comp
     :exp-string     years_experience
     :bonus          compensation_bonus
     :other          compensation_other
     :posted_date    (or publication_date entry_date)
     :promoted?      (cond (= job_status "Pending") pending-promoted
                           (= job_status "Approved") (-> featured current-promotion not-empty)
                           :else false)}))

(defn candidate-record
  "Formats jobseeker information and returns candidate record"
  [application-status
   {:keys [jobSeekerId jobSeekerDesired jobSeekerFirstName jobSeekerLastName secure-id
           location apply_time jobSeekerEducation jobSeekerHistory lastEmailOpen
           lastResumeUpdate lastLogin lastProfileUpdate jobSeekerWorkExperience] :as candidate}]
  {:id                  jobSeekerId
   :secure-id           secure-id
   :desired             jobSeekerDesired
   :first-name          jobSeekerFirstName
   :last-name           jobSeekerLastName
   :location            location
   :apply-time          (when apply_time (f/parse (f/formatters :date-time-no-ms) apply_time))
   :education           jobSeekerEducation
   :history             jobSeekerHistory
   :years-experience    (:years jobSeekerWorkExperience)
   :application-history (get application-status jobSeekerId [])
   :last-email-open     lastEmailOpen
   :last-resume-update  lastResumeUpdate
   :last-login          lastLogin
   :last-profile-update lastProfileUpdate})

(defn active-job-location
  "Returns job location by id from job"
  [locations job-loc-id]
  (->> locations
       (filter #(= (:job_location_id %) job-loc-id))
       first))

(defn active-location
  "Returns active job location or first location"
  [locations active-job-loc-id]
  (if active-job-loc-id
    (active-job-location locations active-job-loc-id)
    (first locations)))

(defn apply-time
  "Destructures applicant to get apply time and formats to obj"
  [{:keys [apply_time]}]
  (when apply_time
    (f/parse (f/formatters :date-time-no-ms) apply_time)))

(defn applicant-sort-fns
  "Returns tuple of sort-fn and comparator"
  [sort-col sort-order]
  (let [time-comp (sort/time-comparator sort-order)]
    (condp = sort-col
      :apply-time [apply-time time-comp]
      [apply-time time-comp])))

(defn sorted-applicants
  "Returns job location applicants sorted by given col and order"
  [location sort-col sort-order]
  (let [[sort-fn comp] (applicant-sort-fns sort-col sort-order)]
    (->> location
         :applicants
         flatten
         (sort-by sort-fn comp))))

(defn candidate-indices
  "Returns collection with index of candidate with given secure-id"
  [candidates secure-id]
  (keep-indexed
    (fn [idx candidate] (when (= secure-id (:secure-id candidate)) idx))
    candidates))

(defn job-location-views
  "Returns flattened viewers for job location"
  [location]
  (-> location :views flatten))

(defn location-candidates
  "Returns either applicants or viewers for job location"
  [{:keys [applicants views]} tab]
  (case tab
    :tab1 applicants
    :tab2 views))

(defn candidate-index
  "Returns active job candidate based on active tab and id"
  [location tab secure-id]
  (-> location
      (location-candidates tab)
      (candidate-indices secure-id)
      first))

(defn tab-id
  "Returns candidate_tab_id according to active tab"
  [tab]
  (case tab
    :tab1 2
    :tab2 5))

(defn application-keyed-by-jsid
  "Returns application status keyed by jobseeker-id"
  [application-status]
  (reduce #(update %1 (:jobseeker_id %2) conj %2) {} application-status))

(defn dismissed-entry
  "Returns entry for dismissal in jobseeker_application_status"
  [jobseeker-id]
  {:apply_status        "Dismissed"
   :jobseeker_id        jobseeker-id
   :job_apply_action_id 2})

(defn updated-job-location
  "Returns job location with updated application status to dismiss jobseeker"
  [locations jobseeker-id job-location-id]
  (let [locations (into [] locations)]
    (update-in
      locations
      [(job-location-index locations job-location-id) :jobseeker_application_status]
      conj
      (dismissed-entry jobseeker-id))))

(defn dismiss-candidate
  "Update job location in given jobs to dismiss candidate"
  [jobs jobseeker-id job-id job-location-id]
  (update-in
    jobs
    [job-id :locations]
    updated-job-location
    jobseeker-id
    job-location-id))

(defn posted-date
  "Returns either publication or entry date as DateTime object"
  [{:keys [publication_date entry_date]}]
  (d/timestamp (or publication_date entry_date)))

(defn jobseeker-count
  "Returns count of given key in job location.
  Main use for this is to count applicants or views"
  [location key]
  (-> location (get key) set count))

(defn total-jobseeker-count
  "Returns total jobseekers in given key in all job locations"
  [locations key]
  (reduce #(+ %1 (jobseeker-count %2 key)) 0 locations))

(defn- unparsed-date
  "Returns date string from datetime object when given"
  [datetime]
  (if datetime
    (f/unparse (f/formatter "MM/dd/yy") datetime)
    ""))

(defn posted-str
  "Returns string of posted date for job"
  [job]
  (->> job
       posted-date
       unparsed-date))

(defn expire-time
  "Returns expiration datetime object for job"
  [job]
  (when-let [pub-time (posted-date job)]
    (t/plus pub-time (t/days 56))))

(defn expire-string
  "Returns string of expiration date for job"
  [job]
  (->> job
       expire-time
       unparsed-date))

(defn- location
  "Returns formatted location from job service response.
  Note: Applicants/views will start empty, if this is an updated job, the
  applicants/views will be merged from existing db"
  [{:keys [id text]}]
  {:job_location_id id
   :location        text
   :applicants      []
   :views           []})

(def job-status-map
  "Map of job service statuses to corresponding id and status names"
  {"PENDING"   {:id   "P"
                :name "Pending"}
   "APPROVED"  {:id   "A"
                :name "Approved"}
   "CANCELLED" {:id   "C"
                :name "Cancelled"}
   "FILLED"    {:id   "F"
                :name "Filled"}
   "REJECTED"  {:id   "R"
                :name "Rejected"}
   "DELETED"   {:id   "D"
                :name "Deleted"}
   "ACTIVE"    {:id   "A"
                :name "Approved"}
   "EXPIRED"   {:id   "A"
                :name "Approved"}})

(defn formatted-job
  "Converts response from job service to proper job format"
  [{:keys [fullDescription jobStatus jobId compensation company title createDate
           locations recruiterId jobPrivacy yearsExperience targetSalaryRange
           segment]}]
  {:full_desc           fullDescription
   :job_id              jobId
   :compensation_salary (:salary compensation)
   :compensation_bonus  (:bonus compensation)
   :compensation_other  (:other compensation)
   :company_name        (:name company)
   :title               title
   :entry_date          createDate
   :locations           (map location locations)
   :job_status_id       (:id (get job-status-map jobStatus))
   :job_status          (:name (get job-status-map jobStatus))
   :recruiter_id        recruiterId
   :is_confidential     (:companyNameConfidential jobPrivacy)
   :is_anonymous        (:recruiterAnonymous jobPrivacy)
   :years_experience    (:name yearsExperience)
   :work_experience_id  (:id yearsExperience)
   :salary_band_upper   (:maximum targetSalaryRange)
   :salary_band_lower   (:minimum targetSalaryRange)
   :industry_id         (-> segment :jobFunction :jobFunctionId)
   :company_size_id     (-> company :size :id)
   :featured            []})
