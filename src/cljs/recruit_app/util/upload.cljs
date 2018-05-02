(ns recruit-app.util.upload
  (:require [re-com.core :as rc]
            [reagent.core :as r]))

(def id "upload")

(defn- on-file-upload
  [on-upload]
  (let [file (aget (js/document.getElementById id) "files" 0)
        reader (js/FileReader.)]
    (.addEventListener reader "load" #(on-upload
                                        (aget % "target" "result")
                                        (aget file "type")
                                        (aget file "name")
                                        (aget file "size")))
    (when file
      (.readAsDataURL reader file))))

(defn- input
  [on-change]
  (fn [on-change]
    [:input {:id        id
             :type      "file"
             :on-change #(on-file-upload on-change)
             :style     {:display "none"}}]))

(defn on-click
  []
  (.click (js/document.getElementById id)))

(defn element
  "Component to wrap HTML file upload in given element.

  Params:
   * :el Element connected to file upload
   * :on-upload Callback to be called upon successful file upload"
  [& {:keys [el on-upload]}]
  (fn [& {:keys [on-upload]}]
    [rc/v-box
     :children [[input on-upload]
                [el]]]))

(defn button
  "Component to wrap HTML file upload in a button.
  Clicking the button will trigger file upload.

  Params:
   * :label Label for the button
   * :on-upload Callback to be called upon successful file upload"
  [& {:keys [label on-upload]}]
  (fn [& {:keys [label on-upload]}]
    [rc/v-box
     :children [[input on-upload]
                [rc/button
                 :label label
                 :on-click on-click]]]))


