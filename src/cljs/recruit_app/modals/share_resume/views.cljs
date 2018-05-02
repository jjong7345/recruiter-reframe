(ns recruit-app.modals.share-resume.views
  (:require [re-frame.core :as rf]
            [cljs.spec.alpha :as s]
            [recruit-app.modals.share-resume.db :as specs]
            [recruit-app.components.form :as form]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.modal :as modal]))

(defn modal
  []
  (let [share-resume-form (rf/subscribe [:share-resume/form-values])]
    (fn []
      (let [is-valid? (s/valid? ::specs/share-resume @share-resume-form)]
        [modal/modal
         :modal-key ::modal/share-resume
         :title "Share Resume"
         :body [[layout/column
                 :padding 36
                 :children [[layout/row-top
                             :padding 6
                             :children [[form/input-text
                                         :ns "share-resume"
                                         :type "emails"
                                         :spec ::specs/emails
                                         :error-msg "Invalid email addresses."
                                         :label "Email Address(es)"
                                         :flag "separated with commas"
                                         :placeholder "email address"]]]
                            [layout/row-bottom
                             :padding 6
                             :children [[form/input-textarea
                                         :ns "share-resume"
                                         :type "message"
                                         :label "Message"
                                         :flag "optional"
                                         :attr {:rows 8}
                                         :placeholder "write a message here"]]]]]]
         :action {:label    "Share"
                  :on-click #(rf/dispatch (if is-valid? [:share-resume/share] [:share-resume/show-errors?-change true]))}]))))
