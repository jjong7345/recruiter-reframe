(ns recruit-app.specs.common
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [clojure.string :as cs]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def email? (s/and string? #(re-matches email-regex %)))

(def secure-id-regex #"^01-sid-[A-Z0-9]{26}")
(def secure-id? (s/and string? #(re-matches secure-id-regex %)))

(def phone-char-regex #"\(|\)|-|\s|\.|x")
(def phone-number-format #"1?[0-9]{10,14}")

(def not-blank? (complement cs/blank?))

(defn- local-number-start
  "Returns index of beginning of local number (without area code)"
  [number-str]
  (if (= "1" (subs number-str 0 1)) 4 3))

(defn local-number
  "Removes area code and country code from number"
  [number-str]
  (subs number-str (local-number-start number-str)))

(defn valid-prefix?
  "Invalid if not 3 characters or equal to 555"
  [prefix]
  (and (= 3 (count prefix))
       (not= "555" prefix)))

(defn phone?
  "Validates that given input is valid US phone number"
  [phone]
  (let [number-str (cs/replace phone phone-char-regex "")]
    (when (re-matches phone-number-format number-str)
      (let [local-number (local-number number-str)
            prefix (subs local-number 0 3)]
        (valid-prefix? prefix)))))

;; Creating predicate in case we ever change the handling
(def recruiter-id? pos-int?)

(s/def ::firstname string?)
(s/def ::lastname string?)
(s/def ::email email?)
(s/def ::telephone phone?)
;; some db responses use underscore case
(s/def ::recruiter_id recruiter-id?)
(s/def ::recruiter-id recruiter-id?)
(s/def ::secure-id secure-id?)
(s/def ::admin-notes string?)
(s/def ::title string?)

(s/def ::id pos-int?)
(s/def ::name string?)
(s/def ::company (s/keys :req-un [::id ::name]))
(s/def ::state string?)
