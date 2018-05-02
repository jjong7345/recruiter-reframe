(ns recruit-app.search-results.db
  (:require [cljs.spec.alpha :as s]
            [recruit-app.specs.search :as search]))

(s/def ::saved-search-loaded? boolean?)
(s/def ::show-minimum-criteria-error? boolean?)
(s/def ::selected-filter (s/nilable #{:keywords :location :profile :salary
                                      :experience :education}))
(s/def ::page (s/nilable nat-int?))
(s/def ::sort-by #{:recency :relevance})
(s/def ::saved-search-id (s/nilable search/search-id?))
(s/def ::criteria (s/nilable ::search/criteria))
(s/def ::search-results (s/keys :opt-un [::criteria
                                         ::saved-search-id
                                         ::sort-by
                                         ::page
                                         ::selected-filter
                                         ::saved-search-loaded?
                                         ::show-minimum-criteria-error?]))
