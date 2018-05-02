(ns recruit-app.modals.projects.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.projects.db :as db]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.typography :as type]
            [recruit-app.components.form :as form]))

(defn edit-modal
  []
  (let [projectId (rf/subscribe [:projects/editing-project-projectId])
        valid? (rf/subscribe [:projects/editing-form-valid?])]
    (fn []
      [modal/modal
       :modal-key ::modal/rename-project
       :on-close #(rf/dispatch [:projects/close-modal])
       :title "Rename Project"
       :body [[layout/column
               :padding 36
               :children [[form/input-text
                           :ns "projects"
                           :type "editing-project-title"
                           :label "Name"
                           :height :tall
                           :spec ::db/name
                           :error-msg "Please enter a project name"]]]]
       :action {:label    "Rename"
                :on-click #(rf/dispatch (if @valid?
                                          [:projects/edit-project]
                                          [:projects/show-errors?-change true]))}])))
