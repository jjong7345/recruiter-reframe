(ns recruit-app.components.alert
  (:require [recruit-app.styles :as styles]
            [stylefy.core :refer [use-style]]
            [recruit-app.components.typography :as type]))

(defn error
  "Renders error message"
  [message]
  [:div
   (use-style styles/error-alert)
   message])
