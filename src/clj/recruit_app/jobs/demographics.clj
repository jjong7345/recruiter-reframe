(ns recruit-app.jobs.demographics
  (:require [clojure.string :as string]))

(def education-degrees
  "Map of education degrees to proper key"
  {"None"            :unknown
   "H.S. Diploma"    :unknown
   "Associates"      :unknown
   "BA"              :bachelors
   "BS"              :bachelors
   "Other Bachelors" :bachelors
   "MA"              :masters
   "MS"              :masters
   "MBA"             :masters
   "Other Masters"   :masters
   "PhD"             :phd
   "PsyD"            :phd
   "EdD"             :phd
   "MD"              :phd
   "J.D."            :phd
   "Other Doctorate" :phd})

(def experience-map
  "Map of experience values to keys"
  {"Less than 5" :<5
   "5 - 7"       :5-7
   "8 - 10"      :8-10
   "11 - 15"     :11-15
   "15+"         :>15})

(defn- salary-integer
  "Cleans desired string to remove $ and k+"
  [desired]
  (when (not (string/blank? desired))
    (Integer. (string/replace desired #"[\$kK\+]" ""))))

(defn- salary-key
  "Returns appropriate salary band key given desired salary for applicant"
  [desired]
  (if-let [salary (salary-integer desired)]
    (cond
      (< salary 80) :<80
      (< salary 100) :80-100
      (< salary 150) :100-150
      (< salary 200) :150-200
      (< salary 250) :200-250
      :else :>250)
    :unknown))

(defn- with-salary-statistics
  "Adds salary statistic for applicant to salary map"
  [salary-statistics salary]
  (update salary-statistics (salary-key salary) (fnil inc 0)))

(defn- highest-degree
  "Returns name of highest degree given education history"
  [education]
  (let [education (remove (comp nil? :education_degree_id) education)]
    (when-not (empty? education)
      (:education_degree_name (apply max-key :education_degree_id education)))))

(defn- education-key
  "Returns appropriate education key given education history of applicant"
  [education]
  (get education-degrees (highest-degree education) :unknown))

(defn- with-education-statistics
  "Adds education statistic for applicant to education map"
  [education-statistics education]
  (update education-statistics (education-key education) (fnil inc 0)))

(defn- experience-key
  "Returns proper key based on string value of years worked"
  [years]
  (get experience-map years :unknown))

(defn- with-experience-statistics
  "Adds experience statistic for applicant to experience map"
  [experience-statistics {:keys [years]}]
  (update experience-statistics (experience-key years) (fnil inc 0)))

(defn with-applicant-demographic
  "Adds stats for given applicant to map of demographics"
  [demographics
   {:keys [jobSeekerWorkExperience jobSeekerDesired jobSeekerEducation]}]
  (-> demographics
      (update :total (fnil inc 0))
      (update :total-salary (fnil + 0 0) (salary-integer jobSeekerDesired))
      (update :salary with-salary-statistics jobSeekerDesired)
      (update :education with-education-statistics jobSeekerEducation)
      (update :experience with-experience-statistics jobSeekerWorkExperience)))
