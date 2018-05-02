(ns recruit-app.specs.onboarding
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [clojure.string :as cs]
                    [recruit-app.specs.common :as common]
                    [recruit-app.specs.account :as account]))

(def length-valid? #(> (count %) 5))
(def contains-number-regex #".*[0-9].*")
(def contains-letter-regex #".*[a-zA-Z].*")
(def contains-special-char-regex #".*[!\"\#\$\%\&\'\(\)\*\+\,\-.\/\:\;\<\=\>\?\@\[\\\]\^\_\`\{\|\}\~].*")
(def contains-number? #(re-matches contains-number-regex %))
(def contains-letter? #(re-matches contains-letter-regex %))
(def contains-special-character? #(re-matches contains-special-char-regex %))
(def not-blank? (complement cs/blank?))

(s/def ::firstname (s/and string? not-blank?))
(s/def ::lastname (s/and string? not-blank?))
(s/def ::phonenumber common/phone?)
(s/def ::extension (or string? int?))
(s/def ::companyname (s/and string? #(<= 2 (count %) 50)))
(s/def ::zipcode account/zip-code?)
(s/def ::companytype (s/and pos-int?))
(s/def ::company-id pos-int?)
(s/def ::password (s/and string?
                         contains-number?
                         contains-letter?
                         contains-special-character?
                         length-valid?))

(s/def ::non-preapproved (s/keys :req-un [::firstname ::lastname ::phonenumber
                                          ::companyname ::zipcode ::companytype
                                          ::password]
                                 :opt-un [::extension]))

(s/def ::preapproved (s/keys :req-un [::firstname ::lastname ::phonenumber
                                      ::password ::company-id]
                             :opt-un [::extension]))
