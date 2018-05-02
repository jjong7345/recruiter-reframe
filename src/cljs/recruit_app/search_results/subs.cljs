(ns recruit-app.search-results.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.member :as member]
            [clojure.set :refer [subset?]]
            [recruit-app.components.table :as table]
            [recruit-app.search.criteria :as criteria]))

(subs/reg-subs "search-results" [["saved-search-loaded?" false]
                                 ["saved-search-id" nil]
                                 ["selected-filter" nil]
                                 ["criteria" nil]
                                 ["sort-by" criteria/sort-by-default]
                                 ["page" 0]
                                 ["show-minimum-criteria-error?" false]])
(subs/reg-subs "search-results" "criteria" [["search-criteria" {}]
                                            ["search-name" ""]])
(subs/reg-subs
  "search-results"
  "criteria"
  "search-criteria"
  [["salary-min" criteria/salary-min-default]
   ["salary-max" criteria/salary-max-default]
   ["company" ""]
   ["school" ""]
   ["candidate-name" ""]
   ["title" ""]
   ["keyword" ""]
   ["min-degree-category-id" criteria/min-degree-default]
   ["work-experience-ids" criteria/work-experience-ids-default]
   ["discipline-ids" criteria/discipline-ids-default]])
(subs/reg-subs
  "search-results"
  "criteria"
  "search-parameters"
  [["include-candidates-viewed?" true]
   ["include-candidates-contacted?" true]
   ["include-candidates-never-viewed?" true]
   ["include-candidates-never-contacted?" true]])

(defn search-params
  "Returns search params given search criteria"
  [[criteria sort-by]]
  (when criteria
    (criteria/search-request-params criteria sort-by)))

(defn selected-filter?
  "Returns whether given filter is equal to selected filter"
  [selected [_ filter]]
  (= selected filter))

(defn keywords-filter-active?
  "Returns whether keyword is blank in search criteria"
  [{:keys [keyword]}]
  (seq keyword))

(defn location-filter-active?
  "Returns whether location is blank in search criteria"
  [{:keys [location]}]
  (seq location))

(defn profile-filter-active?
  "Returns whether one of title/skills/candidate-name is blank"
  [{:keys [title skills discipline-ids candidate-name]}]
  (or (seq title)
      (seq skills)
      (and (not-empty discipline-ids)
           (not= discipline-ids criteria/discipline-ids-default))
      (seq candidate-name)))

(defn salary-filter-active?
  "Returns whether min or max salary is set"
  [{:keys [salary-min salary-max]
    :or   {salary-min criteria/salary-min-default
           salary-max criteria/salary-max-default}}]
  (not (and (= criteria/salary-min-default salary-min)
            (= criteria/salary-max-default salary-max))))

(defn experience-filter-active?
  "Returns whether work experience is not empty or company is not blank"
  [{:keys [work-experience-ids company]
    :or   {work-experience-ids criteria/work-experience-ids-default}}]
  (or (not= (set work-experience-ids) (set criteria/work-experience-ids-default))
      (seq company)))

(defn education-filter-active?
  "Returns whether school is not blank or a minimum degree is set"
  [{:keys [school min-degree-category-id]
    :or   {min-degree-category-id criteria/min-degree-default}}]
  (or (seq school) (not= criteria/min-degree-default min-degree-category-id)))

(defn active-filter?
  "Returns whether filter has active values"
  [search-criteria [_ filter-key]]
  (case filter-key
    :keywords (keywords-filter-active? search-criteria)
    :location (location-filter-active? search-criteria)
    :profile (profile-filter-active? search-criteria)
    :salary (salary-filter-active? search-criteria)
    :experience (experience-filter-active? search-criteria)
    :education (education-filter-active? search-criteria)
    false))

(defn location-filter-label
  "Returns label for location filter"
  [{:keys [location radius]
    :or   {radius criteria/radius-default}}]
  (when (seq location)
    (str "Within " radius " mi of " location)))

(defn salary-filter-label
  "Returns label for salary filter"
  [[salary-min salary-max]]
  (when (not (and (= criteria/salary-min-default salary-min)
                  (= criteria/salary-max-default salary-max)))
    (str "$" (/ salary-min 1000) "K to " "$" (/ salary-max 1000) "K")))

(defn education-filter-label
  "Returns name of highest level degree if chosen"
  [min-degree-category-id]
  (when-not (= criteria/min-degree-default min-degree-category-id)
    (->> (dd/search-education)
         (filter (comp (partial = min-degree-category-id) :id))
         first
         :label)))

(defn years-experience-range
  [work-experience-ids]
  (let [[min-lower min-higher] (->> work-experience-ids (apply min) (get member/experience-bands))
        [max-lower max-higher] (->> work-experience-ids (apply max) (get member/experience-bands))]
    (str (if-not min-lower (str "<" min-higher) min-lower)
         " to "
         (if-not max-higher (str max-lower "+") max-higher))))

(defn years-experience-label
  "Converts ids to years experience string"
  [work-experience-ids]
  (when-not (= (set work-experience-ids) (-> member/experience-map keys set))
    (str (years-experience-range work-experience-ids) " Years Experience")))

(defn role-filter-label
  "Returns label for given set of discipline ids"
  [discipline-ids]
  (when-not (= discipline-ids criteria/discipline-ids-default)
    (->> (dd/search-role)
         (filter (comp (partial = discipline-ids) :id))
         first
         :label)))

(defn show-fa-overlay?
  "Determines whether or not to show the full-access overlay"
  [[current-page full-access filter approved? loading?] _]
  (cond
    loading? false
    (not approved?) false
    full-access false
    (= filter "contacted") false
    :else (or (= 0 current-page) (> current-page 1))))

(defn view
  "Returns either :all, :unviewed or :contacted based on search parameters"
  [[include-viewed? include-contacted? include-never-viewed? include-never-contacted?]]
  (cond
    (not include-viewed?) :unviewed
    (not include-contacted?) :uncontacted
    (not include-never-viewed?) :viewed
    (not include-never-contacted?) :contacted
    :else :all))

(defn filter-removable?
  "Returns false if given key is the only filter that is not empty"
  [[keywords title candidate-name company] [_ filter-key]]
  (if-let [values (case filter-key
                    :keywords [title candidate-name company]
                    :title [keywords candidate-name company]
                    :candidate-name [keywords title company]
                    :company [keywords title candidate-name]
                    nil)]
    (not (every? empty? values))
    true))

(rf/reg-sub
  :search-results/search-params
  :<- [:search-results/criteria]
  :<- [:search-results/sort-by]
  search-params)

(rf/reg-sub
  :search-results/selected-filter?
  :<- [:search-results/selected-filter]
  selected-filter?)

(rf/reg-sub
  :search-results/active-filter?
  :<- [:search-results/criteria-search-criteria]
  active-filter?)

(rf/reg-sub
  :search-results/keywords-filter-label
  :<- [:search-results/criteria-search-criteria-keyword]
  not-empty)

(rf/reg-sub
  :search-results/location-filter-label
  :<- [:search-results/criteria-search-criteria]
  location-filter-label)

(rf/reg-sub
  :search-results/title-filter-label
  :<- [:search-results/criteria-search-criteria-title]
  not-empty)

(rf/reg-sub
  :search-results/salary-filter-label
  :<- [:search-results/criteria-search-criteria-salary-min]
  :<- [:search-results/criteria-search-criteria-salary-max]
  salary-filter-label)

(rf/reg-sub
  :search-results/candidate-name-filter-label
  :<- [:search-results/criteria-search-criteria-candidate-name]
  not-empty)

(rf/reg-sub
  :search-results/education-filter-label
  :<- [:search-results/criteria-search-criteria-min-degree-category-id]
  education-filter-label)

(rf/reg-sub
  :search-results/company-filter-label
  :<- [:search-results/criteria-search-criteria-company]
  not-empty)

(rf/reg-sub
  :search-results/school-filter-label
  :<- [:search-results/criteria-search-criteria-school]
  not-empty)

(rf/reg-sub
  :search-results/years-experience-label
  :<- [:search-results/criteria-search-criteria-work-experience-ids]
  years-experience-label)

(rf/reg-sub
  :search-results/role-label
  :<- [:search-results/criteria-search-criteria-discipline-ids]
  role-filter-label)

(rf/reg-sub
  :search-results/search-criteria-valid?
  :<- [:search/criteria]
  some?)

(rf/reg-sub
  :search-results/show-fa-overlay?
  :<- [(table/page-number-sub ::table/search-results)]
  :<- [:recruiter/full-access]
  :<- [:search-results/view]
  :<- [:recruiter/approved?]
  :<- [(table/loading-sub ::table/search-results)]
  show-fa-overlay?)

(rf/reg-sub
  :search-results/view
  :<- [:search-results/criteria-search-parameters-include-candidates-viewed?]
  :<- [:search-results/criteria-search-parameters-include-candidates-contacted?]
  :<- [:search-results/criteria-search-parameters-include-candidates-never-viewed?]
  :<- [:search-results/criteria-search-parameters-include-candidates-never-contacted?]
  view)

(rf/reg-sub
  :search-results/filter-removable?
  :<- [:search-results/criteria-search-criteria-keyword]
  :<- [:search-results/criteria-search-criteria-title]
  :<- [:search-results/criteria-search-criteria-candidate-name]
  :<- [:search-results/criteria-search-criteria-company]
  filter-removable?)
