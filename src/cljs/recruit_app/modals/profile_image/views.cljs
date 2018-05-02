(ns recruit-app.modals.profile-image.views
  (:require [re-frame.core :as rf]
            [recruit-app.util.cropper :as c]
            [recruit-app.components.modal :as modal]))

(defn modal
  []
  (let [image-url (rf/subscribe [:profile-image/image-url])
        image-type (rf/subscribe [:profile-image/image-type])
        data (rf/subscribe [:profile-image/crop-data])]
    (fn []
      [modal/modal
       :modal-key ::modal/profile-image
       :class "profile-image-modal"
       :body [[c/cropper
               :id "cropper"
               :aspect-ratio 1
               :image-url @image-url
               :image-type @image-type
               :on-change #(rf/dispatch [:profile-image/crop-data-change %])
               :on-finish #(rf/dispatch [:profile-image/upload-image %1 %2])]]])))
