(ns recruit-app.util.job
  (:require [cheshire.core :as json]))

(def job-site-id 68)
(def default-values {:reportsTo ""
                     :jobSiteId job-site-id})

(def default-job-function
  {:jobFunctionId 112
   :name          "Other"
   :webName       nil})

(def default-specialty
  {:specialtyId 191
   :name        "Other Specialties"
   :jobFunction default-job-function})

(def default-segment
  "Job functions are determined by approvers so default segment is used"
  {:jobFunction           default-job-function
   :specialties           [default-specialty]
   :publishedJobFunctions [default-job-function]})

(defn- with-years-experience
  "Format years experience into map and dissoc pieces"
  [{:keys [yearsExperienceId yearsExperienceName] :as params}]
  (-> params
      (assoc :yearsExperience {:id yearsExperienceId :name yearsExperienceName})
      (dissoc :yearsExperienceId :yearsExperienceName)))

(defn- with-compensation
  "Format compensation into map and dissoc pieces"
  [{:keys [compensationTotal compensationSalary compensationBonus compensationOther] :as params}]
  (-> params
      (assoc :compensation {:total  compensationTotal
                            :salary compensationSalary
                            :bonus  compensationBonus
                            :other  compensationOther})
      (dissoc :compensationTotal :compensationSalary :compensationBonus :compensationOther)))

(defn- with-segment
  "Format segment into map and dissoc pieces"
  [{:keys [hiringCompanyIndustryId hiringCompanyIndustryName
           specialtyId specialtyName title fullDescription] :as params}]
  (-> params
      (assoc :industry {:name hiringCompanyIndustryName})
      (assoc :segment default-segment)
      (dissoc :hiringCompanyIndustryId :hiringCompanyIndustryName :specialtyId :specialtyName)))

(defn- with-company-size
  "Format company size into map and dissoc pieces"
  [{:keys [hiringCompanySizeId hiringCompanySizeDescription] :as params}]
  (-> params
      (assoc :size {:id hiringCompanySizeId :description hiringCompanySizeDescription})
      (dissoc :hiringCompanySizeId :hiringCompanySizeDescription)))

(defn- with-company
  "Format company into map and dissoc pieces"
  [{:keys [hiringCompanyName industry size] :as params}]
  (-> params
      (assoc :company {:name hiringCompanyName :industry industry :size size})
      (dissoc :hiringCompanyName :industry :size)))

(defn- with-application
  "Format application into map and dissoc pieces"
  [{:keys [applicationName] :as params}]
  (-> params
      (assoc :application {:email applicationName})
      (dissoc :applicationName)))

(defn- formatted-location
  "Returns formatted location for job post"
  [{:keys [text type]}]
  {:text   text
   :active true
   :type   (case type
             "bySpecificLocation" "SPECIFIC_LOCATION"
             "byRegionProvince" "REGION"
             "virtualTravel" "VIRTUAL_TRAVEL")})

(defn- with-formatted-locations
  "Formats locations to conform to job request"
  [params]
  (update params :locations (partial map formatted-location)))

(defn- with-defaults
  "Adds default values to job request"
  [params]
  (merge default-values params))

(defn job-request
  "Returns map of job info for job service request"
  [params]
  (-> params
      with-years-experience
      with-compensation
      with-segment
      with-company-size
      with-company
      with-application
      with-formatted-locations
      with-defaults
      (->> (hash-map :job))))

(defn post-job-request
  [params]
  (-> params
      job-request
      (->> (hash-map :jobChangeRequest))
      (assoc-in [:jobChangeRequest :jobSiteId] job-site-id)
      (assoc-in [:jobChangeRequest :requester] {:id 200 :typeId 1})))

(defn recruiter-job
  "Returns job if it matches recruiter-id or user is a superuser, nil otherwise"
  [recruiter-id {:keys [recruiter_id] :as job}]
  (when (= recruiter-id recruiter_id) job))
