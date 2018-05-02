(ns recruit-app.modals.candidate-notes.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.form :as form]
            [recruit-app.components.typography :as type]))

(defn modal
  []
  (let [is-saving? (rf/subscribe [:candidate-notes/is-saving?])
        secure-id (rf/subscribe [:candidates/active-id])]
    (fn []
      (rf/dispatch [:candidate-notes/fetch-for-candidate @secure-id])
      [modal/modal
       :modal-key ::modal/candidate-notes
       :title "Candidate Notes"
       :on-close #(rf/dispatch [:candidate-notes/close-modal])
       :body [[layout/column
               :padding 0
               :children [[layout/row-top
                           :padding 6
                           :children [[type/modal-copy "These notes are private - the candidate will not see them."]]]
                          [layout/row-bottom
                           :padding 6
                           :children [[form/input-textarea
                                       :ns "candidate-notes"
                                       :type "notes"
                                       :attr {:rows 14}
                                       :char-limit 5000]]]]]]
       :action {:label       "Save"
                :on-click    #(rf/dispatch [:candidate-notes/save-notes])
                :submitting? @is-saving?}])))
