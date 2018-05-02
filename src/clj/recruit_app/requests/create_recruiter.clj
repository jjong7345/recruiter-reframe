(ns recruit-app.requests.create-recruiter
  (:require [clojure.set :refer [rename-keys]]))

(defn- with-company-name
  [request company-name]
  (-> request
      (assoc :company_name company-name)
      (dissoc :companyName)))

(defn- with-company-id
  [request company-id]
  (-> request
      (assoc :company_id company-id)
      (dissoc :companyId)))

(defn- with-company-type
  [request company-type]
  (-> request
      (assoc :company_type_id company-type)
      (dissoc :companyType)))

(defn request
  "Returns request to create recruiter"
  [{:keys [companyName companyType companyId] :as request}]
  (cond-> (rename-keys request {:firstName :firstname
                                :lastName  :lastname
                                :phone     :telephone})
          companyName (with-company-name companyName)
          companyType (with-company-type companyType)
          companyId (with-company-id companyId)))
