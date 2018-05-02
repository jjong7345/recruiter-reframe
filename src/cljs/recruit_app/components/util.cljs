(ns recruit-app.components.util
  (:require [stylefy.core :refer [use-style]]))

(defn recom-component-with-styles
  "Applies params and styles to given component"
  [component params styles]
  (let [{:keys [class style]} (use-style styles)
        mapped-params (apply hash-map params)]
    (apply
      vector
      (concat
        [component]
        (-> mapped-params
            (update :class #(str % " " class))
            (assoc :style style)
            (->> (mapcat identity)))))))
