(ns recruit-app.specs.recruiter-admin
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
              [clojure.string :as string]
              [recruit-app.specs.account :as account]
              [recruit-app.specs.common :as common]))

(defn without-extension
  "Removes extension from phone number"
  [phone]
  (string/replace phone #"x.*$" ""))

(defn telephone?
  "Checks that phone number string is valid without extension"
  [phone]
  (-> phone
      without-extension
      account/cleaned-phone-number
      string/trim
      account/phone-number?))

(s/def ::firstname (s/and (complement string/blank?) ::common/firstname))
(s/def ::lastname (s/and (complement string/blank?) ::common/lastname))
(s/def ::telephone #(or (string/blank? %) (telephone? %)))
(s/def ::postal-code #(or (string/blank? %) (s/valid? ::account/zip %)))
(s/def ::recruiter-admin-account (s/keys :req-un [::firstname ::lastname
                                                  ::telephone ::common/email
                                                  ::postal-code]))
