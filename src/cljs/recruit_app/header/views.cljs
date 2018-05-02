(ns recruit-app.header.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.util.uri :as u]
            [recruit-app.util.img :as img]))


;;header

(defn menu-divider
  []
  (fn []
    [:div {:class "menu-divider"} [:div {:class "menu-stripe"}]]))

(defn arrow
  [mouse-over]
  (fn [mouse-over]
    [:div {:class (if mouse-over "arrow-up-ra" "arrow-down-ra")}]))

(defn menu-holder
  "Creates mouseover atom to handle optional dropdown menu and hover css."
  [& {:keys [label on-click dropdown-menu class]}]
  (let [mouse-over (reagent/atom nil)]
    (fn [& {:keys [children]}]
      [rc/v-box
       :class (str "menu-holder" (when @mouse-over " active") (when class (str " " class)))
       :attr {:on-mouse-over (handler-fn (reset! mouse-over true))
              :on-mouse-out  (handler-fn (reset! mouse-over nil))}
       :children [[rc/button
                   :label label
                   :class "header-menu"
                   :on-click (or on-click #())]
                  (when dropdown-menu [(partial arrow @mouse-over)])
                  (when (and dropdown-menu @mouse-over) dropdown-menu)]])))

(defn super-user-menu
  []
  [menu-holder
   :label "Superuser"
   :class "superuser"
   :dropdown-menu [rc/v-box
                   :class "dropdown super-user-menu"
                   :children [[rc/hyperlink-href
                               :label "Actions"
                               :href "/#/superuser"]
                              [menu-divider]
                              [rc/hyperlink-href
                               :label "Recruiter Admin"
                               :href "/#/recruiter-admin"]]]])

(defn jobs-menu
  []
  [menu-holder
   :label "Jobs"
   :class "jobs-menu"
   :dropdown-menu [rc/v-box
                   :class "dropdown"
                   :children [[rc/hyperlink-href
                               :label "Manage Jobs"
                               :href "#/jobs"]
                              [menu-divider]
                              [rc/hyperlink-href
                               :label "Post a job"
                               :href "#/post-job"]]]])


(defn search-menu
  []
  [menu-holder
   :label "Search"
   :class "search-menu"
   :dropdown-menu [rc/v-box
                   :class "dropdown"
                   :children [[rc/hyperlink
                               :label "New Search"
                               :on-click #(rf/dispatch [:search/new-search])]
                              [menu-divider]
                              [rc/hyperlink-href :label "Saved Searches" :href "#/saved-searches"]]]])


(defn candidates-btn
  []
  [menu-holder
   :label "Projects"
   :on-click #(rf/dispatch [:go-to-route "/projects"])])

(defn products-btn
  []
  [menu-holder
   :label "Products"
   :on-click #(rf/dispatch [:go-to-shopify])])

(defn header-account
  []
  (let [image (rf/subscribe [:recruiter/profile-img])]
    (fn []
      [:img {:src      @image :class "profile-picture" :style {:align-self "center"}
             :on-error #(rf/dispatch [:recruiter/has-photo?-change false])}])))

(defn upload-a-photo
  []
  (fn []
    [rc/hyperlink-href
     :class "upload-a-photo"
     :label "Upload a Photo"
     :href "/#/account"]))

(defn account-management
  []
  (let [account-exec (rf/subscribe [:recruiter/account-executive])]
    (fn []
      [rc/h-box
       :class "account-management"
       :children [[rc/v-box
                   :class "contact-info"
                   :children [[:p "Ask " (:firstname @account-exec) ", your Account Specialist:"]
                              [rc/hyperlink-href
                               :class "email-rep contact"
                               :label [:span "Email" [:br]
                                       (:email @account-exec)]
                               :href (str "mailto:" (:email @account-exec))]
                              [rc/hyperlink-href
                               :class "call-rep contact"
                               :label [:span "Call " (:firstname @account-exec) [:br]
                                       (:telephone @account-exec)]
                               :href "tel:18008712574"]]]]])))

(defn account-menu
  []
  (let [mouse-over (reagent/atom nil)
        first-name (rf/subscribe [:recruiter/firstname])
        has-photo? (rf/subscribe [:recruiter/has-photo?])
        account-exec (rf/subscribe [:recruiter/account-executive])]
    (fn []
      [rc/v-box
       :align :end
       :class (str "menu-holder account-menu " (when @mouse-over "active"))
       :attr {:on-mouse-over (handler-fn (reset! mouse-over true))
              :on-mouse-out  (handler-fn (reset! mouse-over nil))}
       :children [[rc/h-box
                   :justify :end
                   :align :start
                   :class "profile-name-holder"
                   :children [[:button {:class (str "header-menu " (when @mouse-over "active"))} @first-name]
                              (when @has-photo? [header-account])
                              [arrow @mouse-over]
                              (when (not @has-photo?) [upload-a-photo])]]
                  (when @mouse-over
                    [rc/v-box
                     :class "dropdown"
                     :children [[rc/hyperlink-href
                                 :label "My Account"
                                 :href "/#/account"]
                                [menu-divider]
                                (when (seq @account-exec) [account-management])
                                [rc/button
                                 :label "Sign Out"
                                 :on-click #(rf/dispatch [:login/logout])]]])]])))

(defn account-menu-mobile
  []
  (let [mouse-over (reagent/atom nil)
        first-name (rf/subscribe [:recruiter/firstname])
        has-photo? (rf/subscribe [:recruiter/has-photo?])
        account-exec (rf/subscribe [:recruiter/account-executive])]
    (fn []
      [rc/v-box
       :align :start
       :class (str "menu-holder account-menu " (when @mouse-over "active"))
       :attr {:on-mouse-over (handler-fn (reset! mouse-over true))
              :on-mouse-out  (handler-fn (reset! mouse-over nil))}
       :children [[rc/h-box
                   :justify :end
                   :align :start
                   :class "profile-name-holder"
                   :children [[:button {:class (str "header-menu " (when @mouse-over "active"))} @first-name]
                              (when @has-photo? [header-account])
                              [arrow @mouse-over]
                              (when (not @has-photo?) [upload-a-photo])]]
                  (when @mouse-over
                    [rc/v-box
                     :class "dropdown"
                     :children [[rc/hyperlink-href
                                 :label "My Account"
                                 :href "/#/account"]
                                [menu-divider]
                                (when (seq @account-exec) [account-management])
                                [rc/button
                                 :label "Sign Out"
                                 :on-click #(rf/dispatch [:login/logout])]]])]])))

(defn logo []
  [:a {:href "/" :class "home-icon"}
   [:img {:class "logo"
          :src   (img/url :ladders-recruiter-logo)}]])

(defn left-menu
  []
  (let [superuser? (rf/subscribe [:recruiter/superuser?])]
    (fn []
      [rc/h-box
       :class "left-menu"
       :children [[logo]
                  (when @superuser? [super-user-menu])
                  [jobs-menu]
                  [search-menu]
                  [candidates-btn]
                  [products-btn]]])))

(defn left-menu-dropdown
  []
  (let [superuser? (rf/subscribe [:recruiter/superuser?])]
    (fn []
      [rc/v-box
       :class "left-menu-dropdown"
       :children [(when @superuser? [super-user-menu])
                  [jobs-menu]
                  [search-menu]
                  [candidates-btn]
                  [products-btn]
                  [account-menu-mobile]]])))

(defn left-menu-mobile
  []
  (let [mobile-header (rf/subscribe [:header/show-header-dropdown?])]
    (fn []
      [rc/v-box
       :class "left-menu"
       :children [[rc/h-box
                   :style {:align-items "center"}
                   :gap "20px"
                   :children [[rc/md-icon-button
                               :md-icon-name "zmdi-menu"
                               :on-click #(rf/dispatch [:header/toggle-show-header-dropdown?])]
                              [logo]]]
                  (when @mobile-header
                    [left-menu-dropdown])]])))

(defn right-menu
  []
  (fn []
    [rc/h-box
     :justify :end
     :class "right-menu"
     :children [[account-menu]]]))

(defn recruit-header
  []
  (fn []
    [rc/h-box
     :class "header"
     :height "50px"
     :children [[rc/h-box
                 :class "header-content"
                 :children [[left-menu] [right-menu]]]]]))

(defn recruit-header-mobile
  []
  (fn []
    [rc/h-box
     :class "header mobile"
     :height "50px"
     :children [[rc/h-box
                 :class "header-content"
                 :children [[left-menu-mobile]]]]]))

(defn recruit-header-container
  []
  (fn []
    [rc/v-box
     :class "header-container"
     :children [[recruit-header] [recruit-header-mobile]]]))

(defn lo-header
  []
  (let [show-sign-in? (rf/subscribe [:header/show-sign-in?])]
    (fn []
      [rc/box
       :class "lo-header-holder"
       :child [rc/h-box
               :justify :between
               :class "lo-header"
               :children [[rc/box
                           :class "logo-holder"
                           :child [:div {:class "logo"}
                                   [rc/hyperlink-href
                                    :label [:img {:src (img/url :ladders-recruiter-logo)}]
                                    :href "/"]]]
                          (when @show-sign-in?
                            [rc/box
                             :class "login-button"
                             :child [rc/hyperlink
                                     :label "Sign In"
                                     :on-click #(rf/dispatch [:go-to-login])]])]]])))


