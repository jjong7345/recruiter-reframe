(ns recruit-app.teams.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [recruit-app.specs.common :as common]))

(s/def ::team-name (and string? common/not-blank?))
(s/def ::email common/email?)
(s/def ::new-team (s/keys :req-un [::team-name ::email]))

(s/def ::teams (s/keys :opt-un []))
