(ns recruit-app.modals.forgot-password-success.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.typography :as type]))

(defn modal
  "Renders modal to show upon successful sending of forgot password email"
  []
  [modal/modal
   :modal-key ::modal/forgot-password-success
   :title "We've sent you a new password link"
   :body [[type/modal-copy "Please check your email. If you don't see it please check your spam or junk folders."]]])
