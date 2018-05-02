(ns recruit-app.search.core
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [clojure.string :as str]
            [recruit-app.util.encryption :as e]
            [config.core :refer [env]]
            [recruit-app.search.util :as util]
            [recruit-app.util.http :as h]
            [clojure.set :refer [rename-keys]]
            [recruit-app.taxonomy.api :as tax]
            [clj-time.coerce :as c]
            [taoensso.timbre :as log]
            [org.purefn.irulan.view :as view]
            [ladders-domains.recruiter.candidate-search :as search]
            [hal.uuid :as uuid]
            [recruit-app.kafka.api :as kafka]))

(def paths [[:criteria :search-criteria :title]
            [:criteria :search-criteria :location]
            [:criteria :search-criteria :radius]
            [:criteria :search-criteria :min-degree-category-id]
            [:criteria :search-criteria :work-experience-ids]
            [:criteria :search-criteria :company]
            [:criteria :search-criteria :keyword]
            [:criteria :search-criteria :salary-min]
            [:criteria :search-criteria :salary-max]
            [:criteria :search-criteria :candidate-name]
            [:criteria :search-criteria :school]
            [:criteria :search-criteria :discipline-ids]
            [:criteria :search-parameters :only-last-title?]
            [:criteria :search-parameters :only-last-company?]
            [:criteria :search-parameters :include-desired-location?]
            [:criteria :search-parameters :include-candidates-never-contacted?]
            [:criteria :search-parameters :include-candidates-contacted?]
            [:criteria :search-parameters :include-candidates-never-viewed?]
            [:criteria :search-parameters :include-candidates-viewed?]
            [:limit]
            [:offset]
            [:sort-by]])

(def search-keys [:title
                  :geo_location
                  :radius
                  :highest_degree
                  :experience_list
                  :company
                  :skills
                  :salary_lower
                  :salary_upper
                  :fullname
                  :school
                  :specialties
                  :last_title_only
                  :last_company_only
                  :include_desired_location
                  :never-contacted?
                  :contacted?
                  :never-viewed?
                  :viewed?
                  :size
                  :from
                  :sort_by])

(defn- map-when
  "Function which given a map m of key/value pairs renames the keys, specified by the 'path' argument to those listed in vector specified as 'k'"
  [m path k]
  (let [val (get-in m path)]
    (when (not-empty (str val)) {k val})))

(defn base-es-params
  "Function which creates the ES search request payload as expected by Lambda ES Endpoint"
  [req external-search?]
  (reduce merge {:is_searchable      "true"
                 :function_field     :last-login
                 :is_external_search external-search?
                 :size               10}
          (map #(map-when req %1 %2) paths search-keys)))

(defn- format-experience
  "Function which formats the experience section of the Jobseeker model"
  [{:keys [company title start-date end-date description]}]
  {:updateTime        nil
   :createTime        nil
   :creatorId         nil
   :updaterId         nil
   :id                -1
   :profileId         -1
   :companyName       company
   :title             title
   :privateCompany    false
   :industry          nil
   :jobFunction       nil
   :compensationRank  nil
   :companySize       nil
   :startDate         (-> start-date
                          util/parsed-date
                          c/to-long)
   :endDate           (-> end-date
                          util/parsed-date
                          c/to-long)
   :description       description
   :status            nil
   :presentExperience (nil? end-date)
   :insertTime        nil})

(defn- format-education
  "Function which formats the education section of the Jobseeker model"
  [{:keys [field degree-id degree-category-id degree-name school]}]
  {:updateTime      nil
   :createTime      nil
   :creatorId       nil
   :updaterId       nil
   :id              -1
   :profileId       -1
   :educationDegree {:id               degree-id
                     :degreeCategoryId degree-category-id
                     :name             degree-name
                     :description      field
                     :createTime       nil
                     :creatorId        nil
                     :updateTime       nil
                     :updaterId        nil
                     :active           true}
   :school          school
   :fieldOfStudy    field
   :degreeDate      nil
   :activities      nil
   :status          nil
   :insertTime      nil})

(defn- format-candidate
  "Function which formats the candidate section of the Jobseeker model"
  [{:keys [jobseeker]} viewed contacted]
  (let [desired-locations (get-in jobseeker [:job-preferences :desired-locations])
        candidate-name (str (:firstname jobseeker) " " (:lastname jobseeker))
        location (:location (get desired-locations 0))
        radius (:radius (get desired-locations 0))
        city (:city location)
        state (:state location)
        zip (:zipcode location)
        compensation-description (get-in jobseeker [:current-compensation :description])
        compensation-value (get-in jobseeker [:current-compensation :lower-limit])
        jobseeker-id (:jobseeker-id jobseeker)
        is-premium (:premium (:subscription jobseeker))
        highest-degree-id (:highest-degree-id (:education jobseeker))]
    {:jobseekerId                     jobseeker-id
     :showAsBasicJobseeker            (not is-premium)
     :staticCopyForEntireCandidateRow nil
     :candidateName                   candidate-name
     :location                        (str city ", " state)
     :compensation                    compensation-description
     :compensationValue               compensation-value
     :jobseekerDisplayState           "FULL"
     :showSubscriberId                false
     :interactionMenuWidgetType       "CONNECT_WITH_MENU"
     :showActionsButton               true
     :hasConnectionPrivilege          true
     :profile                         {:updateTime              (:last-profile-update jobseeker)
                                       :createTime              (:profile-create-time jobseeker)
                                       :creatorId               nil
                                       :updaterId               nil
                                       :id                      -1
                                       :subscriberId            jobseeker-id
                                       :firstName               (:firstname jobseeker)
                                       :lastName                (:lastname jobseeker)
                                       :confidential            false
                                       :travelPreference        nil
                                       :location                nil
                                       :targetLocations         [{:updateTime nil
                                                                  :createTime nil
                                                                  :creatorId  nil
                                                                  :updaterId  nil
                                                                  :id         nil
                                                                  :profileId  nil
                                                                  :location   (str city ", " state)
                                                                  :region     nil
                                                                  :country    nil
                                                                  :zipCode    nil
                                                                  :commutable false
                                                                  :radius     radius
                                                                  :insertTime nil}]
                                       :workExperienceId        (get-in jobseeker [:job-preferences :years-of-experience-id])
                                       :workAuthorizationId     (:work-authorization-id jobseeker)
                                       :securityClearanceId     (:security-clearance-id jobseeker)
                                       :city                    (:city location)
                                       :state                   (:state location)
                                       :zipCode                 {:zipId          -1
                                                                 :zipCode        zip
                                                                 :neighborhood   nil
                                                                 :cityName       city
                                                                 :stateName      state
                                                                 :country        nil
                                                                 :location       nil
                                                                 :region         nil
                                                                 :timezoneOffset nil
                                                                 :known          true
                                                                 :unknown        false}
                                       :status                  (str/upper-case (:profile-status jobseeker))
                                       :visibility              (:profile-visibility jobseeker)
                                       :searchable              true
                                       :isPremium               is-premium
                                       :experience              (vec (util/sorted-history (map #(format-experience %) (get-in jobseeker [:experience :experiences]))))
                                       :education               (vec (map #(format-education %) (get-in jobseeker [:education :educations])))
                                       :resumeId                -1
                                       :contactInfoConfidential false
                                       :fullName                candidate-name
                                       :premium                 is-premium
                                       :anyCompanyConfidential  false
                                       :privileges              ["CONNECTION"]
                                       :mostRecentCompensation  nil
                                       :insertTime              (:profile-create-time jobseeker)}
     :recruiterInteractions           {:profileNote                 nil
                                       :declinedDate                nil
                                       :viewed                      (contains? viewed jobseeker-id)
                                       :saved                       nil
                                       :applied                     nil
                                       :clipboard                   nil
                                       :recruiterContactedJobSeeker (contains? contacted jobseeker-id)
                                       :jobSeekerContactedRecruiter false
                                       :downloadableResume          true
                                       :showProfileBlocker          false
                                       :showAllInteractionOptions   true
                                       :dismissStatus               nil
                                       :warmCandidate               false
                                       :profileNoteSaved            false}

     :subscriptionType                nil
     :showSubscriptionType            false
     :jobLikeDate                     nil
     :showSearchability               false
     :secureId                        (e/encrypt-subscriberid jobseeker-id)
     :school                          (->> (get-in jobseeker [:education :educations])
                                           (into {} (filter #(= highest-degree-id (:degree-id %))))
                                           :school)
     :hasResume                       false
     :educationDegreeId               (get-in jobseeker [:education :highest-degree-id])
     :displayableIntent               nil
     :candidateNameClickable          true
     :jobLiked                        false
     :lastEmailOpen                   (:last-email-open jobseeker)
     :lastResumeUpdate                (:last-resume-update jobseeker)
     :lastLogin                       (:last-login jobseeker)}))

(defn format-search-request
  "Function which formats the ES search request and transforms some of the data fields"
  [req viewed contacted external-search?]
  (let [request {:config-name :jobseeker
                 :config-ns   :jobseeker-search
                 :es-params   (-> req
                                  (base-es-params external-search?)
                                  (util/filters viewed contacted)
                                  util/split-fullname
                                  util/parse-location
                                  util/clean-text-fields
                                  util/last-company-check
                                  util/last-title-check)}]
    request))

(defn search
  "Function to format the candidates retrieved from a search"
  [search-body savedsearch-id viewed contacted]
  (let [viewed (set viewed)
        contacted (set contacted)
        {:keys [total-hits jobseekers]} (->> search-body
                                             json/generate-string
                                             (http/post (-> env :services :search :es)))]
    {:total   total-hits
     :results (map #(format-candidate % viewed contacted) jobseekers)}))

(defn usage
  "Returns map of search counts by day for given recruiter"
  [recruiter-id]
  (kafka/view
    {::view/view-type ::search/recruiter-search-usage-view
     ::view/key       (uuid/type-6 "subscriber" recruiter-id)}))
