(ns recruit-app.modals.share-resume.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]))

(s/def ::jobseeker-id string?)

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def email? (s/and string? #(re-matches email-regex %)))

(s/def ::email email?)

(def valid-email? #(s/valid? ::email %))
(def multi-email? (s/and string? #(every? valid-email? (string/split % #","))))

(s/def ::emails multi-email?)
(s/def ::message string?)

(s/def ::share-resume (s/keys :req-un [::emails ::jobseeker-id]
                              :opt-un [::message]))
