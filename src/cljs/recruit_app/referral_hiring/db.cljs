(ns recruit-app.referral-hiring.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as cstr]))

(def input-not-blank?
  (complement cstr/blank?))

;;name
(s/def ::fullname (s/and string? input-not-blank?))

;;email
(s/def ::email (s/and string? input-not-blank?))

;;company
(s/def ::company (s/and string? input-not-blank?))

;;referral
(s/def ::referral (s/and string? input-not-blank?))