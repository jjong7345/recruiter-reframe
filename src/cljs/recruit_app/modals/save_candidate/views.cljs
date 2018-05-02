(ns recruit-app.modals.save_candidate.views
  (:require [re-frame.core :as rf]
            [recruit-app.projects.views :as projects]
            [recruit-app.components.loading :as l]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.icon :as icon]))

(defn render-project-list
  [{:keys [projectId] :as project}]
  (let [active-candidate-id (rf/subscribe [:projects/active-candidate-id])
        projects-for-candidate (rf/subscribe [:projects/projects-for-active-candidate])
        disabled? (rf/subscribe [:projects/project-disabled? projectId])]
    (fn [{:keys [projectId] :as project}]
      (let [candidate-in-project? (contains? @projects-for-candidate projectId)]
        [projects/project-bar
         :padding 0
         :height "100%"
         :on-click (when-not @disabled?
                     #(rf/dispatch (if candidate-in-project?
                                     [:projects/remove-candidate-from-project @active-candidate-id projectId]
                                     [:projects/add-candidate-to-project @active-candidate-id projectId])))
         :children [[layout/row
                     :padding 0
                     :height "100%"
                     :justify :between
                     :align :center
                     :children [[layout/col-left
                                 :padding 0
                                 :children [[link/hyperlink
                                             :label (:title project)
                                             :on-click #()]]]
                                [layout/col-right
                                 :padding 0
                                 :children [[layout/row
                                             :padding 0
                                             :align :center
                                             :children [[layout/column
                                                         :children [[link/hyperlink
                                                                     :label [:span (str (:candidate_count project))]
                                                                     :on-click #()]]]
                                                        [icon/person
                                                         :disabled? (not candidate-in-project?)]]]]]]]]]))))

(defn project-lists
  []
  (let [data (rf/subscribe [:projects/projects-data])]
    (fn []
      [layout/column
       :padding 0
       :children (mapv
                   (fn [project]
                     ^{:key (:projectId project)}
                     [layout/row
                      :padding-top 12
                      :padding-bottom 0
                      :children [[render-project-list project]]]) @data)])))

(defn projects-modal
  []
  (let [loaded? (rf/subscribe [:projects/page-loaded?])]
    (rf/dispatch [:projects/load-view])
    (fn []
      [modal/modal
       :modal-key ::modal/save-candidate
       :class "save-candidate"
       :title "Save to Project"
       :body [[layout/column
               :padding 36
               :children (if @loaded?
                           [[layout/row
                             :padding 0
                             :children [[projects/create-project]]]
                            [project-lists]]
                           [[l/loading-page]])]]])))
