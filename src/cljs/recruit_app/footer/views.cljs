(ns recruit-app.footer.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [goog.string :as gstring]
            [recruit-app.util.uri :as u]
            [recruit-app.util.img :as img]
            [cljs-time.format :as f]
            [cljs-time.core :as time]))

(def footer_links [{:title "About Us"
                    :link  "https://www.theladders.com/corporate/about-us"}
                   {:title "Work at Ladders"
                    :link  "http://careers.theladders.com/"}
                   {:title "FAQs"
                    :link  "#/faqs"}
                   {:title "News & Advice"
                    :link  "https://www.theladders.com/career-advice/"}
                   {:title "Privacy"
                    :link  "http://www.theladders.com/theladders-privacy"}
                   {:title "Terms of Use"
                    :link  "#/terms-of-use"}
                   {:title "Referral Program"
                    :link  "#/referral"}])

(defn social-links
  []
  "Social Links"
  [rc/v-box
   :style {:margin-left "auto"}
   :children [[rc/h-box
               :style {:margin-left "auto"}
               :class "sublinks-social"
               :children [[:a {:href "https://www.facebook.com/LaddersHQ"} [:span {:class "social facebook"}]]
                          [:a {:href "https://www.linkedin.com/company/ladders"} [:span {:class "social linkedin"}]]
                          [:a {:href "https://twitter.com/LaddersHQ"} [:span {:class "social twitter"}]]
                          [:a {:href "https://www.instagram.com/LaddersHQ"} [:span {:class "social instagram"}]]]]

              [:div {:class "copyright"}
               [:span {:class "currentYear"}
                (gstring/unescapeEntities "&copy;") (str (f/unparse (f/formatter "YYYY") (time/now)) " TheLadders.com")]]]])

(defn contact-slide-up
  []
  (let [show-contact? (rf/subscribe [:footer/show-contact?])
        account-exec (rf/subscribe [:recruiter/account-executive])]
    (fn []
      (when (not (empty? @account-exec))
        [rc/v-box
         :class (str "contact-slide-up" (when @show-contact? " up"))
         :attr {:on-click #(rf/dispatch [:footer/toggle-show-contact?])}
         :children [[:div.contact
                     [:div.label "CONTACT"]
                     [:div "Account Representative"]]
                    [:div.name (str (:firstname @account-exec) " " (:lastname @account-exec))]
                    [:a {:href (str "mailto:" (:email @account-exec)) :on-click #(.stopPropagation %)} (:email @account-exec)]
                    [:a {:href (str "tel:" (:telephone @account-exec)) :on-click #(.stopPropagation %)} (:telephone @account-exec)]]]))))

(defn recruit-footer
  []
  "Recruit Main Footer"
  (fn []
    [rc/v-box
     :class "footer-container"
     :children [[contact-slide-up]
                [rc/v-box
                 :class "footer"
                 :children [[rc/v-box
                             :class "content"
                             :children [[rc/v-box :class "logo"
                                         :children [[:a {:href (u/uri :login)} [:img {:src (img/url :ladders-recruiter-logo)}]]]]
                                        [rc/h-box
                                         :children [[rc/v-box
                                                     :class "links"
                                                     :children (for [x (range 0 3)]
                                                                 ^{:key x} [rc/box
                                                                            :class "sublinks"
                                                                            :child [:a {:href (get-in footer_links [x :link])} (get-in footer_links [x :title])]])]
                                                    [rc/v-box
                                                     :class "links"
                                                     :children (for [x (range 3 6)]
                                                                 ^{:key x} [rc/box
                                                                            :class "sublinks"
                                                                            :child [:a {:href (get-in footer_links [x :link])} (get-in footer_links [x :title])]])]
                                                    [rc/v-box
                                                     :class "links"
                                                     :children (for [x (range 6 7)]
                                                                 ^{:key x} [rc/box
                                                                            :class "sublinks"
                                                                            :child [:a {:href (get-in footer_links [x :link])} (get-in footer_links [x :title])]])]
                                                    [social-links]]]]]]]]]))