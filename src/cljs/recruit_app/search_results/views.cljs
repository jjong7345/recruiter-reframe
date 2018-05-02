(ns recruit-app.search-results.views
  (:require [re-frame.core :as rf]
            [recruit-app.util.uri :as u]
            [recruit-app.search.views :as search]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.table :as table]
            [recruit-app.components.button :as btn]
            [recruit-app.components.form :as form]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.misc :as misc]
            [recruit-app.modals.saved-search.views :as ss-modal]
            [re-com.core :as rc]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.loading :as lo]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.util.candidate :as cu]
            [recruit-app.util.date :as d]
            [recruit-app.components.candidate :as candidate]
            [cljs-time.coerce :as c]
            [recruit-app.util.search :as su]
            [recruit-app.components.typography :as type]
            [recruit-app.components.alert :as alert]))

(def filters
  {:keywords   {:label "Skills"
                :form  search/skills}
   :location   {:label "Location"
                :form  search/location-form}
   :profile    {:label "Profile"
                :form  search/profile-form}
   :salary     {:label "Salary"
                :form  search/salary-slider}
   :experience {:label "Experience"
                :form  search/experience-inputs}
   :education  {:label "Education"
                :form  search/education-form}})

(def removable-filter-subs
  "Map of data pertaining to filters that can be removed from search"
  [[:keywords :search-results/keywords-filter-label]
   [:location :search-results/location-filter-label]
   [:title :search-results/title-filter-label]
   [:role :search-results/role-label]
   [:candidate-name :search-results/candidate-name-filter-label]
   [:salary :search-results/salary-filter-label]
   [:years-experience :search-results/years-experience-label]
   [:company :search-results/company-filter-label]
   [:education :search-results/education-filter-label]
   [:school :search-results/school-filter-label]])

(def headers
  [{:label "Name"
    :width 22.1}
   {:label "Location"
    :width 19.6}
   {:label "Salary"
    :width 7.9}
   {:label "Experience"
    :width 37.3}
   {:label "Last Active"
    :width 13.1}])

(defn- member-experience
  "Converts experience history to proper member experience format"
  [{:keys [title companyName startDate endDate]}]
  {:title      title
   :company    companyName
   :start-date (when startDate (d/db-date-string (c/from-long startDate)))
   :end-date   (when endDate (d/db-date-string (c/from-long endDate)))})

(defn blurred-row
  [idx]
  [:td
   {:colSpan (count headers)}
   [:img {:src (u/uri :blur-row (mod idx 3))}]])

(defn row-data
  "Returns row data for given candidate"
  [blur-candidate? idx {:keys [candidateName location compensation lastLogin lastEmailOpen
                               lastResumeUpdate profile secureId]}]
  (if (blur-candidate? idx)
    [table/row
     :cells [[blurred-row idx]]]
    (let [days-since-active (d/days-passed-since (cu/last-active-date lastLogin lastEmailOpen lastResumeUpdate))]
      (conj
        (table/wrap-row
          [[link/table-cell-hyperlink
            :label candidateName
            :on-click #(rf/dispatch [:search/click-candidate secureId idx])]
           location
           compensation
           [candidate/experience-history (map member-experience (:experience profile))]
           (when days-since-active
             (case days-since-active
               0 "Today"
               1 "Yesterday"
               (str days-since-active " days ago")))])
        :on-click
        #(rf/dispatch [:search/click-candidate secureId idx])))))

(defn results
  []
  (let [page (rf/subscribe [:search-results/page])
        params (rf/subscribe [:search-results/search-params])
        premium? (rf/subscribe [:recruiter/full-access])
        view (rf/subscribe [:search-results/view])
        approved? (rf/subscribe [:recruiter/approved?])]
    (fn []
      [table/backend-pagination-table
       :table-key ::table/search-results
       :fetch-url (u/uri :search)
       :fetch-params params
       :initial-page (or @page 0)
       :row-data-fn (partial row-data #(su/blur-candidate? % @premium? @view @approved?))
       :on-page-change #(rf/dispatch [:search-results/go-to-page %])
       :headers headers])))

(defn filter-popover-actions
  []
  (let [valid? (rf/subscribe [:search/minimum-required-inputs?])]
    (fn []
      [layout/row
       :padding 0
       :justify :between
       :children [[link/hyperlink
                   :label "Cancel"
                   :on-click #(rf/dispatch [:search-results/cancel-filter-click])]
                  [link/hyperlink
                   :label "Apply"
                   :on-click (when @valid?
                               #(rf/dispatch [:search-results/apply-filter-click]))]]])))

(defn filter-btn
  [[filter-key {:keys [label form]}]]
  (let [active? (rf/subscribe [:search-results/active-filter? filter-key])
        selected-filter? (rf/subscribe [:search-results/selected-filter? filter-key])]
    (fn []
      [layout/col-left
       :padding 18
       :children [[rc/popover-anchor-wrapper
                   :position :below-right
                   :showing? selected-filter?
                   :anchor [(if @active? btn/primary-dashboard-button btn/secondary-dashboard-button)
                            :label label
                            :on-click #(rf/dispatch [:search-results/selected-filter-change filter-key])]
                   :popover [rc/popover-content-wrapper
                             :width "425px"
                             :on-cancel #(rf/dispatch [:search-results/cancel-filter-click])
                             :body [layout/column
                                    :padding 18
                                    :children [[layout/row
                                                :padding 18
                                                :children [[form]]]
                                               [layout/row
                                                :padding 18
                                                :children [[filter-popover-actions]]]]]]]]])))

(defn filter-option
  [[filter-key sub]]
  (let [value (rf/subscribe [sub])
        removable? (rf/subscribe [:search-results/filter-removable? filter-key])]
    (fn []
      (when @value
        [layout/col-left
         :padding 16
         :children [[form/removable-option
                     :label @value
                     :on-remove (if @removable?
                                  #(rf/dispatch [:search-results/remove-filter filter-key])
                                  #(rf/dispatch [:search-results/flash-minimum-criteria-error]))]]]))))

(defn search-filters
  []
  [layout/row
   :padding 0
   :children (mapv (partial vector filter-btn) filters)])

(defn sort-dropdown
  []
  [form/single-dropdown
   :ns "search-results"
   :type "sort-by"
   :class "search-sort-by"
   :choices [{:id :recency :label "Recency"}
             {:id :relevance :label "Relevance"}]])

(defn view-dropdown
  []
  [form/single-dropdown
   :ns "search-results"
   :type "view"
   :class "search-view"
   :choices [{:id :all :label "All"}
             {:id :viewed :label "Viewed"}
             {:id :contacted :label "Contacted"}
             {:id :unviewed :label "Unviewed"}
             {:id :uncontacted :label "Uncontacted"}]])

(defn saved-search-decoy
  []
  [:div.component-dropdown.saved-searches
   (use-style styles/single-dropdown)
   [:div.rc-dropdown.chosen-container.chosen-container-single.noselect
    [:a.chosen-single.chosen-default
     [:span.loader-container "Saved Searches"
      [lo/loading-circle-tiny
       :class "saved-search-loading-ani"]]]]])

(defn saved-search-dropdown
  []
  (let [saved-searches (rf/subscribe [:saved-searches/saved-searches-vector])
        fetching? (rf/subscribe [:saved-searches/fetching-all?])]
    (rf/dispatch [:saved-searches/fetch-saved-searches])
    (fn []
      (if @fetching?
        [saved-search-decoy]
        [form/single-dropdown
         :ns "search-results"
         :type "saved-search-id"
         :class "saved-searches"
         :placeholder "Saved searches"
         :choices saved-searches
         :id-fn :search-id
         :label-fn :search-name]))))

(defn header
  []
  (let [total (rf/subscribe [(table/total-sub ::table/search-results)])
        saved-search-id (rf/subscribe [:search-results/saved-search-id])
        saved-search (rf/subscribe [:search-results/criteria])
        approved? (rf/subscribe [:recruiter/approved?])
        show-minimum-criteria-error? (rf/subscribe [:search-results/show-minimum-criteria-error?])]
    (fn []
      [layout/row
       :padding-top (if @show-minimum-criteria-error? 0 30)
       :padding-bottom 14
       :justify :between
       :children [[layout/column
                   :padding 0
                   :children [[header/header-2 (str (if (> @total 999) "999+" @total) " Candidate" (when-not (= @total 1) "s"))]]]
                  (when @approved?
                    [layout/column
                     :padding 0
                     :children [[layout/row
                                 :padding 0
                                 :align :center
                                 :children [[layout/col-left
                                             :padding 12
                                             :children [[link/hyperlink
                                                         :label "Save this search"
                                                         :on-click #(rf/dispatch [:saved-searches/open-save-new-modal @saved-search])]]]
                                            (when @saved-search-id
                                              [layout/column
                                               :padding 12
                                               :children [[link/hyperlink
                                                           :label "Update this search"
                                                           :on-click #(rf/dispatch [:saved-searches/open-edit-modal @saved-search])]]])
                                            [layout/col-right
                                             :padding 0
                                             :children [[saved-search-dropdown]]]]]]])]])))

(defn full-access-page-overlay
  [page]
  (let [total-candidates (rf/subscribe [(table/total-sub ::table/search-results)])]
    (fn [page]
      [misc/overlay
       (if (> page 0)
         [header/header-3 (str "View " (str @total-candidates) " more quality candidates.")]
         [header/header-3 "Don’t miss out on these top candidates."])
       [layout/row-bottom
        :padding 24
        :children [(if (> page 0)
                     [type/body-copy-light "Find the right candidates in less time with our Full Access product suite."]
                     [type/body-copy-light "Upgrade to Full Access to see the top 3 results for your search."])]]
       [layout/row
        :padding-top 28
        :padding-bottom 36
        :justify :center
        :children [[btn/primary-button
                    :label "Get Full Access Now"
                    :on-click #(rf/dispatch [:search/click-fa])]]]])))

(defn full-access-overlay []
  (let [current-page (rf/subscribe [(table/page-number-sub ::table/search-results)])
        show-fa-overlay? (rf/subscribe [:search-results/show-fa-overlay?])]
    (fn []
      (when @show-fa-overlay?
        [full-access-page-overlay @current-page]))))

(defn fa-overlay
  []
  (let [table-registered? (rf/subscribe [::table/pagination-registered? ::table/search-results])]
    (fn []
      (when @table-registered? [full-access-overlay]))))

(defn pending-user-overlay []
  [misc/overlay
   [header/header-3 "Your account is still pending"]
   [layout/row
    :padding-top 24
    :padding-bottom 40
    :children [[type/body-copy-light "You will have access to run this search when your profile is approved."]]]])

(defn body
  []
  (let [approved? (rf/subscribe [:recruiter/approved?])
        show-minimum-criteria-error? (rf/subscribe [:search-results/show-minimum-criteria-error?])]
    (fn []
      [layout/column
       :padding 0
       :children [(when @show-minimum-criteria-error?
                    [layout/row
                     :padding-top 18
                     :padding-bottom 7
                     :children [[alert/error "Sorry but you need to have at least a skill, job title, candidate name, or company name to be able to perform a search."]]])
                  [header]
                  [layout/row-top
                   :padding-bottom 10
                   :justify :between
                   :align :end
                   :children [[layout/column
                               :padding 0
                               :children [[layout/row-top
                                           :padding 6
                                           :children [[header/info-header "Filter Results:"]]]
                                          [search-filters]]]
                              [layout/col-left
                               :padding 18
                               :children [[layout/row-top
                                           :padding 2
                                           :children [[form/form-label
                                                       :label "View:"]]]
                                          [view-dropdown]]]
                              [layout/column
                               :padding 0
                               :children [[layout/row-top
                                           :padding 2
                                           :children [[form/form-label
                                                       :label "Sort By:"]]]
                                          [sort-dropdown]]]]]
                  [layout/wrapping-row-with-children
                   :padding 0
                   :children (mapv (partial vector filter-option) removable-filter-subs)]
                  [layout/row-bottom
                   :padding 9
                   :children [[misc/overlay-holder
                               :overlay (if (not @approved?)
                                          [pending-user-overlay]
                                          [fa-overlay])
                               :anchor [results]]]]]])))

(defn index
  [body-content]
  [layout/column
   :padding 0
   :children [[layout/page-content body-content]
              [ss-modal/edit-modal]]])

(defn saved-search-view
  "Waits for saved search criteria to load before rendering table"
  []
  (let [saved-search-loaded? (rf/subscribe [:search-results/saved-search-loaded?])]
    (rf/dispatch [:search-results/load-saved-search])
    (fn []
      (when @saved-search-loaded? [body]))))

(defn unsaved-view
  []
  (let [criteria-valid? (rf/subscribe [:search-results/search-criteria-valid?])]
    (fn []
      (if @criteria-valid?
        [index [body]]
        (rf/dispatch [:go-to-route "/search"])))))

(defn saved-view
  []
  [index [saved-search-view]])
