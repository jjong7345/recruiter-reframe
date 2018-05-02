(ns recruit-app.modals.checkout.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [reagent.core :as reagent]))


(defn ^:export update-page-step
  [step]
  (rf/dispatch [:checkout/on-page-step-update step]))

(defn ^:export handle-post-message
  [e]
  (case e.data.method
    "update-page-step" (rf/dispatch [:checkout/on-page-step-update e.data.step])
    "promote-job" (rf/dispatch [:checkout/promote-publish])
    "default"))

(defn ^:export close-modal
  []
  (rf/dispatch [:checkout/show-modal-change false]))

(defn modal-iframe
  [url]
  (fn [url]
      [:div {:class "modal-window modal-box"}
       [:iframe {:class "iframe-window"
                 :id "iframe"
                 :src url}]
       [:div {:class "modal-close"
              :on-click #(close-modal)}]]))



(defn render-modal
  []
  (let [open-modal? (rf/subscribe [:checkout/open-modal])
        url (rf/subscribe [:checkout/modal-url])]
    (fn []
      (when @open-modal?
        [rc/modal-panel
         :class "iframe-container"
         :child [modal-iframe @url]
         :wrap-nicely? false]))))

(defn checkout-modal
  []
  (rf/dispatch [:checkout/get-modal-url])
  [render-modal])

(.addEventListener js/window "message" (fn [e] (handle-post-message e)) false)




















