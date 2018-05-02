(ns recruit-app.components.modal
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [recruit-app.components.header :as header]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.icon :as icon]
            [recruit-app.components.button :as btn]
            [cljs.spec.alpha :as s]))

(s/def ::show-modal? boolean?)
(s/def ::modal-config (s/keys :opt [::show-modal?]))

;; Event Spec
(s/def ::open-modal any?)
(s/def ::close-modal any?)

(s/def ::email ::modal-config)
(s/def ::ats-select-job ::modal-config)
(s/def ::candidate-notes ::modal-config)
(s/def ::confirm-email ::modal-config)
(s/def ::create-account ::modal-config)
(s/def ::forgot-password-success ::modal-config)
(s/def ::full-access-success ::modal-config)
(s/def ::full-access-confirmation ::modal-config)
(s/def ::profile-image ::modal-config)
(s/def ::delete-project ::modal-config)
(s/def ::rename-project ::modal-config)
(s/def ::delete-project-candidates ::modal-config)
(s/def ::referral-hiring-success ::modal-config)
(s/def ::resend-verification-success ::modal-config)
(s/def ::save-candidate ::modal-config)
(s/def ::edit-saved-search ::modal-config)
(s/def ::share-resume ::modal-config)
(s/def ::forgot-password-email ::modal-config)
(s/def ::verification-email ::modal-config)
(s/def ::create-team ::modal-config)
(s/def ::edit-team ::modal-config)
(s/def ::remove-team ::modal-config)
(s/def ::edit-team-member ::modal-config)
(s/def ::remove-team-member ::modal-config)
(s/def ::exporting-to-ats ::modal-config)
(s/def ::export-to-ats-success ::modal-config)
(s/def ::promote-job ::modal-config)
(s/def ::purchase-again ::modal-config)
(s/def ::purchase-and-promote ::modal-config)
(s/def ::remove-job ::modal-config)
(s/def ::modal (s/keys :opt [::email
                             ::ats-select-job
                             ::candidate-notes
                             ::confirm-email
                             ::create-account
                             ::forgot-password-success
                             ::full-access-success
                             ::full-access-confirmation
                             ::profile-image
                             ::delete-project
                             ::rename-project
                             ::delete-project-candidates
                             ::referral-hiring-success
                             ::resend-verification-success
                             ::save-candidate
                             ::edit-saved-search
                             ::share-resume
                             ::forgot-password-email
                             ::verification-email
                             ::create-team
                             ::edit-team
                             ::remove-team
                             ::edit-team-member
                             ::remove-team-member
                             ::exporting-to-ats
                             ::export-to-ats-success
                             ::promote-job
                             ::purchase-again
                             ::purchase-and-promote
                             ::remove-job]))

(defn- modal-body
  [body]
  [layout/column
   :padding 0
   :class "modal-body"
   :children body])

(defn- modal-title
  [title on-close]
  [layout/row
   :padding 0
   :justify :between
   :align :start
   :children [[header/header-3 title]
              [icon/modal-x
               :on-click on-close]]])

(defn- modal-action
  [params]
  [layout/row-top
   :padding-bottom 36
   :justify :center
   :children [(into [btn/primary-button] (mapcat identity params))]])

(defn- modal-box
  [& {:keys [title message body on-close action]}]
  [rc/v-box
   :class "modal-box"
   :children (cond-> []
                     title (conj [modal-title title on-close])
                     body (conj [layout/row
                                 :padding 30
                                 :children [[modal-body body]]])
                     action (conj [modal-action action]))])

(defn- close-modal-fn
  "Returns function to close given modal"
  [modal-key]
  #(rf/dispatch [::close-modal modal-key]))

(defn modal
  "The modal component will wrap functionality around opening and closing the modal (whether or not to show).
  When creating a modal component, a fully qualified `modal-key` (defined in the modal namespace) must be passed
  in order to register the subs/events. Once created, there is no need to wrap it in a mechanism that will show or hide it.
  It will be hidden by default and can be shown via an event.

  - `modal-key`: Fully qualified key defined in modal namespace
  - `title`: *Optional* Title of modal
  - `body`: *Optional* Collection of components to be displayed vertically
  - `action`: *Optional* Map of button attributes to create button to be displayed at bottom of modal
    - `label`: Label of button
      - `on-click`: Event to be fired on button click
      - `on-close`: *Optional* Additional callback that will be fired when modal is closed (No need to fire `:close-modal` event in this function)"
  [& {:keys [modal-key class on-close title body action]
      :or   {on-close #()}}]
  (let [show-modal? (rf/subscribe [::show-modal? modal-key])]
    (fn [& {:keys [modal-key class on-close title body action]
            :or   {on-close #()}}]
      (let [on-close-fn (comp on-close (close-modal-fn modal-key))]
        (when @show-modal?
          [rc/modal-panel
           :class (str "modal " class)
           :wrap-nicely? false
           :backdrop-on-click on-close-fn
           :style {:width "101%"}
           :child [modal-box
                   :title title
                   :body body
                   :on-close on-close-fn
                   :action action]])))))
