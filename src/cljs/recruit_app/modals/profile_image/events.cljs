(ns recruit-app.modals.profile-image.events
  (:require [recruit-app.util.events :as ev]
            [recruit-app.events :as events]
            [clojure.string :as str]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [recruit-app.util.ajax :as a]
            [cljs-time.core :as t]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "profile-image" ["crop-data" "image-url" "image-type" "image-filename"])

(defn open-modal
  "Sets initial image values and opens modal"
  [_ [_ url type filename size]]
  (let [is-image? (= "image" (-> type
                                 (str/split #"/")
                                 (nth 0)))
        is-valid-filesize? (> 15728640 size)]
    (if (and is-image? is-valid-filesize?)
      {:dispatch-n [[:profile-image/image-url-change url]
                    [:profile-image/image-type-change type]
                    [:profile-image/image-filename-change filename]
                    [::modal/open-modal ::modal/profile-image]]}
      {:dispatch [:alerts/add-error "Upload Failed: That is not a valid upload type or file size"]})))

(defn handle-upload-response
  "Handles response from uploading image"
  [_ [_ response]]
  {:dispatch-n [[:account/toggle-is-uploading?]
                [:recruiter/profile-img-last-update-change (t/now)]
                [:recruiter/has-photo?-change true]]})

(defn upload-image
  "Uploads img using blob and sets profile img url"
  [{:keys [db]} [_ url blob]]
  {:dispatch-n [[:account/toggle-is-uploading?]
                [::modal/close-modal ::modal/profile-image]
                [:account/profile-img-url-change url]]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :upload-image (-> db :recruiter :recruiter-id))
                   :params          {:imageProfile {:blob     blob
                                                    :filename (-> db :profile-image :image-filename)}}
                   :format          (a/multipart-form-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:profile-image/handle-upload-response]
                   :on-failure      [:profile-image/handle-upload-response]}})

(events/reg-event-fx
  :profile-image/open-modal
  open-modal)

(events/reg-event-fx
  :profile-image/handle-upload-response
  handle-upload-response)

(events/reg-event-fx
  :profile-image/upload-image
  upload-image)
