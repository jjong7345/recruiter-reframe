(ns recruit-app.components.misc
  (:require [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]))

(defn flag
  "Renders small text flag with border"
  [text]
  [:div
   (use-style styles/flag)
   text])

(defn overlay
  "Renders box around elements in full access overlay"
  [& components]
  (into
    [:div (use-style styles/overlay)]
    components))

(defn overlay-holder
  "Renders relative wrapper to hold overlay with anchor"
  [& {:keys [overlay anchor]}]
  [:div
   (use-style styles/overlay-holder)
   overlay
   anchor])
