(ns recruit-app.util.cropper
  (:require [reagent.core :as r]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [re-com.core :as rc]))

(defn- crop-data
  [js-object]
  (-> js-object
      (aget "detail")
      (js->clj)
      (keywordize-keys)))

(defn- on-crop-finish
  "Will call given on-finish function with data url of cropped image and blob of cropped image data"
  [canvas on-finish image-type]
  (.toBlob canvas (partial on-finish (.toDataURL canvas image-type)) image-type))

(defn cropper
  "Creates a cropper with the following options:

  Params:
   * :id HTML id selector for img tag
   * :image-url URL to image to be cropped
   * :image-type Mime-type of img to be cropped
   * :aspect-ratio Aspect ratio for crop-box within cropper
   * :on-change Function to be run on every crop change
   * :on-finish Function to be run upon crop finish"
  [& {:keys [id image-url image-type aspect-ratio on-change on-finish]}]
  (let [this (r/atom nil)]
    (r/create-class
      {:component-did-mount
                     (fn [component]
                       (reset! this
                               (js/Cropper.
                                 (js/document.getElementById id)
                                 #js {:aspectRatio      aspect-ratio
                                      :crop             #(on-change (crop-data %))
                                      :viewMode         1
                                      :dragMode         "move"
                                      :guides           false
                                      :center           false
                                      :cropBoxResizable false
                                      :cropBoxMovable   false})))

       :display-name (str "cropper-" id)

       :reagent-render
                     (fn [& {:keys [id image-url image-type aspect-ratio on-change on-finish]}]
                       [rc/v-box
                        :class "cropper-holder"
                        :children [[:div.img-holder
                                    [:img {:id  id
                                           :src image-url}]]
                                   [rc/h-box
                                    :justify :between
                                    :children [[rc/h-box
                                                :class "zoom-holder"
                                                :children [[rc/button
                                                            :label "-"
                                                            :class "zoom"
                                                            :on-click #(.zoom @this -0.1)]
                                                           [rc/button
                                                            :label "+"
                                                            :class "zoom"
                                                            :on-click #(.zoom @this 0.1)]]]
                                               [rc/button
                                                :label "Crop"
                                                :class "crop-btn"
                                                :on-click #(on-crop-finish (.getCroppedCanvas @this) on-finish image-type)]]]]])})))
