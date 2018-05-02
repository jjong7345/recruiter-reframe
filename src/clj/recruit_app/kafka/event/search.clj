(ns recruit-app.kafka.event.search
  (:require [recruit-app.taxonomy.api :as tax]
            [recruit-app.member :as member]
            [ladders-domains.recruiter.candidate-search :as search-event])
  (:import [java.util UUID]))

(defn with-criteria
  "Adds search criteria to event"
  [{:keys [search-criteria search-parameters]} event]
  (let [{:keys [salary-min
                salary-max
                keyword
                title
                company
                location
                radius
                candidate-name
                school
                min-degree-category-id
                work-experience-ids
                discipline-ids]} search-criteria
        {:keys [include-candidates-contacted?
                include-candidates-never-contacted?
                include-candidates-viewed?
                include-candidates-never-viewed?
                only-last-title?
                only-last-company?]
         :or   {include-candidates-contacted?       true
                include-candidates-never-contacted? true
                include-candidates-viewed?          true
                include-candidates-never-viewed?    true
                only-last-title?                    false
                only-last-company?                  false}} search-parameters]
    (merge
      event
      (cond-> {::search-event/include-candidates-contacted?       include-candidates-contacted?
               ::search-event/include-candidates-never-contacted? include-candidates-never-contacted?
               ::search-event/include-candidates-viewed?          include-candidates-viewed?
               ::search-event/include-candidates-never-viewed?    include-candidates-never-viewed?
               ::search-event/min-compensation                    salary-min
               ::search-event/max-compensation                    salary-max
               ::search-event/work-experiences                    (map member/experience-map work-experience-ids)
               ::search-event/min-degree                          (member/degree-map min-degree-category-id)
               ::search-event/roles                               (-> discipline-ids tax/disciplines-to-function-ids tax/role-labels)}
              title (assoc ::search-event/title title)
              title (assoc ::search-event/latest-title-only? only-last-title?)
              company (assoc ::search-event/company company)
              company (assoc ::search-event/latest-company-only? only-last-company?)
              keyword (assoc ::search-event/keyword keyword)
              location (assoc ::search-event/location location)
              radius (assoc ::search-event/location-radius radius)
              school (assoc ::search-event/school school)
              candidate-name (assoc ::search-event/candidate-name candidate-name)))))

(defn with-saved-search-id
  "Adds jp_saved_search_id UUID to event"
  [saved-search-id event]
  (if saved-search-id
    (->> saved-search-id
         UUID/fromString
         (assoc event ::search-event/saved-search-id))
    event))
