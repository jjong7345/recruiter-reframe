(ns recruit-app.projects.db
  (:require [cljs.spec.alpha :as s]
            [recruit-app.specs.common :as common]))

(s/def ::name (s/and string? common/not-blank?))
(s/def ::project (s/keys :req-un [::name]))
