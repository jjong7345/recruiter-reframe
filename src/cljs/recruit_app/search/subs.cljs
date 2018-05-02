(ns recruit-app.search.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]
            [recruit-app.util.search :as su]
            [re-frame.subs :as subs]
            [recruit-app.components.table :as table]
            [recruit-app.member :as member]
            [recruit-app.search.criteria :as criteria]))

(sub/reg-subs "search" [["per-page" 10] ["criteria" nil]])
(sub/reg-subs "search" "criteria" [["search-criteria" {}] ["search-parameters" {}]])
(sub/reg-subs "search" [["show-no-result-error?" false]])

(sub/reg-subs
  "search"
  "criteria"
  "search-criteria"
  [["location" ""]
   ["radius" criteria/radius-default]
   ["salary-min" criteria/salary-min-default]
   ["salary-max" criteria/salary-max-default]
   ["company" ""]
   ["school" ""]
   ["candidate-name" ""]
   ["title" ""]
   ["keyword" ""]
   ["min-degree-category-id" criteria/min-degree-default]
   ["work-experience-ids" criteria/work-experience-ids-default]
   ["discipline-ids" criteria/discipline-ids-default]])

(sub/reg-subs
  "search"
  "criteria"
  "search-parameters"
  [["include-desired-location?" true]
   ["only-last-title?" false]
   ["only-last-company?" false]])

(defn formatted-candidates
  "Takes tuple of index and candidates"
  [all-candidates [idx candidates]]
  (assoc all-candidates idx (map su/candidate-record candidates)))

(defn active-search-candidates
  "Returns formatted candidates for current page"
  [results _]
  (reduce formatted-candidates {} results))

(defn minimum-required-inputs?
  "Returns true if any of the parameters ('keyword', 'title', 'candidate-name', 'company') passed is not empty"
  [[keyword title candidate-name company] _]
  (or (not (empty? keyword))
      (not (empty? title))
      (not (empty? candidate-name))
      (not (empty? company))))

(defn min-experience
  "Returns index of minimum experience based on work-experience-ids"
  [work-experience-ids]
  (- (apply min work-experience-ids) 1))

(defn max-experience
  "Returns index of maximum experience based on work-experience-ids"
  [work-experience-ids]
  (- (apply max work-experience-ids) 1))

(rf/reg-sub
  :search/active-search-candidates
  :<- [(table/page-map-sub ::table/search-results)]
  active-search-candidates)

(rf/reg-sub
  :search/minimum-required-inputs?
  :<- [:search/criteria-search-criteria-keyword]
  :<- [:search/criteria-search-criteria-title]
  :<- [:search/criteria-search-criteria-candidate-name]
  :<- [:search/criteria-search-criteria-company]
  minimum-required-inputs?)

(rf/reg-sub
  :search/min-experience
  :<- [:search/criteria-search-criteria-work-experience-ids]
  min-experience)

(rf/reg-sub
  :search/max-experience
  :<- [:search/criteria-search-criteria-work-experience-ids]
  max-experience)
