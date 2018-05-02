(ns recruit-app.specs.teams
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [recruit-app.specs.common :as common]
                    [recruit-app.specs.recruiter :as recruiter]))

(s/def ::recruiter-guid common/valid-guid?)
(s/def ::recruiter (s/merge
                     ::recruiter/recruiter
                     (s/keys :req-un [::recruiter-guid])))
(s/def ::members (s/coll-of ::recruiter))
(s/def ::admins (s/coll-of ::recruiter))
(s/def ::time pos-int?)
(s/def ::recruiter-id common/valid-guid?)
(s/def ::team-name string?)
(s/def ::admin-id common/valid-guid?)
(s/def ::team-id common/valid-guid?)
(s/def ::team (s/keys :req-un [::common/email
                               ::team-id
                               ::admin-id
                               ::team-name
                               ::recruiter-id
                               ::time
                               ::admins
                               ::members]))
(s/def ::teams (s/map-of common/valid-guid? ::team))
