(ns recruit-app.modals.saved-search.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.modals.saved-search.db :as db]
            [recruit-app.components.form :as form]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.typography :as type]))

(defn edit-modal
  []
  (let [show-email-interval? (rf/subscribe [:saved-searches/show-email-interval?])
        updated-saved-search-to-edit (rf/subscribe [:saved-searches/updated-saved-search-to-edit])
        saved-search-id (rf/subscribe [:saved-searches/editing-saved-search-search-id])
        valid-name? (rf/subscribe [:saved-searches/valid-name?])
        interval (rf/subscribe [:saved-searches/editing-saved-search-reporting-email-interval])
        can-show-frequency-dropdown? (rf/subscribe [:saved-searches/can-show-frequency-dropdown?])]
    (fn []
      [modal/modal
       :modal-key ::modal/edit-saved-search
       :title (if @saved-search-id "Edit Your Saved Search" "Save Search")
       :on-close #(rf/dispatch [:saved-searches/editing-saved-search-change nil])
       :body [(when-not @can-show-frequency-dropdown?
                [layout/row-top
                 :padding 12
                 :children [[type/error-copy
                             "You have reached the maximum allowed number of emailed searches.
                              If you want to enable emails for this search, please deactivate
                              another emailed search before proceeding."]]])
              [layout/row-top
               :padding 6
               :children [[form/input-text
                           :ns "saved-searches"
                           :type "editing-saved-search-search-name"
                           :label "Search Name"
                           :spec ::db/name
                           :error-msg "Please enter a name"]]]
              [layout/row-bottom
               :padding 6
               :children [[form/single-dropdown
                           :ns "saved-searches"
                           :type "editing-saved-search-reporting-email-frequency-type"
                           :label "Email Frequency"
                           :disabled? (not @can-show-frequency-dropdown?)
                           :choices dd/email-frequency]]]
              (when @show-email-interval?
                [layout/row-bottom
                 :padding 12
                 :children [[form/single-dropdown
                             :ns "saved-searches"
                             :type "editing-saved-search-reporting-email-interval"
                             :label "Email Interval"
                             :choices dd/email-interval]]])]
       :action {:label    "Save"
                :on-click #(rf/dispatch (if @valid-name?
                                          [:saved-searches/save-search @updated-saved-search-to-edit]
                                          [:saved-searches/show-errors?-change true]))}])))