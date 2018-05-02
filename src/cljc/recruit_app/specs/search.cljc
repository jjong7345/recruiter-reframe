(ns recruit-app.specs.search
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [recruit-app.specs.common :as common]))

(def search-id? common/valid-guid?)

;; Search
(s/def ::recruiter-id integer?)
(s/def ::search-id search-id?)
(s/def ::search-name string?)
(s/def ::create-time common/datetime-string?)
(s/def ::update-time common/datetime-string?)

;; Criteria
(s/def ::title string?)
(s/def ::keyword string?)
(s/def ::candidate-name string?)
(s/def ::company string?)
(s/def ::school string?)
(s/def ::location string?)
(s/def ::radius pos-int?)
(s/def ::salary-min (and pos-int?
                         (partial <= 0)))
(s/def ::salary-max pos-int?)
(s/def ::min-degree-category-id pos-int?)
(s/def ::work-experience-ids (s/coll-of pos-int?))
(s/def ::discipline-ids (s/coll-of pos-int?))
(s/def ::search-criteria (s/keys :opt-un [::title
                                          ::keyword
                                          ::candidate-name
                                          ::company
                                          ::school
                                          ::location
                                          ::radius
                                          ::salary-min
                                          ::salary-max
                                          ::min-degree-category-id
                                          ::work-experience-ids
                                          ::discipline-ids]))

;; Parameters
(s/def ::only-last-title? boolean?)
(s/def ::only-last-company? boolean?)
(s/def ::include-candidates-never-contacted? boolean?)
(s/def ::include-candidates-contacted? boolean?)
(s/def ::include-candidates-never-viewed? boolean?)
(s/def ::include-candidates-viewed? boolean?)
(s/def ::search-parameters (s/keys :opt-un [::only-last-title?
                                            ::only-last-company?
                                            ::include-candidates-never-contacted?
                                            ::include-candidates-contacted?
                                            ::include-candidates-never-viewed?
                                            ::include-candidates-viewed?]))

;; Email Reporting
(s/def ::frequency-type (s/int-in 0 3))
(s/def ::interval (s/nilable (s/int-in 1 8)))
(s/def ::last-send-time (s/nilable common/datetime-string?))
(s/def ::email (s/keys :opt-un [::frequency-type
                                ::interval
                                ::last-send-time]))
(s/def ::reporting (s/keys :opt-un [::email]))

(s/def ::criteria (s/keys :opt-un [::recruiter-id
                                   ::search-id
                                   ::search-name
                                   ::create-time
                                   ::update-time
                                   ::search-criteria
                                   ::search-parameters
                                   ::reporting]))
