(ns recruit-app.dashboard.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as rs]
            [cljs.spec.alpha :as s]
            [clojure.string :as cs]
            [recruit-app.specs.account :as spec]
            [recruit-app.util.dashboard :as da]))

(rs/reg-subs "dashboard" [["education-dataset" nil] ["experience-dataset" nil] ["salary-dataset" nil]
                          ["dashboard-data" nil] ["is-fetching?" true] ["team-summary-data" nil]])

(def education-ranges
  "list of education degrees for education chart"
  ["unknown" "bachelors" "masters" "phd"])
(def experience-ranges
  "list of experience ranges for experience chart"
  ["<5" "5-7" "8-10" "11-15" ">15"])
(def salary-ranges
  "list of salary ranges for salary chart"
  ["80-100" "100-150" "150-200" "200-250" "250+"])

(defn experience-range
  "Return experience range data based on range index"
  [experience-dataset [_ index]]
  {:dataset [(-> experience-dataset
                 (:dataset)
                 (get index))]})

(defn most-applicants-excluding-unknown
  [dataset]
  (->> (:dataset dataset)
       (filter #(if (not (= (:label %) "Unknown")) %))
       (apply max-key :applicants)))

(defn most-applicants
  "Return most applicants map. if most applicants map's label is 'Unknown', return the second most one"
  [dataset]
  (let [most (apply max-key :applicants (:dataset dataset))]
    (if (= (:label most) "Unknown")
      (most-applicants-excluding-unknown dataset)
      most)))

(defn most-applicants-label
  "Return most applicants label"
  [most-applicants]
  (:label most-applicants))

(defn most-applicants-percentage
  "Return most applicants percentage math/rounded"
  [most-applicants]
  (-> most-applicants
      (:applicants)
      (Math/round)))

(defn most-applicants-aggregated-percentage
  "Return most applicants plus beyond applicants percentages aggregated and math/rounded"
  [[most-applicants education-dataset] _]
  (let [dataset (:dataset education-dataset)
        start (.indexOf dataset most-applicants)
        aggregative-dataset (subvec dataset start)]
    (->> aggregative-dataset
         (reduce #(+ (:applicants %2) %1)
                 0)
         (Math/round))))

(defn total-applicants
  [applicants-data]
  (reduce + 0 (vals applicants-data)))

(defn average-salary
  "Return average salary math/rounded"
  [{:keys [applicant-demographics]} _]
  (let [total-salary (:total-salary applicant-demographics)
        salary (:salary applicant-demographics)
        total-applicants (total-applicants salary)]
    (-> total-salary
        (/ total-applicants)
        (Math/round))))

(defn capitalise-first-letter
  [string]
  (str (-> string
           (first)
           (cs/upper-case))
       (subs string 1)))

(defn education-dataset
  "Return formatted education dataset excluding degrees that are not present from education-range"
  [{:keys [applicant-demographics]} _]
  (let [education (:education applicant-demographics)
        total-applicants (total-applicants education)
        eudcation-ranges-filtered (filterv #(when ((keyword %) education) %)
                                          education-ranges)]
    (when education
      {:dataset (mapv (fn [d]
                        {:label      (capitalise-first-letter d)
                         :applicants (-> ((keyword d) education)
                                         (or 0)
                                         (/ total-applicants)
                                         (* 100))})
                      eudcation-ranges-filtered)})))

(defn experience-dataset
  "Return formatted experience dataset"
  [{:keys [applicant-demographics]} _]
  (let [experience (:experience applicant-demographics)
        total-applicants (total-applicants experience)]
    (when experience
      {:dataset (mapv (fn [d]
                        {:label      d
                         :applicants (-> ((keyword d) experience)
                                         (or 0)
                                         (/ total-applicants)
                                         (* 100))
                         :average    (da/experience-site-average (keyword d))})
                      experience-ranges)})))

(defn salary-dataset
  "Return formatted salary dataset"
  [{:keys [applicant-demographics]} _]
  (let [salary (:salary applicant-demographics)
        total-applicants (total-applicants salary)]
    (when salary
      {:dataset (mapv (fn [d]
                        {:label      d
                         :applicants (-> ((keyword d) salary)
                                         (or 0)
                                         (/ total-applicants)
                                         (* 100))})
                      salary-ranges)})))

(defn high-performing-jobs
  "Return high performing jobs data"
  [{:keys [job-post-performance]} _]
  (:high-performing job-post-performance))

(defn low-performing-jobs
  "Return low performing jobs data"
  [{:keys [job-post-performance]} _]
  (:poor-performing job-post-performance))

(defn education-site-average
  "Return site average education percentage"
  [most-applicants-education-label _]
  (when most-applicants-education-label
    (da/education-site-average (keyword most-applicants-education-label))))

(defn experience-site-average
  "Return site average experience percentage"
  [most-applicants-experience-label _]
  (when most-applicants-experience-label
    (da/experience-site-average (keyword most-applicants-experience-label))))

(defn saved-searches
  "Return saved-searches data"
  [{:keys [saved-searches-map]} _]
  (:searches saved-searches-map))

(defn has-saved-searches?
  "Return if there is any saved searches"
  [saved-searches _]
  (> (count saved-searches) 0))

(defn job-post-performance
  [{:keys [job-post-performance]} _]
  job-post-performance)

(defn has-job-post-performance?
  "Return if there is any high or low performing jobs"
  [[high-performing low-performing] _]
  (let [has-high (-> high-performing
                     (not-empty))
        has-low (-> low-performing
                    (not-empty))]
    (or has-high has-low)))

(defn job-stats
  "Return job-stats data"
  [{:keys [job-stats]} _]
  job-stats)

(defn engagement
  "Return engagement data"
  [{:keys [engagement]} _]
  engagement)

(defn usage
  "Return usage data"
  [{:keys [usage]} _]
  usage)

(defn suggested-candidates
  [{:keys [suggested-candidates]} _]
  suggested-candidates)

(defn has-applicants?
  "Return true if applicants demographic data is not empty"
  [{:keys [applicant-demographics]} _]
  (not-empty applicant-demographics))

(defn is-highest-degree?
  "Return true if the degree for most applicants is the highest degree (Phd)"
  [most-applicants-education-label _]
  (= most-applicants-education-label "Phd"))

(defn display-data-not-available?
  "Return true if 'Unknown' education degree data is the only data"
  [education-dataset _]
  (let [dataset (:dataset education-dataset)
        first-item (first dataset)]
    (and (= 1 (count dataset)) (= "Unknown" (:label first-item)))))

(defn team-members
  "Return team member data"
  [team-summary-data _]
  (mapv (fn[{:keys [recruiter-name engagement job-stats usage]}]
          [recruiter-name
           (:active job-stats)
           (:expiring-soon job-stats)
           (get-in engagement [:applicants :applicants-this-month])
           (get-in engagement [:views :views-this-month])
           (:searches-this-month usage)
           (:resume-views-this-month usage)])
        team-summary-data))

(rf/reg-sub
  :dashboard/education-dataset
  :<- [:dashboard/dashboard-data]
  education-dataset)

(rf/reg-sub
  :dashboard/experience-dataset
  :<- [:dashboard/dashboard-data]
  experience-dataset)

(rf/reg-sub
  :dashboard/salary-dataset
  :<- [:dashboard/dashboard-data]
  salary-dataset)

(rf/reg-sub
  :dashboard/experience-range
  :<- [:dashboard/experience-dataset]
  experience-range)

(rf/reg-sub
  :dashboard/most-applicants-education
  :<- [:dashboard/education-dataset]
  most-applicants)

(rf/reg-sub
  :dashboard/most-applicants-experience
  :<- [:dashboard/experience-dataset]
  most-applicants)

(rf/reg-sub
  :dashboard/most-applicants-salary
  :<- [:dashboard/salary-dataset]
  most-applicants)

(rf/reg-sub
  :dashboard/most-applicants-education-label
  :<- [:dashboard/most-applicants-education]
  most-applicants-label)

(rf/reg-sub
  :dashboard/most-applicants-education-percentage
  :<- [:dashboard/most-applicants-education]
  :<- [:dashboard/education-dataset]
  most-applicants-aggregated-percentage)

(rf/reg-sub
  :dashboard/most-applicants-experience-label
  :<- [:dashboard/most-applicants-experience]
  most-applicants-label)

(rf/reg-sub
  :dashboard/most-applicants-experience-percentage
  :<- [:dashboard/most-applicants-experience]
  most-applicants-percentage)

(rf/reg-sub
  :dashboard/most-applicants-salary-label
  :<- [:dashboard/most-applicants-salary]
  most-applicants-label)

(rf/reg-sub
  :dashboard/most-applicants-salary-percentage
  :<- [:dashboard/most-applicants-salary]
  most-applicants-percentage)

(rf/reg-sub
  :dashboard/average-salary
  :<- [:dashboard/dashboard-data]
  average-salary)

(rf/reg-sub
  :dashboard/high-performing-jobs
  :<- [:dashboard/dashboard-data]
  high-performing-jobs)

(rf/reg-sub
  :dashboard/low-performing-jobs
  :<- [:dashboard/dashboard-data]
  low-performing-jobs)

(rf/reg-sub
  :dashboard/education-site-average
  :<- [:dashboard/most-applicants-education-label]
  education-site-average)

(rf/reg-sub
  :dashboard/experience-site-average
  :<- [:dashboard/most-applicants-experience-label]
  experience-site-average)

(rf/reg-sub
  :dashboard/saved-searches
  :<- [:dashboard/dashboard-data]
  saved-searches)

(rf/reg-sub
  :dashboard/has-saved-searches?
  :<- [:dashboard/saved-searches]
  has-saved-searches?)

(rf/reg-sub
  :dashboard/job-post-performance
  :<- [:dashboard/dashboard-data]
  job-post-performance)

(rf/reg-sub
  :dashboard/has-job-post-performance?
  :<- [:dashboard/high-performing-jobs]
  :<- [:dashboard/low-performing-jobs]
  has-job-post-performance?)

(rf/reg-sub
  :dashboard/job-stats
  :<- [:dashboard/dashboard-data]
  job-stats)

(rf/reg-sub
  :dashboard/engagement
  :<- [:dashboard/dashboard-data]
  engagement)

(rf/reg-sub
  :dashboard/usage
  :<- [:dashboard/dashboard-data]
  usage)

(rf/reg-sub
  :dashboard/suggested-candidates
  :<- [:dashboard/dashboard-data]
  suggested-candidates)

(rf/reg-sub
  :dashboard/has-applicants?
  :<- [:dashboard/dashboard-data]
  has-applicants?)

(rf/reg-sub
  :dashboard/is-highest-degree?
  :<- [:dashboard/most-applicants-education-label]
  is-highest-degree?)

(rf/reg-sub
  :dashboard/display-data-not-available?
  :<- [:dashboard/education-dataset]
  display-data-not-available?)

(rf/reg-sub
  :dashboard/team-members
  :<- [:dashboard/team-summary-data]
  team-members)