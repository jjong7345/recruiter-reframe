(ns recruit-app.account.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate item-for-id]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [cljs.spec.alpha :as s]
            [recruit-app.util.account :as au]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.upload :as u]
            [recruit-app.modals.profile-image.views :as pi]
            [recruit-app.util.img :as img]
            [recruit-app.specs.account :as spec]
            [recruit-app.components.loading :as l]))

(defn divider []
  [:div {:class "divider"}])

(def input-view (partial iv/input-view "account"))

(defn save-preview-btns []
  (let [form-valid? (rf/subscribe [:account/form-valid?])]
    (fn []
      [rc/h-box
       :justify :end
       :class "button-group"
       :gap "5px"
       :children [[rc/button
                   :class "btn-border preview-btn"
                   :label "Preview"
                   :on-click #(rf/dispatch [:account/profile-preview])]
                  [rc/button
                   :class "save-btn btn"
                   :label "Save"
                   :on-click #(rf/dispatch
                                (if @form-valid?
                                  [:account/profile-update]
                                  [:account/profile-update-fail]))]]])))

(defn save-password-btns []
  (let [password-valid? (rf/subscribe [:account/password-valid?])]
    (fn []
      [rc/h-box
       :justify :start
       :class "button-group password-save-btn"
       :gap "5px"
       :children [[rc/button
                   :class "save-btn btn"
                   :label "Save"
                   :on-click #(rf/dispatch
                                (if @password-valid?
                                  [:account/password-update]
                                  [:account/on-password-change-fail]))]]])))

(defn save-ATS-btns [label]
  (fn [label]
    [rc/h-box
     :justify :start
     :class "button-group ats-save-btn"
     :gap "5px"
     :children [[rc/button
                 :class "save-btn btn"
                 :label label
                 :on-click #(rf/dispatch [:account/ats-update])]]]))

(def account-tab
  [{:id :tab0 :label "Profile" :text "Profile"}
   {:id :tab1 :label "Password" :text "Password"}
   {:id :tab2 :label "Subscriptions" :text "Subscription"}
   {:id :tab3 :label "ATS Settings" :text "ATS Settings"}])

(defn account-tabs
  []
  (let [selected-tab-id (rf/subscribe [:account/active-tab])]
    [rc/horizontal-tabs
     :model @selected-tab-id
     :tabs account-tab
     :on-change #(rf/dispatch [:account/set-active-tab %])]))

(defn fname-input
  []
  [iv/input
   :label "First Name"
   :ns "account"
   :type "fname"
   :placeholder "First Name"
   :attr {:disabled "disabled"}])

(defn lname-input
  []
  [iv/input
   :label "Last Name"
   :ns "account"
   :type "lname"
   :placeholder "Last Name"
   :attr {:disabled "disabled"}])

(defn title-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "job-title"
   :label "Title *"
   :spec ::spec/job-title
   :placeholder "Title"
   :error-msg "Please enter a valid title"])

(defn function-input
  []
  (let [function (rf/subscribe [:account/function])]
    (fn []
      [rc/v-box
       :class "holder"
       :children [[rc/h-box
                   :class "labels"
                   :children [[rc/label :label "Function"]]]
                  [rc/single-dropdown
                   :class "function dropdown"
                   :placeholder "Select"
                   :model @function
                   :max-height "270px"
                   :on-change #(do (rf/dispatch [:account/function-change %]) (rf/dispatch [:account/role-change 0]))
                   :choices (dd/function)]]])))

(defn company-input
  []
  [iv/input
   :label "Company"
   :ns "account"
   :type "company"
   :placeholder "Company"
   :attr {:disabled "disabled"}])

(defn role-input
  []
  (let [role (rf/subscribe [:account/role])
        function (rf/subscribe [:account/function])]
    (fn []
      [rc/v-box
       :class "holder"
       :children [[rc/h-box
                   :class "labels"
                   :children [[rc/label :label "Role"]]]
                  [rc/single-dropdown
                   :class "role dropdown"
                   :placeholder "Select"
                   :model @role
                   :max-height "270px"
                   :on-change #(rf/dispatch [:account/role-change %])
                   :choices (dd/role @function)]]])))

(defn site-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "company-site"
   :label "Company Website"
   :spec ::spec/company-site
   :placeholder "http://"
   :error-msg "The website URL you entered seems invalid. 'http:// or https:// is also required'."])

(defn personal-info
  []
  [rc/v-box
   :class "personal info-box"
   :gap "10px"
   :children [[rc/label :class "title" :label "Personal Information"]
              [divider]
              [rc/h-box :class "row" :gap "20px" :children [[fname-input] [lname-input]]]
              [rc/h-box :class "row" :gap "20px" :children [[title-input] [function-input]]]
              [rc/h-box :class "row" :gap "20px" :children [[company-input] [role-input]]]
              [rc/h-box :class "row" :gap "20px" :children [[site-input]]]]])

(defn linkedin-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "linkedin"
   :label "LinkedIn"
   :spec ::spec/linkedin
   :placeholder "https://www.linkedin.com/in/"
   :error-msg "The website URL you entered seems invalid. 'http:// or https:// is also required'."])

(defn facebook-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "facebook"
   :label "Facebook"
   :spec ::spec/linkedin
   :placeholder "https://www.facebook.com/"
   :error-msg "The website URL you entered seems invalid. 'http:// or https:// is also required'."])

(defn twitter-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "twitter"
   :label "Twitter"
   :spec ::spec/twitter
   :placeholder "https://www.twitter.com/"
   :error-msg "The website URL you entered seems invalid. 'http:// or https:// is also required'."])

(defn blog-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "blog"
   :label "Blog"
   :spec ::spec/blog
   :placeholder "http://"
   :error-msg "The website URL you entered seems invalid. 'http:// or https:// is also required'."])

(defn profile-image
  []
  (let [image (rf/subscribe [:account/profile-img])
        is-uploading (rf/subscribe [:account/is-uploading?])]
    (fn []
      [l/loading-overlay-wrapper
       :class "profile-image"
       :on-click u/on-click
       :size :small
       :show-loading? @is-uploading
       :child [rc/v-box
               :children [[:img {:src      @image
                                 :on-error #(rf/dispatch [:account/set-default-image])}]
                          [rc/button
                           :label "Upload an image"]]]])))

(defn photo-upload
  []
  [rc/v-box
   :class "photo-upload"
   :children [[u/element
               :el profile-image
               :on-upload #(rf/dispatch [:profile-image/open-modal %1 %2 %3 %4])]
              [:div.photo-disclaimer "Max 15MB. No Logos. By uploading a file, you agree that it does not violate our Terms of Service."]
              [pi/modal]]])

(defn photo
  []
  [rc/v-box
   :class "photo info-box"
   :children [[rc/label :class "title" :label "Photo"]
              [divider]
              [:div.photo-header "Your picture is worth 1,000 words."]
              [photo-upload]]])

(defn social-info
  []
  [rc/v-box
   :class "social info-box"
   :gap "10px"
   :children [[rc/label :class "title" :label "Social Information"]
              [divider]
              [rc/h-box :class "row" :gap "20px" :children [[linkedin-input] [facebook-input]]]
              [rc/h-box :class "row" :gap "20px" :children [[twitter-input] [blog-input]]]]])

(defn bio-input
  []
  (let [bio-model (rf/subscribe [:account/bio])]
    (fn []
      [rc/input-textarea
       :class "bio"
       :width "auto"
       :model @bio-model
       :on-change #(rf/dispatch [:account/bio-change %])
       :change-on-blur? false
       :attr {:max-length 2000 :min-length 100}])))

(defn sample-bio
  []
  (fn []
    [:div.sample-bio
     [:h1 "Sample About Me"]
     [:p.em "This sample About Me contains detailed information about the recruiter and the types of jobs they typically recruit for."]
     [:p "I am a seasoned staffing and recruiting professional with more than 15 years of experience in sales and technology. I am based in New York, but also heavily cover Boston, Philadelphia, Chicago and Washington D.C."]
     [:p "I received my Bachelor's Degree in Communications from Ithaca College and an MBA from Columbia Business School. I regularly sit on panels at Human Resources seminars in New York and write a blog on technology trends in the workplace."]
     [:p "Highlights:"]
     [:ul
      [:li "Close relationships with top Fortune 500 hiring managers"]
      [:li "Track record of consistently placing clients in highly competitive opportunities"]
      [:li "Strong understanding of interview and offer negotiation process and strategy"]
      [:li "Intimate knowledge of personal brand building on the web"]
      [:li "President of New York City's largest technology networking group"]]
     [:p "I'm Always Looking For Strong:"]
     [:ul
      [:li "Product Managers at all levels"]
      [:li "Technical Project Managers"]
      [:li ".Net Developers"]
      [:li "C+ Developers"]
      [:li "Database Architects"]
      [:li "Account Executives"]
      [:li "Sales Directors and Managers"]
      [:li "Information Architects"]
      [:li "Graphic Designers"]
      [:li "Copywriters"]
      [:li "Business Development Managers"]
      [:li "Data Analytics Managers"]]
     [:p "I have personally placed talent at Facebook, AOL, IBM, Microsoft, 360i, AvenueA | Razorfish, Adaptive Path, Frog Design, Cisco, Oracle, Hewlett-packard, Sprint/Nextel, Ogilvy, and many other leading companies."]]))

(defn bio
  []
  (let [bio (rf/subscribe [:account/bio])
        show-sample-bio? (rf/subscribe [:account/show-sample-bio?])
        bio-contains-not-enough-characters? (rf/subscribe [:account/bio-contains-not-enough-characters?])
        bio-contains-contact? (rf/subscribe [:account/bio-contains-contact?])]
    (fn []
      [rc/v-box
       :class "biography info-box"
       :gap "10px"
       :children [[rc/label :class "title" :label "Biography"]
                  [divider]
                  [:div
                   {:class "subtitle"}
                   "Use this space to describe what areas you specialize in, the types of companies you work for, and desired applicant criteria."
                   [:a {:on-click #(rf/dispatch [:account/toggle-show-sample-bio?])} " View a sample."]]
                  (when @show-sample-bio?
                    [sample-bio])
                  [bio-input]
                  (when @bio-contains-not-enough-characters?
                    [:div.error-msg "Please enter a minimum of 100 characters."])
                  (when @bio-contains-contact?
                    [:div.error-msg "It looks you've entered your email address or contact information in the 'Biography' section. Please remove it - for safe, secure networking, all first contacts between candidates and recruiters are facilitated through our site, and all email addresses and phone numbers are kept hidden until you choose to share them."])
                  [rc/h-box
                   :class "sub-info gery-small"
                   :justify :between
                   :children [[:p "Please do not include your contact information or email address"]
                              [:p (str (- 2000 (count @bio)) " Characters Remaining")]]]]])))

(defn email-input
  []
  [iv/input
   :label "Work Email"
   :ns "account"
   :type "email"
   :attr {:disabled "disabled"}])

(defn email-info
  []
  (fn []
    [rc/v-box
     :gap "10px"
     :children [[rc/label :class "title" :label "Email"]
                [divider]
                [email-input]]]))

(defn phone-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "phone"
   :label "Phone *"
   :spec ::spec/phone
   :error-msg "Please enter a valid phone number"])

(defn ext-input
  []
  [iv/input
   :label "Extension"
   :ns "account"
   :type "ext"])

(defn phone-info
  []
  (fn []
    [rc/v-box
     :gap "10px"
     :children [[rc/label :class "title" :label "Phone"]
                [divider]
                [rc/h-box
                 :justify :start
                 :gap "20px"
                 :children [[phone-input] [ext-input]]]]]))

(defn street-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "street"
   :label "Street *"
   :spec ::spec/street
   :error-msg "Street is required"])

(defn city-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "city"
   :label "City *"
   :spec ::spec/city
   :error-msg "City is required"])

(defn state-input
  []
  (let [state (rf/subscribe [:account/state])]
    (fn []
      [rc/v-box
       :class "holder state-dd-holder"
       :children [[rc/h-box
                   :class "labels"
                   :children [[rc/label :label "State *"]]]
                  [iv/drpdn-view
                   :ns "account"
                   :type "state"
                   :choices (dd/states)
                   :spec ::spec/state
                   :error-msg [:div {:style {:width "138px"}} "Please select a state"]]]])))

(defn zip-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "zip"
   :label "Zip Code *"
   :spec ::spec/zip
   :error-msg "Please enter a valid zip code"])

(defn country-input
  []
  (fn []
    [rc/v-box
     :class "holder country-dd-holder"
     :children [[rc/h-box
                 :class "labels"
                 :children [[rc/label :label "Country *"]]]
                [iv/drpdn-view
                 :ns "account"
                 :type "country"
                 :choices (dd/countries)
                 :spec ::spec/country
                 :error-msg [:div {:style {:width "138px"}} "Please select a country"]]]]))

(defn address-info
  []
  (fn []
    [rc/v-box
     :gap "10px"
     :children [[rc/label :class "title" :label "Address"]
                [divider]
                [rc/h-box :gap "20px" :children [[street-input] [city-input] [state-input]]]
                [rc/h-box :gap "20px" :children [[zip-input] [country-input]]]]]))

(defn hidden-info
  []
  (fn []
    [rc/v-box
     :class "hidden-info info-box"
     :gap "30px"
     :children [[rc/h-box
                 :justify :between
                 :children [[:h2 {:class "head-text"} "Hidden Information"]
                            [:div.hidden-info-desc "This information is hidden from public view."]]]
                [email-info]
                [phone-info]
                [address-info]]]))

(defn current-password-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "current-password"
   :label "Enter your current password"
   :spec ::spec/current-password
   :error-msg "Please enter your current password"
   :input-options [:input-type :password]])

(defn new-password-input
  []
  [iv/specd-input-view
   :ns "account"
   :type "new-password"
   :label "Enter your new password"
   :spec ::spec/new-password
   :error-msg "Your password is not strong enough. Minimum 6 characters are required including at least 1 number, 1 special character and 1 letter."
   :input-options [:input-type :password]])

(defn confirm-password-input
  []
  (let [input-model (rf/subscribe [:account/confirm-password])
        show-errors? (rf/subscribe [:account/show-errors?])
        match? (rf/subscribe [:account/password-match?])]
    (fn []
      [rc/v-box
       :width "none"
       :class "confirm-password-holder holder"
       :children [[rc/h-box
                   :class "labels"
                   :justify :between
                   :children [[rc/label :class "input-label" :label "Confirm your new password"]]]
                  [rc/input-text
                   :class (str "confirm-password " (when (and @show-errors? (not @match?)) " error"))
                   :model @input-model
                   :width "none"
                   :on-change #(rf/dispatch [:account/confirm-password-change %])
                   :change-on-blur? false
                   :input-type :password]
                  (when (and @show-errors? (not @match?)) [:div.error-msg "'New Password' and 'Confirm Password' do not match."])]])))

(defn password-info
  []
  [rc/v-box
   :class "password-info info-box"
   :gap "10px"
   :children [[rc/label :class "title" :label "Change your password"]
              [divider]
              [rc/v-box :class "password-col" :gap "10px" :children [[current-password-input]
                                                                     [new-password-input]
                                                                     [confirm-password-input]
                                                                     [save-password-btns]]]]])

(defn choose-ats-input
  []
  (let [ats (rf/subscribe [:account/ats])]
    (fn []
      [rc/v-box
       :class "holder ats-dropdown-holder"
       :children [[rc/h-box
                   :class "labels"
                   :children [[rc/label :label "Choose your ATS:"]]]
                  [rc/single-dropdown
                   :class "ats-dropdown dropdown"
                   :placeholder "Select"
                   :model (:id @ats)
                   :max-height "270px"
                   :on-change #(rf/dispatch [:account/ats-dropdown-change (item-for-id % (dd/ats))])
                   :choices (dd/ats)]]])))

(defn api-key-input
  []
  [iv/input
   :label "Enter your API key:"
   :ns "account"
   :type "api-key"
   :placeholder "API Key"])

(defn secondary-api-key-input
  []
  [iv/input
   :label "Enter your Job Board API key:"
   :ns "account"
   :type "secondary-api-key"
   :placeholder "API Key"])


(defn enter-ats-input
  []
  [iv/input
   :label "Choose your ATS:"
   :ns "account"
   :type "ats-name"
   :placeholder "ATS Company"])

(defn job-board-token-input
  []
  (let [input-model (rf/subscribe [:account/job-board-token])]
    (fn []
      [rc/v-box
       :size "initial"
       :width "none"
       :class "job-board-token-holder holder"
       :children [[rc/v-box
                   :class "labels"
                   :children [[rc/label :class "input-label" :label "Enter your Job Board token:"]
                              [rc/label :class "info" :label "Your token can be found in any job posting URL:\nhttps://boards.greenhouse.io/{Job Board Token}/jobs/12345"]]]
                  [rc/input-text
                   :class "job-board-token"
                   :model input-model
                   :width "none"
                   :placeholder "Job Board Token"
                   :on-change #(rf/dispatch [:account/job-board-token-change %])
                   :change-on-blur? false]]])))

(defn lever-dropdown
  []
  (let [lever-user-selected (rf/subscribe [:account/lever-user-selected])
        lever-users (rf/subscribe [:account/lever-users])]
    (fn []
      [rc/v-box
       :class "holder ats-dropdown-holder"
       :children [[rc/label :label "Choose User:"]
                  [rc/single-dropdown
                   :class "dropdown"
                   :placeholder "Select"
                   :model (if @lever-user-selected @lever-user-selected nil)
                   :max-height "270px"
                   :on-change #(rf/dispatch [:account/lever-user-selected-change %])
                   :choices (mapv #(hash-map :id (:id %) :label (:name %)) (:data @lever-users))]]])))

(defn render-ats-input
  []
  (let [ats (rf/subscribe [:account/ats])]
    (fn []
      [rc/v-box
       :gap "10px"
       :children (condp = (:id @ats)
                   "lever" [[api-key-input]
                            [lever-dropdown]
                            [save-ATS-btns "Save"]
                            [divider]
                            [:div.lever-desc [:span "Lever: "] "Under Integrations, find \"Lever API credentials\" and click the Generate New Key button"]
                            [:img {:src (img/url :lever-image-url)}]]
                   "greenhouse" [[secondary-api-key-input]
                                 [job-board-token-input]
                                 [save-ATS-btns "Authorize"]]
                   "workable" [[api-key-input]
                               [save-ATS-btns "Save"]]
                   "other" [[enter-ats-input]
                            [api-key-input]
                            [save-ATS-btns "Save"]]
                   [[api-key-input]
                    [save-ATS-btns "Save"]])])))

(defn ats-info
  []
  [rc/v-box
   :class "ats-info info-box"
   :gap "10px"
   :children [[rc/label :class "title" :label "Integrate with your ATS"]
              [:p "Select your ATS from the dropdown menu, then enter your API key."]
              [divider]
              [rc/v-box :class "ats-col" :gap "10px" :children [[choose-ats-input] [render-ats-input]]]]])

(defn user-info
  []
  [rc/v-box
   :class "user"
   :children [[rc/h-box
               :justify :between
               :children [[personal-info] [photo]]]]])

(defn subscriptions-row
  [& {:keys [children title desc sub]}]
  (fn [& {:keys [children title desc sub]}]
    [rc/h-box
     :justify :start
     :class "subscriptions-row"
     :children [[rc/v-box
                 :class "cell1"
                 :children [[rc/label :class "title" :label title]
                            [:p desc]]]
                [:div {:class (str "sub-icon" (if sub " check" " nein"))}]
                children]]))

(defn subscriptions-table
  []
  (let [newsletter (rf/subscribe [:account/newsletter])
        special-offers (rf/subscribe [:account/special-offers])
        connection-req (rf/subscribe [:account/connection-req])
        feedback (rf/subscribe [:account/feedback])
        suggested-cand (rf/subscribe [:account/suggested-cand])
        search-based-cand (rf/subscribe [:account/search-based-cand])]
    (fn []
      [rc/v-box
       :class "subscriptions-info info-box"
       :gap "10px"
       :children [[rc/h-box
                   :justify :start
                   :children [[rc/label :class "title cell1" :label "Communication Preferences"] [:div {:class "status"} "Status"]]]
                  [divider]
                  [subscriptions-row
                   :title "TheLadders Newsletters"
                   :desc "E-newsletters with industry updates and expert perspectives."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "newsletter"])} (if @newsletter "Unsubscribe" "Subscribe")]
                   :sub @newsletter]
                  [subscriptions-row
                   :title "Special Offers"
                   :desc "Get the scoop on exciting new offers, promotions and services from TheLadders."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "special-offers"])} (if @special-offers "Unsubscribe" "Subscribe")]
                   :sub @special-offers]
                  [subscriptions-row
                   :title "Status Updates"
                   :desc "Stay up-to-date on the latest product changes specific to your account to help you use our site to your full advantage."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "special-offers"])} (if @special-offers "Unsubscribe" "Subscribe")]
                   :sub @special-offers]
                  [subscriptions-row
                   :title "Candidate Alerts"
                   :desc "When you save a search, we will automatically email you candidates that match that search."
                   :children [:a {:on-click #(rf/dispatch [:account/goto-manage-saved-searches])} "Manage Saved Searches"]
                   :sub @special-offers]
                  [subscriptions-row
                   :title "Connection Requests"
                   :desc "Prompt me to send a connection request each time I download a resume."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "connection-req"])} (if @connection-req "Turn off" "Turn on")]
                   :sub @connection-req]
                  [subscriptions-row
                   :title "Feedback Follow-up"
                   :desc "When job seekers view your positive feedback, they have the option to follow-up with a short message to you. We'll send you an email with the message."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "feedback"])} (if @feedback "Turn off" "Turn on")]
                   :sub @feedback]
                  [subscriptions-row
                   :title "Suggested Candidates"
                   :desc "When you have an active job post, we will automatically email you candidates that match the post criteria."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "suggested-cand"])} (if @suggested-cand "Turn off" "Turn on")]
                   :sub @suggested-cand]
                  [subscriptions-row
                   :title "Search-Based Candidates"
                   :desc "When you do some related searches, we will automatically email you candidates that match your search history."
                   :children [:a {:on-click #(rf/dispatch [:account/communication-preference-update "search-based-cand"])} (if @search-based-cand "Turn off" "Turn on")]
                   :sub @search-based-cand]]])))


(defn change-password
  []
  [rc/v-box
   :class "password"
   :children [[rc/h-box
               :justify :between
               :children [[password-info]]]]])

(defn ats
  []
  [rc/v-box
   :class "ats"
   :children [[rc/h-box
               :justify :between
               :children [[ats-info]]]]])

(defn subscriptions
  []
  [rc/v-box
   :class "subscriptions"
   :children [[rc/h-box
               :justify :between
               :children [[subscriptions-table]]]]])

(defn profile-tab
  []
  [rc/v-box
   :children [[user-info] [social-info] [bio] [hidden-info] [save-preview-btns]]])

(defn password-tab
  []
  [rc/v-box
   :children [[change-password]]])

(defn subscriptions-tab
  []
  [rc/v-box
   :children [[subscriptions]]])

(defn ats-tab
  []
  [rc/v-box
   :children [[ats]]])

(defn tabs-body
  []
  (let [active-tab (rf/subscribe [:account/active-tab])]
    (case @active-tab
      :tab0 [profile-tab]
      :tab1 [password-tab]
      :tab2 [subscriptions-tab]
      :tab3 [ats-tab]
      [:div ""])))

(defn header
  []
  (let [active-tab (rf/subscribe [:account/active-tab])
        function (rf/subscribe [:account/function])
        role (rf/subscribe [:account/role])
        company (rf/subscribe [:account/company])]
    (fn []
      (let [function-name (au/function-name @function)
            role-name (au/role-name @function @role)]
        [rc/h-box
         :class "title-container"
         :children [[rc/v-box
                     :class "title-box"
                     :children [[rc/title
                                 :label "My Account"
                                 :level :level1]
                                [:div.account-info (str function-name " - " role-name ", " @company)]
                                [account-tabs]
                                (when (= @active-tab :tab0)
                                  [save-preview-btns])]]]]))))

(defn body
  []
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [[rc/v-box
                 :class "content"
                 :children [[tabs-body]]]]]))

(defn index
  []
  (rf/dispatch [:account/load-view])
  (fn []
    [rc/v-box
     :class "account main"
     :children [[header] [body]]]))