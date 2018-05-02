(ns recruit-app.hubspot.api
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [recruit-app.specs.common :as common]
            [cemerick.url :as url]
            [taoensso.timbre :as log]))

(def forms-url (str (-> env :hubspot :endpoints :submit-form)
                    "?hapikey=" (-> env :hubspot :api-key)))
(def success-response {:status 200})
(def error-response {:status 500})
(def success? #(= % 204))

(defn- form-request
  "Formats submitted value to format to be submitted to hubspot form"
  [{:keys [email firstname lastname telephone recruiter_id plan title company state_province]}]
  (-> {:firstname               firstname
       :lastname                lastname
       :jobtitle                title
       :company                 (:name company)
       :email                   email
       :company_phone_number__c telephone
       :phone                   telephone
       :recruiter_id__c         recruiter_id
       :pricing_plan            plan
       :state                   state_province}
      url/map->query
      (->> (hash-map :body))
      (assoc-in [:headers :content-type] "application/x-www-form-urlencoded")))

(defn- make-post-request
  "Makes request to forms API and returns proper response depending on status"
  [url request]
  (let [{:keys [status]} (http/post url request)]
    (if (success? status)
      success-response
      error-response)))

(defn submit-form
  "Submits user info to given hubspot form"
  [form field-values]
  (let [{:keys [portal-id guid]} (-> env :hubspot :forms (get form))]
    (try
      (make-post-request
        (format forms-url portal-id guid)
        (form-request field-values))
      (catch Exception e
        (log/error "Error submitting form to Hubspot: " field-values)
        error-response))))

;; specs

(s/def ::plan #{"Individual" "Enterprise"})
(s/def ::field-values (s/keys :req-un [::common/email ::common/firstname
                                       ::common/lastname ::common/telephone
                                       ::common/title ::common/company ::common/state]
                              :opt-un [::common/recruiter_id ::plan]))
(s/def ::form #{:full-access :pay-curtain :approved-recruiters})
(s/def ::status #{200 500})
(s/def ::response (s/keys :req-un [::status]))

(s/fdef submit-form
        :args (s/cat :form ::form
                     :field-values ::field-values)
        :ret ::response)
