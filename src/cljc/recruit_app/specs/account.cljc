(ns recruit-app.specs.account
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [clojure.string :as string]
                    [recruit-app.specs.common :as common]
                    [clojure.string :as str]))

(def not-blank? (complement string/blank?))
(def phone-char-regex #"\(|\)|-|\s|\.|x")
(def phone-number-format #"1?[0-9]{10,14}")

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

(defn phone-number?
  "Validates number after all dashes have been removed"
  [phone]
  (when (re-matches phone-number-format phone)
    (let [local-number (local-number phone)
          prefix (subs local-number 0 3)]
      (valid-prefix? prefix))))

(defn cleaned-phone-number
  "Removes special characters from phone string"
  [phone]
  (string/replace phone phone-char-regex ""))

(defn phone?
  "Validates that given input is valid US phone number"
  [phone]
  (-> phone cleaned-phone-number phone-number?))

(def url-pattern #"(?i)^(?:(?:https?|ftp)://)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$")
(def contain-email-regex #"[a-zA-Z0-9._%-]+@[a-zA-Z0-9][a-zA-Z0-9.-]{0,61}[a-zA-Z0-9]\.[a-zA-Z.]{2,6}")
(def contain-phone-regex #"1?(-|.|\s)?(\()?(\d{3})(\))?(-|.|\s|)(\d{3})(-|.|\s|)(\d{4})(-|.|\s|x|)(\d*)")
(def us-postal-code-regex #"(^[0-9]{5}(?:-[0-9]{4})?$)")
(def canada-postal-code-regex #"(^[ABCEGHJKLMNPRSTVXY]{1}\d{1}[A-Z]{1} *\d{1}[A-Z]{1}\d{1}$)")

(defn north-america-postal-code?
  [postal-code]
  (or (re-matches us-postal-code-regex postal-code) (re-matches canada-postal-code-regex postal-code)))

(def empty-string? (and string? string/blank?))

(def url-pattern? (s/or :url (and string? #(re-matches url-pattern %))
                        :empty empty-string?))

(def contain-email? (and string?  #(re-find contain-email-regex %)))

(def contain-phone? (and string? #(re-find contain-phone-regex %)))

(def contains-enough-characters? (and string? #(>= (count %) 100)))

(def valid-bio? (s/or :bio (s/and contains-enough-characters?
                                  (complement contain-email?)
                                  (complement contain-phone?))
                      :empty empty-string?))

(def zip-code? (s/and string? north-america-postal-code?))

(s/def ::linkedin url-pattern?)
(s/def ::twitter url-pattern?)
(s/def ::facebook url-pattern?)
(s/def ::blog url-pattern?)
(s/def ::company-site url-pattern?)

(s/def ::street (s/and string? not-blank?))
(s/def ::city (s/and string? not-blank?))
(s/def ::state (s/and string? not-blank?))
(s/def ::country (s/and string? not-blank?))
(s/def ::job-title (s/and string? not-blank?))
(s/def ::zip zip-code?)
(s/def ::phone phone?)
(s/def ::bio valid-bio?)
(def length-valid? #(> (count %) 5))
(def contains-number-regex #".*[0-9].*")
(def contains-letter-regex #".*[a-zA-Z].*")
(def contains-special-char-regex #".*[!\"\#\$\%\&\'\(\)\*\+\,\-.\/\:\;\<\=\>\?\@\[\\\]\^\_\`\{\|\}\~].*")
(def contains-number? #(re-matches contains-number-regex %))
(def contains-letter? #(re-matches contains-letter-regex %))
(def contains-special-character? #(re-matches contains-special-char-regex %))
(s/def ::current-password (s/and string? not-blank?))
(s/def ::new-password (s/and string?
                             contains-number?
                             contains-letter?
                             contains-special-character?
                             length-valid?))
(s/def ::confirm-password (s/and string? not-blank?))

(s/def ::account (s/keys :req-un [::street ::city ::state
                                  ::country ::job-title ::phone ::zip]
                         :opt-un [::company-site ::linkedin ::twitter
                                  ::facebook ::blog ::bio]))

(s/def ::change-password (s/keys :req-un [::current-password ::new-password ::confirm-password]))
