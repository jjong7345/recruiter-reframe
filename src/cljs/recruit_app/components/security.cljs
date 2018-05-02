(ns recruit-app.components.security
  (:require [re-frame.core :as rf]))

(defn superuser-content
  "Renders given component if user is superuser, otherwise routes to homepage"
  [content]
  (let [superuser? (rf/subscribe [:recruiter/superuser?])]
    (fn [content]
      (if @superuser?
        content
        (rf/dispatch [:go-to-route "/"])))))
