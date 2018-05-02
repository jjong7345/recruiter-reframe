(ns recruit-app.job.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [clojure.set :refer [subset? rename-keys]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [recruit-app.util.pagination :as pag]
            [hickory.core :as h]
            [goog.string :as gs]
            [clojure.string :as string]
            [recruit-app.post-job.preview.views :as preview]
            [recruit-app.util.input-view :as iv]
            [recruit-app.modals.email.views :as email]
            [recruit-app.util.candidate :as cu]
            [cljs-time.format :as f]
            [recruit-app.util.img :as img]
            [recruit-app.components.loading :as l]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.table :as table]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.util.date :as d]
            [cljs-time.coerce :as c]
            [recruit-app.components.button :as btn]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.typography :as typography]
            [recruit-app.components.candidate :as candidate]))

(def job-tab
  [{:id :tab0 :label "Preview" :text "Preview"}
   {:id :tab1 :label "Applicants" :text "Applicants"}
   {:id :tab2 :label "Views" :text "Views"}])

(defn job-tabs
  []
  (let [selected-tab-id (rf/subscribe [:job/active-tab])
        job-id (rf/subscribe [:job/active-job-id])
        job-loc-id (rf/subscribe [:job/active-job-loc-selection])]
    (fn []
      [rc/horizontal-tabs
       :model @selected-tab-id
       :tabs job-tab
       :on-change #(rf/dispatch (case %
                                  :tab0 [:job/prev-for-job @job-id]
                                  :tab1 [:job/apps-for-job @job-id @job-loc-id]
                                  :tab2 [:job/views-for-job @job-id @job-loc-id]))])))

(defn preview-tab
  []
  [:div [preview/preview]])

(defn- location-text
  "Displays text of city/state of applicants"
  [{:keys [city state]}]
  (when (and city state (seq city) (seq state))
    (str city ", " state)))

(defn- member-experience
  "Converts history item into proper member format"
  [history-item]
  (rename-keys history-item {:company_name :company
                             :start_date   :start-date
                             :end_date     :end-date}))

(defn candidate-row-data
  [idx {:keys [jobSeekerFirstName jobSeekerLastName secure-id
               jobSeekerDesired jobSeekerHistory location]}]
  (let [history-list (sort-by :start_date > jobSeekerHistory)]
    [[link/table-cell-hyperlink
      :label (str jobSeekerFirstName " " jobSeekerLastName)
      :on-click #(rf/dispatch [:job/click-candidate secure-id idx])]
     (location-text location)
     jobSeekerDesired
     [candidate/experience-history (map member-experience history-list)]]))

(defn applicant-row-data
  "Adds application time to applicant row"
  [idx {:keys [apply_time] :as applicant}]
  (conj
    (candidate-row-data idx applicant)
    (when apply_time
      (d/formatted-date :month-and-date (d/db-date-time apply_time)))))

(def applicant-headers
  [{:label "Name"
    :width 15}
   {:label "Location"
    :width 17}
   {:label "Desired"
    :width 7.6}
   {:label "Experience"
    :width 45.3}
   {:label "Applied"
    :sort  {:application-date (sort-util/sort-fn (comp d/db-date-time :apply_time) sort-util/before?)}
    :width 11.3}])

(def viewer-headers
  [{:label "Name"
    :width 17}
   {:label "Location"
    :width 17.6}
   {:label "Desired"
    :width 7.6}
   {:label "Experience"
    :width 51.2}])

(def table-actions
  [{:label    "Email"
    :on-click #(rf/dispatch [:job/click-email-candidates %])}])

(defn applicants-tab
  []
  [layout/column
   :padding 0
   :children [[layout/row-bottom
               :padding-top 24
               :children [[table/frontend-pagination-table
                           :table-key ::table/job-applicants
                           :headers applicant-headers
                           :row-data-fn applicant-row-data
                           :data-sub :job/active-job-applicants
                           :actions table-actions]]]]])

(defn views-tab
  []
  [layout/column
   :padding 0
   :children [[layout/row-bottom
               :padding-top 24
               :children [[table/frontend-pagination-table
                           :table-key ::table/job-viewers
                           :headers viewer-headers
                           :row-data-fn candidate-row-data
                           :data-sub :job/active-job-viewers
                           :actions table-actions]]]]])

(defn tabs-body
  []
  (let [active-tab (rf/subscribe [:job/active-tab])]
    (fn []
      (case @active-tab
        :tab0 [preview-tab]
        :tab1 [applicants-tab]
        :tab2 [views-tab]
        [:div "Oops, something went wrong, please refresh the page."]))))

(defn job-locations []
  (let [locations (rf/subscribe [:job/job-loc-choices])
        job-loc (rf/subscribe [:job/active-job-loc-selection])]
    (fn []
      [rc/single-dropdown
       :width "auto"
       :choices @locations
       :model @job-loc
       :on-change #(rf/dispatch [:job/active-job-loc %])])))

(defn job-title [title]
  [rc/label
   :class "job-title"
   :label (gs/unescapeEntities title)])

(defn header []
  (let [job (rf/subscribe [:job/active-job])]
    (fn []
      (let [title (:job-title @job)]
        (when (seq title)
          [rc/h-box
           :class "title-container"
           :children [[rc/v-box
                       :class "title-box"
                       :justify :between
                       :children [[rc/hyperlink
                                   :label "< Back to all Jobs"
                                   :class "back-to-jobs"
                                   :on-click #(rf/dispatch [:job/back])]
                                  [rc/h-box
                                   :class "title"
                                   :style {:flex-flow "row wrap"}
                                   :children [[job-title title]
                                              [:span.title-in " in "]
                                              [job-locations]]]
                                  [job-tabs]]]]])))))

(defn index
  []
  (rf/dispatch [:job/load-view])
  (let [page-loaded? (rf/subscribe [:job/is-active-job-fetched?])]
    (fn []
      (if @page-loaded?
        [rc/v-box
         :class "job main"
         :children [[header]
                    [layout/page-content [tabs-body]]
                    [email/email-modal]]]
        [l/loading-page]))))