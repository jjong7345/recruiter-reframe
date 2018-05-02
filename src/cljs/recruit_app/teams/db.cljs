(ns recruit-app.teams.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [recruit-app.specs.common :as common]))

(def not-blank? (complement string/blank?))
(s/def ::team-name (and string? not-blank?))
(s/def ::email common/email?)
(s/def ::team (s/keys :req-un [::team-name ::email]))
