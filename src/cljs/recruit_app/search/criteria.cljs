(ns recruit-app.search.criteria
  (:require [recruit-app.member :as member]))

(def criteria-defaults
  {:salary-min             100000
   :salary-max             500000
   :min-degree-category-id 1
   :radius                 25
   :work-experience-ids    (keys member/experience-map)
   :discipline-ids         [2101, 2102, 2103, 2104, 2105, 2201, 2202, 2203, 2204, 2205, 2206, 2207, 2901, 2902, 2903, 2904, 2905, 2906,
                            2907, 2908, 2401, 2402, 2403, 2404, 2405, 2406, 2407, 2408, 2409, 2410, 2411, 2412, 2413, 2001, 2002, 2003,
                            2004, 2005, 2501, 2502, 2503, 2504, 2505, 2506, 2507, 2801, 2802, 2803, 2804, 2805, 2806, 2807, 2809, 2601,
                            2602, 2603, 2604, 2605, 2606, 2301, 2302, 2303, 2304, 2305, 2306, 2307, 2701, 2702, 2703]})

(def parameter-defaults
  {:include-candidates-contacted?       true
   :include-candidates-never-contacted? true
   :include-candidates-viewed?          true
   :include-candidates-never-viewed?    true
   :include-desired-location?           true})

(def salary-min-default (:salary-min criteria-defaults))
(def salary-max-default (:salary-max criteria-defaults))
(def min-degree-default (:min-degree-category-id criteria-defaults))
(def radius-default (:radius criteria-defaults))
(def work-experience-ids-default (:work-experience-ids criteria-defaults))
(def discipline-ids-default (:discipline-ids criteria-defaults))
(def sort-by-default :recency)

(defn- merge-criteria-default
  [{:keys [search-criteria] :as criteria} k v]
  (if-not (contains? search-criteria k)
    (assoc-in criteria [:search-criteria k] v)
    criteria))

(defn criteria-with-defaults
  "Merges defaults in for criteria if not given"
  [criteria]
  (reduce-kv merge-criteria-default criteria criteria-defaults))

(defn search-request-params
  "Returns request made to search API (with defaults added to criteria)"
  ([criteria]
   (search-request-params criteria sort-by-default))
  ([criteria sort-by]
   {:criteria (criteria-with-defaults criteria)
    :sort-by  sort-by}))
