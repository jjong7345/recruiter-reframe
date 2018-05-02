(ns recruit-app.recruiter.db
  (:require [recruit-app.specs.recruiter :as recruiter]
            [cljs.spec.alpha :as s]))

(s/def ::recruiter ::recruiter/recruiter)
