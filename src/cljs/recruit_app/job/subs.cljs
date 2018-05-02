(ns recruit-app.job.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [clojure.set :refer [union]]
            [goog.string :as gs]
            [recruit-app.util.subscription :as subs]
            [recruit-app.util.job :as ju]
            [recruit-app.post-job.subs :as ps]
            [recruit-app.util.sort :as s]
            [cljs-time.format :as f]
            [recruit-app.components.table :as table]))

(subs/reg-subs "job" [["active-tab" :tab0] ["active-job-id" nil] ["active-job-loc-id" nil] ["checked-candidates" #{}]
                      ["show-contact-candidates-modal?" false] ["fetched" #{}]])

(defn preview-job
  "Returns job to be previewed based on preview type"
  [[post active type] _]
  (if (= type :post) post active))

(defn min-total-comp
  "Returns min total comp for active job based on min-comp and bonus"
  [[min-comp bonus] _]
  (ju/comp-with-bonus min-comp bonus))

(defn max-total-comp
  "Returns max total comp for active job based on max-comp and bonus"
  [[max-comp bonus] _]
  (ju/comp-with-bonus max-comp bonus))

(defn preview-type
  "Returns preview type
    :post if from post-job view
    :active if viewing existing job"
  [db _]
  (get-in db [:preview-job :type] :post))

(defn active-job-loc-selection
  "Returns active-job-loc-id if set, otherwise returns id of first location"
  [[job-loc-id locations] _]
  (or job-loc-id (-> locations first :id)))

(defn active-job
  "Returns active job formatted for preview"
  [[active-job-id jobs] _]
  (let [job (some #(when (= active-job-id (:job_id %)) %) jobs)]
    (if job
      (ju/post-job-format job)
      (ps/with-default-values job))))

(defn active-job-locations
  "Returns vector of names of location options"
  [active-job _]
  (map :name (:locations active-job)))

(defn active-job-location
  "Returns active job location depending on active job location id"
  [[job-loc-id job] _]
  (ju/active-job-location (:locations job) job-loc-id))

(defn active-job-applicants
  "Returns applicants for active job location"
  [{:keys [applicants]} _]
  applicants)

(defn active-job-viewers
  "Returns viewers for active job location"
  [{:keys [views]} _]
  views)

(defn job-loc-choices
  "Returns location options for active job"
  [job _]
  (if-let [locs (:locations job)]
    (mapv #(hash-map :id (:job_location_id %) :label (gs/unescapeEntities (:name %))) locs)
    []))

(defn email-candidate-checked?
  "Returns whether or not given jobseeker has been checked (to be contacted)"
  [checked-candidates [_ js-id]]
  (some #(= (:jobSeekerId %) js-id) checked-candidates))

(defn checked-candidate-count
  "Returns count of checked candidates"
  [checked-candidates _]
  (count checked-candidates))

(defn active-job-location-application-status
  "Returns map of application status keyed by jobseeker-id"
  [{:keys [jobseeker_application_status]} _]
  (ju/application-keyed-by-jsid jobseeker_application_status))

(defn active-applicants
  "Returns active applicants being seen on candidate profile page"
  [[applicants application-status] _]
  (map (partial ju/candidate-record application-status) applicants))

(defn active-viewers
  "Returns active job viewers being seen on candidate profile page"
  [[viewers application-status] _]
  (map (partial ju/candidate-record application-status) viewers))

(defn is-job-fetched?
  "Checks if job-id is in collection of fetched jobs"
  [[job-id fetched] _]
  (contains? fetched job-id))

(rf/reg-sub
  :preview-job/job
  :<- [:post-job/job]
  :<- [:job/active-job]
  :<- [:preview-job/type]
  preview-job)

(rf/reg-sub
  :active-job/min-total-comp
  :<- [:active-job/min-comp]
  :<- [:active-job/bonus]
  min-total-comp)

(rf/reg-sub
  :active-job/max-total-comp
  :<- [:active-job/max-comp]
  :<- [:active-job/bonus]
  max-total-comp)

(rf/reg-sub
  :preview-job/type
  preview-type)

(rf/reg-sub
  :job/active-job-loc-selection
  :<- [:job/active-job-loc-id]
  :<- [:job/job-loc-choices]
  active-job-loc-selection)

(rf/reg-sub
  :job/active-job
  :<- [:job/active-job-id]
  :<- [:jobs/all]
  active-job)

(rf/reg-sub
  :job/active-job-locations
  :<- [:job/active-job]
  active-job-locations)

(rf/reg-sub
  :job/active-job-location
  :<- [:job/active-job-loc-selection]
  :<- [:job/active-job]
  active-job-location)

(rf/reg-sub
  :job/active-job-applicants
  :<- [:job/active-job-location]
  active-job-applicants)

(rf/reg-sub
  :job/active-job-viewers
  :<- [:job/active-job-location]
  active-job-viewers)

(rf/reg-sub
  :job/job-loc-choices
  :<- [:job/active-job]
  job-loc-choices)

(rf/reg-sub
  :job/email-candidate-checked?
  :<- [:job/checked-candidates]
  email-candidate-checked?)

(rf/reg-sub
  :job/checked-candidate-count
  :<- [:job/checked-candidates]
  checked-candidate-count)

(rf/reg-sub
  :job/active-job-location-application-status
  :<- [:job/active-job-location]
  active-job-location-application-status)

(rf/reg-sub
  :job/active-applicants
  :<- [(table/sorted-data-sub ::table/job-applicants)]
  :<- [:job/active-job-location-application-status]
  active-applicants)

(rf/reg-sub
  :job/active-viewers
  :<- [(table/sorted-data-sub ::table/job-viewers)]
  :<- [:job/active-job-location-application-status]
  active-viewers)

(rf/reg-sub
  :job/is-active-job-fetched?
  :<- [:job/active-job-id]
  :<- [:job/fetched]
  is-job-fetched?)
