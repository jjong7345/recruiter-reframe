(ns recruit-app.util.search
  (:require [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [clojure.string :as cs]
            [recruit-app.member :as member]))

(defn candidate-index
  "Returns index within candidate map for given secure-id"
  [candidates secure-id page per-page]
  (->> candidates
       (keep-indexed (fn [idx candidate] (when (= secure-id (:secureId candidate)) idx)))
       (first)
       (+ (* page per-page))))

(defn long->date-string
  "Converts given long to date string"
  [num]
  (when num
    (->> num
         (c/from-long)
         (f/unparse (f/formatters :date-time-no-ms)))))

(defn candidate-history
  "Returns formatted history item for profile page from search result"
  [{:keys [title companyName startDate endDate]}]
  {:title        title
   :company_name companyName
   :start_date   (long->date-string startDate)
   :end_date     (long->date-string endDate)})

(defn parsed-name
  "Parses full name returning tuple of first name and rest of string"
  [full-name]
  (let [pieces (cs/split full-name #" ")]
    [(first pieces) (cs/join " " (nthrest pieces 1))]))

(defn candidate-record
  "Returns formatted candidate record for profile page from search result"
  [{:keys [secureId candidateName jobSeekerId profile location compensation
           lastEmailOpen lastResumeUpdate lastLogin lastProfileUpdate]}]
  (let [[first-name last-name] (parsed-name candidateName)]
    {:id                  jobSeekerId
     :secure-id           secureId
     :desired             compensation
     :first-name          first-name
     :last-name           last-name
     :location            {:city location}
     :education           (:education profile)
     :history             (map candidate-history (:experience profile))
     :years-experience    (get member/experience-map (:workExperienceId profile))
     :last-email-open     lastEmailOpen
     :last-resume-update  lastResumeUpdate
     :last-login          lastLogin
     :last-profile-update lastProfileUpdate}))

(def role-ids
  [2300, 2800, 2900, 2600, 2200, 2500, 2700, 2100, 2000, 2400])

(defn get-function-ids
  [role]
  (if (or (= role 0) (nil? role))
    role-ids
    (vector (role-ids (dec role)))))

(defn get-role-from-function-ids
  [function-ids]
  (if (> (count function-ids) 1)
    0
    (->> function-ids
         (first)
         (.indexOf role-ids)
         (inc))))

(defn get-workexperience-ids
  [min max]
  (let [ids [1 2 3 4 5]
        start (if (nil? min) 0 min)
        end (if (nil? max) 5 (inc max))]
    (subvec ids start end)))

(defn format-salary
  [salary]
  (/ (cond
       (< salary 40000) 40000
       (> salary 500000) 500000
       :else salary)
     1000))

(defn search-request
  [{:keys [location distance name skills title latest-title? company latest-company? min-salary max-salary role min-experience max-experience education school filter sort-by active-saved-search-name active-saved-search-id email-time-sent email-time-executed email-frequency-type email-interval time-created time-updated]
    :or   {name "" skills "" title "" company "" latest-title? false min-salary 40 max-salary 500 latest-company? false education 1 sort-by "recency" email-time-sent nil email-time-executed nil email-frequency-type 0 email-interval nil distance 25}} start saved-search-id]
  {:savedsearch {:name         active-saved-search-name
                 :id           saved-search-id
                 :reporting-options
                               {:email
                                {:interval       email-interval
                                 :time-sent      email-time-sent
                                 :frequency-type email-frequency-type}}
                 :criteria
                               {:location-text                       (if (empty? location) nil location)
                                :location-radius                     distance
                                :candidate-name                      name
                                :keyword                             skills
                                :title                               {:text           title
                                                                      :mostRecentOnly latest-title?}
                                :salary-min                          (* min-salary 1000)
                                :salary-max                          (* max-salary 1000)
                                :function-ids                        (get-function-ids role)
                                :include-candidates-viewed?          (cond
                                                                       (= filter "unviewed") false
                                                                       (= filter "contacted") true
                                                                       :else true)
                                :include-candidates-contacted?       (cond
                                                                       (= filter "unviewed") false
                                                                       (= filter "contacted") true
                                                                       :else true)
                                :include-candidates-never-viewed?    (cond
                                                                       (= filter "unviewed") true
                                                                       (= filter "contacted") false
                                                                       :else true)
                                :include-candidates-never-contacted? (cond
                                                                       (= filter "unviewed") true
                                                                       (= filter "contacted") false
                                                                       :else true)
                                :work-experience-ids                 (get-workexperience-ids min-experience max-experience)
                                :company                             {:text           company
                                                                      :mostRecentOnly latest-company?}
                                :min-degree-category-id              education
                                :schools-attended                    school}
                 :time-created time-created
                 :time-updated time-updated
                 :sort-by      sort-by
                 :pagination
                               {:start (* start 10)
                                :rows  10}}})

(defn format-search-name
  [name]
  (when name
    (-> name
        (cs/trim)
        (cs/lower-case))))

(defn get-saved-search-data
  [val key data]
  (when (not (empty? (str val)))
    (->> data
         (filter (fn [x] (= (format-search-name (str ((keyword key) x))) (str val))))
         (into {}))))

(defn saved-search-request
  [saved-search-data start sort-by]
  {:savedsearch (-> saved-search-data
                    (assoc-in [:sort-by] (or sort-by "recency"))
                    (assoc-in [:pagination :start] (* start 10))
                    (assoc-in [:pagination :rows] 10))})

(defn has-same-name?
  [input data]
  (> (count (->> data
                 (filter (fn [d] (= (:name d) input))))) 0))

(defn gen-name
  [name data]
  (let [i (atom 0)]
    (reduce (fn [input d] (if (has-same-name? input data) (str input "-" (swap! i inc)) input)) name data)))

(defn saved-search-id-from-name
  "Returns saved-search-id for given name"
  [name saved-searches]
  (when name
    (-> name
        (get-saved-search-data "name" saved-searches)
        :id)))

(defn saved-search-id
  "Either return given id or get id from name in result"
  [id name saved-searches]
  (or id (saved-search-id-from-name name saved-searches)))

(defn last-run-time
  "Returns either last execution time or insert time for saved search"
  [{:keys [time-created reporting-options]}]
  (or (-> reporting-options :email :time-executed) time-created))

(defn blur-candidate?
  [index full-access view approved?]
  (cond
    (not approved?) true
    full-access false
    (= view :contacted) false
    :else (or (< index 3) (>= index 20))))
