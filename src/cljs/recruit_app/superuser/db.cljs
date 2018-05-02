(ns recruit-app.superuser.db
  (:require [cljs.spec.alpha :as s]))

(s/def ::quantity pos-int?)
(s/def ::type #{:promoted-job})
(s/def ::action #{:use-inventory :purchase-inventory})
(s/def ::recruiter-id pos-int?)
(s/def ::inventory-form (s/keys :req [::recruiter-id ::action ::type ::quantity]))
