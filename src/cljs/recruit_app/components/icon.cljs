(ns recruit-app.components.icon
  (:require [recruit-app.util.img :as img]
            [recruit-app.styles :as styles]
            [stylefy.core :refer [use-style]]))

(def x-img [[:img
             (merge
               {:src (img/url :stroke-icon-url)}
               (use-style styles/stroke-left))]
            [:img
             (merge
               {:src (img/url :stroke-icon-url)}
               (use-style styles/stroke-right))]])

(defn- clickable-icon
  [& {:keys [on-click children]}]
  (into
    [:a (merge {:on-click on-click} (use-style styles/clickable-icon))]
    children))

(defn pencil
  "Renders a clickable pencil icon"
  [& {:keys [on-click]}]
  [clickable-icon
   :on-click on-click
   :children [[:img
               (merge
                 {:src (img/url :icon-pencil-url)}
                 (use-style styles/pencil-icon-img))]]])

(defn x
  "Renders a clickable x icon"
  [& {:keys [on-click]}]
  [clickable-icon
   :on-click on-click
   :children x-img])

(defn promote
  "Renders a clickable promote job icon"
  [& {:keys [on-click disabled?]}]
  [:a
   (merge
     {:on-click on-click}
     (use-style (if disabled?
                  styles/icon
                  styles/clickable-icon)))
   [:img
    (merge
      {:src (img/url (if disabled? :promote-disabled :promote))}
      (use-style styles/promote-icon-img))]])

(defn modal-x
  "Renders a large clickable X for closing modal"
  [& {:keys [on-click]}]
  [:a
   (merge {:on-click on-click} (use-style styles/modal-x-icon))
   [:img
    (merge
      {:src (img/url :btn-close)}
      (use-style styles/modal-x-img))]])

(defn person
  "Renders person icon"
  [& {:keys [disabled?]}]
  [:img
   (merge
     {:src (img/url (if disabled? :person-disabled :person))}
     (use-style styles/person-icon-img))])
