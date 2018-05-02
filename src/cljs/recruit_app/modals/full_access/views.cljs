(ns recruit-app.modals.full-access.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.typography :as type]))

(defn thank
  "Renders modal on sending message to account executive"
  []
  [modal/modal
   :modal-key ::modal/full-access-success
   :title "Thank you"
   :body [[type/modal-copy "An Account Executive will be in touch with you shortly."]]
   :action {:label    "Close"
            :on-click #(rf/dispatch [::modal/close-modal ::modal/full-access-success])}])