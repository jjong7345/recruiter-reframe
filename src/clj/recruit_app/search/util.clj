(ns recruit-app.search.util
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as http]
            [clojure.string :as str]
            [config.core :refer [env]]
            [clj-time.format :as f]
            [recruit-app.kafka.query :as ri]))

(defn- clean-string
  [string]
  (if string
    (-> string
        (str/replace #"\\" " ")
        (str/replace #"\b(&)" "\\\\&")
        (str/replace #"\b( +&)" " AND ")
        (str/replace #"/" "\\\\/")
        (str/replace #"," " ")
        (str/replace #";" " ")
        (str/replace #"\|" " OR ")
        (str/replace #"\b( +-)|^( *)-" " NOT ")
        (str/replace #"\\" "\\\\")
        (str/replace #"!" "\\\\!")
        (str/replace #"\:" "\\\\:")
        (str/replace #"\^" "\\\\^")
        (str/replace #"\[" "\\\\[")
        (str/replace #"\]" "\\\\]")
        (str/replace #"\{" "\\\\{")
        (str/replace #"\}" "\\\\}")
        (str/replace #"\~" "\\\\~")
        (str/replace #"\?" "\\\\?"))))


(defn- upper-case-booleans
  [string]
  (-> string
      (str/replace #"\b( +or +)" " OR ")
      (str/replace #"\)( +or +)" ") OR ")
      (str/replace #"\*( +or +)" "* OR ")
      (str/replace #"\)( +and +)" ") AND ")
      (str/replace #"\*( +and +)" "* AND ")
      (str/replace #"\b( +and +)" " AND ")))


(defn- unnecessary-operator
  [string]
  (-> string
      (str/trim)
      (str/replace #"(AND$)|(and$)" "")
      (str/replace #"(\s+OR$)|(\s+or$)" "")
      (str/replace #"(^\s*AND)|(^\s*and)" "")
      (str/replace #"(-$)" "")))

(defn- pre-process-query
  [string]
  (-> string
      clean-string
      upper-case-booleans
      unnecessary-operator))

(defn clean-text-fields
  [{:keys [title company skills] :as es-params}]
  (cond-> es-params
          title (assoc-in [:title] (pre-process-query title))
          company (assoc-in [:company] (pre-process-query company))
          skills (assoc-in [:skills] (pre-process-query skills))))

(defn parse-location
  [{:keys [geo_location] :as es-params}]
  (if geo_location
    (let [location-map (http/post (-> env :services :locations :parser) (json/generate-string {:location-string geo_location}))]
      (-> es-params
          (assoc-in [:geo_location] [(:longitude location-map)
                                     (:latitude location-map)])))
    es-params))

(defn- split-name
  [name]
  (let [vector-names (str/split name #" +")
        last-name-map {:lastname (last vector-names)}
        first-name (str/trim (str/join " " (take (- (count vector-names) 1) vector-names)))]
    (if (not (str/blank? first-name)) (assoc-in last-name-map [:firstname] first-name)
                                      last-name-map)))

(defn split-fullname
  [{:keys [fullname] :as es-params}]
  (if fullname
    (merge es-params (split-name fullname))
    es-params))

(defn last-company-check
  [{:keys [company] :as es-params}]
  (if (str/blank? company)
    (dissoc es-params :last_company_only)
    es-params))

(defn last-title-check
  [{:keys [title] :as es-params}]
  (if (str/blank? title)
    (dissoc es-params :last_title_only)
    es-params))

(def custom-formatter (f/formatter "yyyy-MM-dd'T'00:00:00Z"))

(defn parsed-date
  [string]
  (when (not (nil? string)) (f/parse custom-formatter string)))

(defn after?
  "Returns true if first arg is not null and after second arg"
  [a b]
  (if a
    (if b (> a b) true)
    false))

(defn sorted-history
  "Returns history sorted by start date for experiences list"
  [history]
  (sort-by
    #(:startDate %)
    after?
    history))

(defn add-contacted-filter
  [req contacted]
  (assoc req :include_contacted_ids contacted))

(defn add-not-contacted-filter
	[req contacted]
	(assoc req :exclude_contacted_ids contacted))

(defn add-unviewed-filter
  [req viewed]
  (assoc req :exclude_viewed_ids viewed))

(defn add-viewed-filter
	[req viewed]
	(assoc req :include_viewed_ids viewed))

(defn filters
	[req viewed contacted]
	(let [{:keys [never-contacted?
								viewed?
								contacted?
								never-viewed?]} req]
		(cond (false? never-contacted?) (add-contacted-filter req
																													contacted)
					(false? contacted?) (add-not-contacted-filter req contacted)
					(false? never-viewed?) (add-viewed-filter req viewed)
					(false? viewed?) (add-unviewed-filter req viewed)
					:else (dissoc req :recruiter-id))))