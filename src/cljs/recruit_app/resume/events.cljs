(ns recruit-app.resume.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [cognitect.transit :as t]
            [recruit-app.util.job :as ju]))

(defn add-metadata
  "Associates metadata to given secure-id in resume ns"
  [db [_ secure-id metadata]]
  (assoc-in db [:resume :metadata secure-id] metadata))

(defn resume-metadata-success
  "Dispatch events to add secure-id to `fetched-metadata` and associate metadata if not empty"
  [_ [_ secure-id response]]
  {:dispatch-n (cond-> [[:resume/add-to-metadata-fetched secure-id]]
                       (seq response) (conj [:resume/add-metadata secure-id response]))})

(defn resume-metadata-failure
  "Dispatch event to add secure-id to `fetched-metadata`"
  [_ [_ secure-id]]
  {:dispatch [:resume/add-to-metadata-fetched secure-id]})

(defn add-to-metadata-fetched
  "Update `metadata-fetched` set to add given secure-id"
  [db [_ secure-id]]
  (update-in db [:resume :metadata-fetched] ju/add-to-set secure-id))

(defn- metadata-not-fetched?
  "Check if metadata has yet to be fetched for given secure-id"
  [{:keys [resume]} secure-id]
  (-> resume
      :metadata-fetched
      (contains? secure-id)
      not))

(defn get-resume-metadata
  "Fetch metadata for given secure-id if not already fetched"
  [{:keys [db]} [_ secure-id]]
  (when (metadata-not-fetched? db secure-id)
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :resume-metadata secure-id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:resume/resume-metadata-success secure-id]
                     :on-failure      [:resume/resume-metadata-failure secure-id]}}))

(defn resume-downloaded
  "Makes API call to track that a resume was downloaded"
  [{:keys [db]} [_ secure-id job-location-id resume-version]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :resume-downloaded)
                   :params          {:secure-id       secure-id
                                     :job-location-id job-location-id
                                     :resume-version  resume-version}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:http-no-on-success]
                   :on-failure      [:http-no-on-failure]}})

(rf/reg-event-db
  :resume/add-metadata
  add-metadata)

(rf/reg-event-fx
  :resume/resume-metadata-success
  resume-metadata-success)

(rf/reg-event-fx
  :resume/resume-metadata-failure
  resume-metadata-failure)

(rf/reg-event-db
  :resume/add-to-metadata-fetched
  add-to-metadata-fetched)

(rf/reg-event-fx
  :resume/get-resume-metadata
  get-resume-metadata)

(rf/reg-event-fx
  :resume/resume-downloaded
  resume-downloaded)
