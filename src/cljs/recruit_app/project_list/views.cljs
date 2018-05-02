(ns recruit-app.project-list.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [recruit-app.util.candidate :as cu]
            [recruit-app.modals.email.views :as email]
            [recruit-app.util.projects :as p]
            [recruit-app.components.loading :as l]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.table :as table]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.button :as btn]
            [recruit-app.components.typography :as type]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.candidate :as candidate]))

(defn candidate-row
  [idx {:keys [subscriber profile] :as candidate}]
  (let [history-list (p/sorted-history (:experience profile))]
    [[link/table-cell-hyperlink
      :label (str (:firstName subscriber) " " (:lastName subscriber))
      :on-click #(rf/dispatch [:projects/click-candidate idx (:secureId subscriber)])]
     (str (:city subscriber) ", " (:stateProvince subscriber))
     (p/desired-compensation subscriber)
     [candidate/experience-history history-list]]))

(def candidate-headers
  [{:label "Name"
    :sort  {:name (sort-util/sort-fn (comp :lastName :subscriber) compare)}
    :width 17}
   {:label "Location"
    :width 17.6}
   {:label "Desired"
    :width 7.6}
   {:label "Experience"
    :width 51.2}])

(def table-actions
  [{:label    "Email"
    :on-click #(rf/dispatch [:project-list/click-email-candidates %])}
   {:label    "Remove"
    :on-click #(rf/dispatch [::modal/open-modal ::modal/delete-project-candidates])}])

(defn candidates-table
  "Renders candidates for given project"
  []
  [table/frontend-pagination-table
   :table-key ::table/project-candidates
   :data-sub :project-list/active-list-candidates
   :row-data-fn candidate-row
   :headers candidate-headers
   :initial-sort-col :name
   :initial-sort-dir :asc
   :actions table-actions])

(defn delete-candidates-modal
  []
  (let [checked-candidates (rf/subscribe [(table/checked-sub ::table/project-candidates)])]
    (fn []
      [modal/modal
       :modal-key ::modal/delete-project-candidates
       :title (str "Delete Project Candidate" (when (> (count @checked-candidates) 1) "s"))
       :body [[type/modal-copy (cond-> "Are you sure you want to delete "
                                       (> (count @checked-candidates) 1) (str (count @checked-candidates) " candidates?")
                                       (= 1 (count @checked-candidates)) (str "this candidate?"))]]
       :action {:label    "Delete"
                :on-click #(rf/dispatch [:project-list/remove-candidate-list @checked-candidates])}])))

(defn body
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :children [[link/breadcrumb-hyperlink
                           :label "< Back to all Projects"
                           :on-click #(rf/dispatch [:project-list/back-to-all-projects])]]]
              [candidates-table]]])

(defn index
  []
  (rf/dispatch [:project-list/load-view])
  (let [page-loaded? (rf/subscribe [:project-list/page-loaded?])
        project (rf/subscribe [:projects/curr-project])]
    (fn []
      [layout/column
       :class "main"
       :children (if @page-loaded?
                   [[header/page-header
                     :header-text (:title @project)]
                    [layout/page-content [body]]
                    [email/email-modal]
                    [delete-candidates-modal]]
                   [[l/loading-page]])])))