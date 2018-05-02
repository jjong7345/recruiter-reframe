(ns recruit-app.util.quill
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]))

(defn quill-toolbar [id]
  [:div {:id (str "quill-toolbar-" id) :tabIndex "-1"}

   [:span {:class "ql-formats"}
    [:button {:class "ql-bold" :tabIndex "-1"}]
    [:button {:class "ql-italic" :tabIndex "-1"}]
    [:button {:class "ql-underline":tabIndex "-1"}]]

   [:span {:class "ql-formats"}
    [:select {:class "ql-align" :tabIndex "-1"}]]

   [:span {:class "ql-formats"}
    [:button {:class "ql-list" :value "ordered" :tabIndex "-1"}]
    [:button {:class "ql-list" :value "bullet" :tabIndex "-1"}]]

   [:span {:class "ql-formats"}
    [:button {:class "ql-indent" :value "-1" :tabIndex "-1"}]
    [:button {:class "ql-indent" :value "+1" :tabIndex "-1"}]]])


(defn editor [{:keys [id content selection on-change-fn]}]
  (let [this (r/atom nil)
        value #(aget @this "container" "firstChild" "innerHTML")]
    (r/create-class
      {:component-did-mount
                     (fn [component]
                       (reset! this
                               (js/Quill.
                                 (aget (.-children (r/dom-node component)) 1)
                                 #js {:modules     #js {:toolbar (aget (.-children (r/dom-node component)) 0)}
                                      :theme       "snow"
                                      :placeholder "Write or paste in job description here..."}))

                       (.on @this "text-change"
                            (fn [delta old-delta source]
                              (on-change-fn source (value))))

                       (if (= selection nil)
                         (.setSelection @this nil)
                         (.setSelection @this (first selection) (second selection) "api")))

       :component-will-receive-props
                     (fn [component next-props]
                       (if
                         (or
                           (not= (:content (second next-props)) (value))
                           (not= (:id (r/props component)) (:id (second next-props))))
                         (do
                           (if (= selection nil)
                             (.setSelection @this nil)
                             (.setSelection @this (first selection) (second selection) "api"))
                           (.pasteHTML @this (:content (second next-props))))))

       :display-name (str "quill-editor-" id)

       :reagent-render
                     (fn []
                       [:div {:id (str "quill-wrapper-" id)}
                        [quill-toolbar id]
                        [:div {:id                      (str "quill-editor-" id)
                               :class                   "quill-editor"
                               :dangerouslySetInnerHTML {:__html content}}]])})))


(defn display-area [{:keys [id content]}]
  (let [this (r/atom nil)]
    (r/create-class
      {:component-did-mount
                     (fn [component]
                       (reset! this (js/Quill. (r/dom-node component)
                                               #js {:theme       "snow"
                                                    :placeholder "Compose an epic..."}))
                       (.disable @this))

       :component-will-receive-props
                     (fn [component next-props]
                       (.pasteHTML @this (:content (second next-props))))

       :display-name (str "quill-display-area-" id)

       :reagent-render
                     (fn []
                       [:div {:id                      (str "quill-display-area-" id)
                              :class                   "quill-display-area"
                              :dangerouslySetInnerHTML {:__html content}}])})))