(ns recruit-app.modals.referral-hiring.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.typography :as type]))

(defn success-modal
  []
  [modal/modal
   :modal-key ::modal/referral-hiring-success
   :title "Thank you for signing up!"
   :body [[type/modal-copy "We'll reach out to you soon."]]
   :action {:label    "Close"
            :on-click #(rf/dispatch [::modal/close-modal ::modal/referral-hiring-success])}])
