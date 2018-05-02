(ns recruit-app.db
  (:require [cljs.spec.alpha :as s]
            [recruit-app.recruiter.db :as recruiter]
            [recruit-app.search.db :as search]
            [recruit-app.search-results.db :as search-results]
            [recruit-app.teams.db :as teams]))

(s/def ::db (s/keys :opt-un [::recruiter/recruiter
                             ::search/search
                             ::search-results/search-results
                             ::teams/teams]))
(def default-db {:is-fetching-user? false})
