(ns recruit-app.post-job.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as cstr]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;specs

(s/def ::recruiterId pos-int?)
(s/def ::job-id pos-int?)
(s/def ::promoted boolean?)
(s/def ::url string?)
(s/def ::job.title (s/and string? #(and (>= (count (cstr/trim %)) 1) (<= (count %) 255))))
(s/def ::fullDescription (s/and string? #(and (>= (-> %
                                                      (clojure.string/split #" ")
                                                      count) 100) (<= (count %) 15000))))
(s/def ::config (s/keys :req-un [::url]))

;;yearsexperience
(s/def ::yearsExperience.id (s/and pos-int? #(and (>= % 1) (<= % 5))))
(s/def ::name string?)
(s/def ::sortOrder pos-int?)
(s/def ::active boolean?)
(s/def ::yearsExperience (s/keys :req-un [::yearsExperience.id ::name]
                                 :opt-un [::sortOrder ::active]))

;;;compensation
(s/def ::total string?)
(s/def ::salary (s/and string? #(re-matches #"\b(4[0-9]|5[0-9]|6[0-9]|7[0-9]|8[0-9]|9[0-9]|[1-4][0-9]{2}|500)\b" %)))
(s/def ::bonus pos-int?)
(s/def ::other string?)
(s/def ::compensation (s/keys :req-un [::total ::salary]
                              :opt-un [::bonus ::other]))
;;
(s/def ::id pos-int?)
;;;company
(s/def ::logoUrl string?)
(s/def ::company.name (s/and string? #(and (>= (count %) 2) (<= (count %) 90))))
;;industry
(s/def ::industry.name (s/and string? #(not= % "0")))

(s/def ::sector (s/keys :opt-un [::name ::id]))
(s/def ::industry (s/keys :req-un [:industry/name]
                          :opt-un [::id ::sector]))

;;;;;;;;;;;CompanySize
(s/def ::lowerSize pos-int?)
(s/def ::displayText string?)
(s/def ::description string?)
(s/def ::company.size.id (s/and pos-int? #(and (>= % 1) (<= % 7))))
;;;company size id might need  extra work,
;;;like validateJreqCompanySize in Job class
;(s/def :company/size (s/keys :req-un [:company/size/id]
;                             :opt-un [::lowerSize ::displayText ::description
;                                           ::active]))

;;;;
(s/def ::company (s/keys :req-un [:company/name ::industry]
                         :opt-un [::logoUrl :company/size]))
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;end of company
;;location
(s/def :location/name (and string? #(not (cstr/blank? %))))
(s/def :location/type (and string? #(not (cstr/blank? %))))
(s/def ::location (s/keys :req-un [:location/name]))
(s/def ::locations (s/and (s/coll-of ::location) #(and (>= (count %) 1) (<= (count %) 100))))

(s/def ::job (s/keys :req-un [::recruiterId ::promoted :job/title ::fullDescription
                              ::yearsExperience ::compensation ::company ::locations]))


(s/def ::jobChangeRequest (s/keys :req-un [::job]))
(s/def ::job-info (s/keys :req-un [::jobChangeRequest]))

(s/def ::true #(boolean %))




;;;;;;
(s/fdef job-promotion :args (s/cat
                              :config ::config
                              :job-id ::job-id))

(s/fdef post-job :args (s/cat
                         :config ::config
                         :job-info ::job-info))