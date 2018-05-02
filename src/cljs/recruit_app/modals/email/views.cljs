(ns recruit-app.modals.email.views
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [recruit-app.util.dropdowns :as dd]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [recruit-app.util.input-view :as iv]
            [cljs.spec.alpha :as s]
            [recruit-app.util.email :as e]
            [recruit-app.util.img :as img]
            [recruit-app.components.button :as btn]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.typography :as type]
            [recruit-app.components.modal :as modal]))

(defn email-title
  [rec-count recipient]
  (cond-> "Contact "
          (= 1 rec-count) (str (:jobSeekerFirstName recipient) " " (:jobSeekerLastName recipient))
          (< 1 rec-count) (str rec-count " Candidates")))

(def email-input-view (partial iv/input-view "email"))

(defn subject-input
  []
  [iv/specd-input-view
   :ns "email"
   :type "subject"
   :label "Subject"
   :spec ::e/subject
   :error-msg "Please fill the subject line."])

(defn recipient-name
  "Returns first name of recipient if 1, {js-first-name} if more than 1"
  [recipients]
  (if (< 1 (count recipients))
    "{js-first-name}"
    (-> recipients first :jobSeekerFirstName)))

(defn greeting-dropdown []
  (let [recipients (rf/subscribe [:email/recipients])]
    (fn []
      (let [greeting-model (rf/subscribe [:email/greetings])
            choices (-> @recipients
                        (recipient-name)
                        (dd/greetings))]
        [rc/v-box
         :class "email-input"
         :children [[rc/label :label "Greeting"]
                    [rc/single-dropdown
                     :class "greeting-dropdown"
                     :model greeting-model
                     :on-change #(rf/dispatch [:email/greetings-change %])
                     :choices choices]]]))))

(defn msg-body-input []
  (let [msg-body-model (rf/subscribe [:email/msg-body])]
    (fn []
      [rc/v-box
       :class "email-input"
       :children [[iv/specd-input-view
                   :ns "email"
                   :type "msg-body"
                   :label "Message Body"
                   :input-type rc/input-textarea
                   :input-options [:rows "10"
                                   :width "100%"]
                   :max-length 5000
                   :spec ::e/msg-body
                   :error-msg "Please fill the message body."]
                  [iv/character-limit (count @msg-body-model)]]])))

(defn email-fields [name]
  (fn [name]
    [rc/v-box
     :class "fields"
     :children [[subject-input]
                [greeting-dropdown name]
                [msg-body-input]]]))

(defn send-email-click
  "Toggles errors if any, otherwise sends email"
  []
  (let [email (rf/subscribe [:email])]
    (if (s/valid? ::e/email @email)
      (rf/dispatch [:email/send])
      (rf/dispatch [:email/show-errors?-change true]))))

(defn email-btns []
  (let [name (rf/subscribe [:email/name])
        active-template (rf/subscribe [:email/active-template])
        can-add-template? (rf/subscribe [:email/can-add-template?])
        show-save? (reagent/atom nil)
        show-errors? (rf/subscribe [:email/show-errors?])
        is-sending? (rf/subscribe [:email/is-sending?])]
    (fn []
      [rc/v-box
       :class "btn-holder"
       :children [[rc/hyperlink :class "email-link" :label "Save as new template" :disabled? (not @can-add-template?) :on-click (handler-fn (reset! show-save? (not @show-save?)))]
                  (when @show-save?
                    [iv/action-text-input :model name :on-change #(rf/dispatch [:email/name-change %]) :action-label "Save" :on-action #(rf/dispatch [:email/save-template])])
                  [rc/hyperlink :class "email-link" :label "Update the existing template" :disabled? (nil? @active-template) :on-click #(rf/dispatch [:email/update-template])]
                  [rc/button :class "send-btn" :label "Send Message" :on-click send-email-click :disabled? @is-sending?]]])))

(defn email-form
  []
  (let [rec-count (rf/subscribe [:email/recipient-count])
        recipient (rf/subscribe [:email/first-recipient])]
    (fn []
      [rc/v-box
       :class "email-form"
       :children [[rc/label
                   :class "title"
                   :label (email-title @rec-count @recipient)]
                  [email-fields]
                  [email-btns]]])))

(defn delete-template
  [id]
  (fn [id]
    [:img.delete-template {:src      (img/url :x-icon-url)
                           :on-click #(rf/dispatch [:email/delete-template id])}]))

(defn template-item
  [[_ {:keys [id name] :as template}]]
  (let [active-template (rf/subscribe [:email/active-template])
        is-hovered? (reagent/atom nil)]
    (fn [[_ {:keys [id name] :as template}]]
      [rc/h-box
       :class "template-list-item-holder"
       :attr {:on-mouse-enter (handler-fn (reset! is-hovered? (not @is-hovered?)))
              :on-mouse-leave (handler-fn (reset! is-hovered? (not @is-hovered?)))}
       :children [[rc/hyperlink
                   :class (str "template" (when (= @active-template id) " active"))
                   :label name
                   :on-click #(rf/dispatch [:email/template-click template])]
                  (when @is-hovered?
                    [delete-template id])]])))

(defn template-list
  []
  (let [templates (rf/subscribe [:email/templates])]
    (fn []
      [rc/v-box
       :class "template-list"
       :children (if (< 0 (count @templates))
                   (map (partial vector template-item) @templates)
                   [[:div.no-templates "No Saved Templates to Display"]])])))

(defn email-templates []
  (fn []
    [rc/v-box
     :class "templates"
     :children [[rc/label :label "Choose a template"]
                [template-list]]]))

(defn email-holder []
  (fn []
    [rc/h-box
     :class "modal-box email-holder"
     :children [[email-form]
                [email-templates]
                [:span {:class "modal-close" :on-click #(rf/dispatch [:email/close-email-modal])}]]]))

(defn email-modal []
  (rf/dispatch [:email/get-templates])
  (let [show? (rf/subscribe [:email/show-email-modal?])
        rec-count (rf/subscribe [:email/recipient-count])
        recipient (rf/subscribe [:email/first-recipient])]
    (fn []
      (when @show? [rc/modal-panel
                    :class "email-modal"
                    :wrap-nicely? false
                    :backdrop-on-click #(rf/dispatch [:email/close-email-modal])
                    :child [email-holder]]))))

