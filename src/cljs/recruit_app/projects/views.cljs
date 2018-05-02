(ns recruit-app.projects.views
  (:require [re-frame.core :as rf]
            [recruit-app.modals.projects.views :as modals]
            [recruit-app.components.loading :as l]
            [cljs-time.coerce :as c]
            [recruit-app.util.projects :as p]
            [recruit-app.util.date :as date]
            [recruit-app.projects.db :as db]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.table :as table]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.components.icon :as icon]
            [recruit-app.components.hyperlink :as link]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.components.form :as form]
            [recruit-app.components.button :as btn]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.typography :as type]))

(defn project-actions
  [project]
  [layout/row
   :padding 0
   :children [[layout/col-left
               :padding-right 6
               :children [[icon/pencil
                           :on-click #(rf/dispatch [:projects/open-edit-modal project])]]]
              [layout/col-right
               :padding-right 6
               :children [[icon/x
                           :on-click #(rf/dispatch [:projects/open-delete-modal project])]]]]])

(defn row-data
  [_ {:keys [title candidate_count date_created projectId] :as project}]
  [[link/table-cell-hyperlink
    :label title
    :on-click #(rf/dispatch [:go-to-route (str "/projects/" projectId)])]
   candidate_count
   (date/formatted-date :date (c/from-long date_created))
   [project-actions project]])

(def table-headers
  [{:label "Name"
    :sort  {:name (sort-util/sort-fn :title compare)}
    :width 71.9}
   {:label "Candidates"
    :sort  {:candidate-count (sort-util/sort-fn :candidate_count compare)}
    :width 11.8}
   {:label "Created"
    :sort  {:date-created (sort-util/sort-fn (comp c/from-long :date_created) sort-util/before?)}
    :width 8.7}
   {:label "Edit"
    :width 7.6}])

(defn table
  []
  [table/frontend-pagination-table
   :table-key ::table/projects
   :headers table-headers
   :row-data-fn row-data
   :initial-sort-col :date-created
   :initial-sort-dir :desc
   :data-sub :projects/projects-vector])

(defn create-project-input
  "Renders either create project bar or input with action buttons"
  []
  (let [form-valid? (rf/subscribe [:projects/create-project-form-valid?])]
    (fn []
      [layout/row
       :padding 0
       :justify :between
       :children [[layout/col-left
                   :children [[form/input-text
                               :ns "projects"
                               :type "new-title"
                               :class "create-project-input"
                               :height :tall
                               :spec ::db/name
                               :error-msg "Please enter a project name"
                               :placeholder "New project name"]]]
                  [layout/column
                   :padding 0
                   :children [[btn/primary-button
                               :label "Create"
                               :on-click #(rf/dispatch (if @form-valid?
                                                         [:projects/on-add-project]
                                                         [:projects/show-errors?-change true]))]]]
                  [layout/col-right
                   :children [[btn/secondary-button
                               :label "Cancel"
                               :on-click #(rf/dispatch [:projects/clear-project-form])]]]]])))

(defn project-bar
  "Renders container with project-bar styling"
  [& {:keys [on-click] :as params}]
  [:div
   (merge (use-style styles/create-project-bar) {:on-click on-click})
   (into
     [layout/row]
     (mapcat identity (dissoc params :on-click)))])

(defn create-project-bar
  "Renders action bar to be clicked to render input"
  []
  [layout/column
   :padding 0
   :width "100%"
   :children [[project-bar
               :padding 12
               :on-click #(rf/dispatch [:projects/toggle-show-create-project-input?])
               :children [[layout/col-right
                           :padding 0
                           :children [[link/hyperlink
                                       :label "+ Create New Project"
                                       :on-click #()]]]]]]])

(defn create-project
  "Renders either input or bar depending on sub"
  []
  (let [show-input-field? (rf/subscribe [:projects/show-create-project-input?])]
    (fn []
      (if @show-input-field?
        [create-project-input]
        [create-project-bar]))))

(defn body
  []
  [layout/column
   :padding 0
   :children [[layout/row
               :padding-top 30
               :padding-bottom 24
               :children [[create-project]]]
              [table]]])

(defn delete-modal
  []
  (let [title (rf/subscribe [:projects/editing-project-title])]
    (fn []
      [modal/modal
       :modal-key ::modal/delete-project
       :title "Delete Project"
       :body [[layout/row-top
               :padding 3
               :children [[type/modal-copy (str "Are you sure you want to delete " @title "?")]]]
              [layout/row-bottom
               :padding 3
               :children [[type/modal-copy "All candidates you have saved in this project will be lost."]]]]
       :action {:label    "Delete"
                :on-click #(rf/dispatch [:projects/delete-project])}])))

(defn index
  []
  (rf/dispatch [:projects/load-view])
  [layout/column
   :padding 0
   :class "projects main content-holder"
   :children [[header/page-header
               :header-text "Projects"
               :sub-header-text "Use Projects to group saved candidates"]
              [layout/page-content [body]]
              [modals/edit-modal]
              [delete-modal]]])
