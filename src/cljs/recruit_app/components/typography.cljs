(ns recruit-app.components.typography
  (:require [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]))

(defn body-copy-grey
  "Renders grey body copy"
  [text]
  [:div
   (use-style styles/body-copy-grey)
   text])

(defn modal-copy
  "Returns simple div with body copy styling"
  [text]
  [:div
   (use-style styles/modal-copy)
   text])

(defn body-copy-light
  "Returns simple div with light body copy styling"
  [text]
  [:div
   (use-style styles/body-copy-light)
   text])

(defn error-copy
  "Returns div with error copy styling"
  [text]
  [:div
   (use-style styles/error-copy)
   text])
