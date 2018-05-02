(ns recruit-app.dashboard.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate item-for-id]]
            [cljs.reader :refer [read-string]]
            [goog.string :as gs]
            [recruit-app.util.img :as img]
            [recruit-app.util.chart :as chart]
            [recruit-app.util.date :as date]
            [recruit-app.util.date :as date]
            [recruit-app.util.dashboard :as da]
            [recruit-app.util.education :as ed]
            [recruit-app.util.job :as jb]
            [recruit-app.saved-searches.views :as ss]
            [recruit-app.components.box :as b]
            [recruit-app.components.header :as h]
            [recruit-app.components.hyperlink :as hl]
            [recruit-app.components.button :as btn]
            [recruit-app.jobs.views :as jobs]
            [recruit-app.components.loading :as lo]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.table :as table]
            [recruit-app.util.date :as d]))

(defn most-education
  []
  (let [most-applicants-label (rf/subscribe [:dashboard/most-applicants-education-label])
        most-applicants-percentage (rf/subscribe [:dashboard/most-applicants-education-percentage])
        site-average (rf/subscribe [:dashboard/education-site-average])
        is-highest-degree? (rf/subscribe [:dashboard/is-highest-degree?])
        display-data-not-available? (rf/subscribe [:dashboard/display-data-not-available?])]
    (fn []
      (let [degree-text (if @is-highest-degree? "Degree" "or Higher Degree")]
        (when (not @display-data-not-available?)
          [rc/v-box
           :class "top-stat most-education"
           :children [[:div.row1 (str @most-applicants-label " " degree-text)]
                      [:div.row2 (str @most-applicants-percentage " %")]
                      [:div.row3 (str "Site Average: " @site-average "% " @most-applicants-label " " degree-text)]]])))))

(defn most-experience
  []
  (let [most-applicants-label (rf/subscribe [:dashboard/most-applicants-experience-label])
        most-applicants-percentage (rf/subscribe [:dashboard/most-applicants-experience-percentage])
        site-average (rf/subscribe [:dashboard/experience-site-average])]
    (fn []
      [rc/v-box
       :class "top-stat"
       :children [[:div.row1 (str @most-applicants-label " Years")]
                  [:div.row2 (str @most-applicants-percentage " %")]
                  [:div.row3 (str "Site Average: " @site-average "%")]]])))

(defn most-salary
  []
  (let [average-salary (rf/subscribe [:dashboard/average-salary])]
    (fn []
      [rc/v-box
       :class "top-stat"
       :children [[:div.row1 "Average Salary"]
                  [:div.row2 (str "$" @average-salary "k")]
                  [:div.row3 "Site Average: $154k"]]])))

(defn education-chart
  []
  (let [dataset (rf/subscribe [:dashboard/education-dataset])
        display-data-not-available? (rf/subscribe [:dashboard/display-data-not-available?])]
    (fn []
      [rc/v-box
       :gap "4px"
       :children [[h/header-5 "Education"]
                  (when @dataset
                    (if @display-data-not-available?
                      [:div.no-data-message "Data not available"]
                      [chart/education-chart dataset]))]])))

(defn experience-chart-by-range
  []
  (let [experience-range-1 (rf/subscribe [:dashboard/experience-range 0])
        experience-range-2 (rf/subscribe [:dashboard/experience-range 1])
        experience-range-3 (rf/subscribe [:dashboard/experience-range 2])
        experience-range-4 (rf/subscribe [:dashboard/experience-range 3])
        experience-range-5 (rf/subscribe [:dashboard/experience-range 4])]
    (fn []
      [rc/h-box
       :class "experience-chart-container"
       :gap "30px"
       :children [[chart/experience-chart experience-range-1 1]
                  [chart/experience-chart experience-range-2 2]
                  [chart/experience-chart experience-range-3 3]
                  [chart/experience-chart experience-range-4 4]
                  [chart/experience-chart experience-range-5 5]]])))

(defn experience-chart-legend
  []
  [rc/h-box
   :children [[:div.rec.applicant-rec "Your Applicants"]
              [:div.rec.average-rec "Site Average"]]])

(defn salary-chart-legend
  []
  [rc/h-box
   :style {:flex-flow "row wrap"}
   :class "salary-chart-legend"
   :justify :start
   :children [[:div.rec.salary-range-rec-1 "$80K–$100K"]
              [:div.rec.salary-range-rec-2 "$100K–$150K"]
              [:div.rec.salary-range-rec-3 "$150K–$200K"]
              [:div.rec.salary-range-rec-4 "$200K–$250K"]
              [:div.rec.salary-range-rec-5 "$250K+"]]])

(defn experience-chart
  []
  (let [dataset (rf/subscribe [:dashboard/experience-dataset])]
    (fn []
      [rc/v-box
       :children [[h/header-5 "Experience (Year)"]
                  (when @dataset
                    [experience-chart-by-range])
                  [experience-chart-legend]]])))

(defn salary-chart-by-range
  []
  (let [dataset (rf/subscribe [:dashboard/salary-dataset])]
    (fn []
      [rc/h-box
       :class "salary-chart-container"
       :gap "20px"
       :children [(when @dataset
                    [chart/salary-chart dataset])]])))

(defn salary-chart
  []
  [rc/v-box
   :gap "4px"
   :children [[h/header-5 "Salary"]
              [salary-chart-by-range]
              [salary-chart-legend]]])

(defn education
  []
  [rc/h-box
   :children [[education-chart]
              [most-education]]])

(defn experience
  []
  [rc/h-box
   :children [[experience-chart]
              [most-experience]]])

(defn salary
  []
  [rc/h-box
   :children [[salary-chart]
              [most-salary]]])

(defn no-applicants-view
  []
  (let [has-job-post-performance? (rf/subscribe [:dashboard/has-job-post-performance?])]
    (fn []
      [:div.no-applicants
       [:div.no-applicants-cover]
       (if @has-job-post-performance?
         [rc/v-box
          :class "no-applicants-message no-applicants-promote"
          :gap "10px"
          :children [[:div "You currently have no applicants to your job posts. Purchasing promoted jobs can return up to 8x more qualified candidates."]
                     [btn/primary-dashboard-button
                      :label "Buy Promoted Jobs"
                      :class "promote-btn"
                      :on-click #(rf/dispatch [:go-to-shopify])]]]
         [:span.no-applicants-message "Post jobs to see how your applicants stack up."])])))

(defn application-demographics
  []
  (let [has-applicants? (rf/subscribe [:dashboard/has-applicants?])]
    (fn []
      [b/box
       :label "Applicant Demographics"
       :class "dashboard-component applicant-demo"
       :body (if @has-applicants?
               [rc/v-box
                :class "charts"
                :gap "20px"
                :children [[education]
                           [experience]
                           [salary]]]
               [no-applicants-view])])))

(defn review-btn
  [job-id]
  [btn/secondary-dashboard-button
   :label "Review Applicants"
   :on-click #(rf/dispatch [:dashboard/go-to-applicants job-id])])

(defn promote-btn
  [job-id]
  (let [has-promotions? (rf/subscribe [:recruiter/has-promotions?])]
    (fn [job-id]
      [btn/primary-dashboard-button
       :label "Promote"
       :on-click (if @has-promotions?
                   #(rf/dispatch [:jobs/promote-job job-id])
                   #(rf/dispatch [:checkout/promote-checkout-modal job-id]))])))

(defn performing-list-item
  [{:keys [publication_date locations job_id company_name title featured]}]
  (let [days (date/days-passed-since (d/db-date-time publication_date))
        applicants (-> locations
                       (first)
                       (:applicants)
                       (count))
        views (-> locations
                  (first)
                  (:views)
                  (count))
        company_name (when (and company_name (not (= company_name "Confidential Company")))
                       company_name)
        location (-> locations
                     (first)
                     (:location))]
    [layout/row
     :padding 6
     :justify :between
     :align :start
     :children [[layout/column
                 :padding 0
                 :class "col-xs-7"
                 :children [[layout/row
                             :padding 0
                             :children [[hl/hyperlink-href-medium
                                         :label (gs/unescapeEntities (str title " " company_name " " location))
                                         :href (str "#/job/" job_id)]]]
                            [layout/row-bottom
                             :padding-top 6
                             :children [[:div (str days " Days • " applicants " Applications • " views " Views")]]]]]
                [layout/column
                 :padding 0
                 :class "col-xs-5"
                 :children [[layout/row
                             :padding 0
                             :justify :end
                             :children [(if (jb/current-promotion featured)
                                          [review-btn job_id]
                                          [promote-btn job_id])]]]]]]))

(defn saved-searches-list
  [search]
  (let [search-name (:search-name search)
        keyword (get-in search [:criteria :keyword])
        location (get-in search [:criteria :location-text])
        last-run (get-in search [:reporting :email :last-send-time])
        search-id (:search-id search)]
    [rc/h-box
     :justify :between
     :children [[rc/v-box
                 :class "dashboard-list"
                 :children [[rc/v-box
                             :children [[hl/hyperlink-href-medium
                                         :label (str (da/trim-long-saved-search-name (gs/unescapeEntities search-name))
                                                     (when (not-empty keyword) (str ", " keyword))
                                                     (when (not-empty location) (str ", " location)))
                                         :href (str "#/search-results/" search-id)]
                                        [:div (when (not-empty last-run)
                                                (str "Last run: " (date/formatted-date :month-and-date (date/subscribe-date-time-with-ms last-run))))]]]]]]]))

(defn suggested-candidate-list
  [{:keys [firstname lastname desired-location education target-salary latest-experience secure-id]}]
  (let [name (str firstname " " lastname)
        location (str (:city desired-location) ", " (:state desired-location))
        degree-name (ed/edu-name (-> education
                                     (get 0)
                                     (:degree-id)))
        salary (-> target-salary
                   (:salary)
                   (/ 1000))
        title (:title latest-experience)
        company (:company latest-experience)]
    [layout/column
     :padding 0
     :size "100%"
     :children [[layout/row
                 :padding 0
                 :children [[hl/hyperlink-href-medium
                             :label name
                             :href (str "/#/candidates?jobseekerId=" secure-id)]]]
                [layout/row-bottom
                 :padding-top 3
                 :children [[:div (str location
                                       (when (not-empty degree-name) (str " • " degree-name))
                                       (when (not= salary 0) (str " • $" salary "k"))
                                       (when (not-empty title) (str " • " title))
                                       (when (not-empty company) (str ", " company)))]]]]]))

(defn performance
  [performing jobs]
  (when (not-empty jobs)
    [layout/column
     :padding 0
     :size "100%"
     :children [[layout/row
                 :padding 0
                 :children [[h/header-5 (str performing " Performing")]]]
                (map
                  (fn [job]
                    ^{:key (str "performance" (:job_id job))}
                    [layout/row
                     :padding 3
                     :children [[performing-list-item job]]])
                  jobs)]]))

(defn high-performance
  []
  (let [high-performing-jobs (rf/subscribe [:dashboard/high-performing-jobs])]
    (fn []
      [performance "high" @high-performing-jobs])))

(defn low-performance
  []
  (let [low-performing-jobs (rf/subscribe [:dashboard/low-performing-jobs])]
    (fn []
      [performance "low" @low-performing-jobs])))

(defn view-all
  [href-link-url]
  [rc/h-box
   :justify :end
   :children [[hl/hyperlink-href-small
               :label "View All"
               :class "view-all"
               :href href-link-url]]])

(defn most-recent-job-post
  []
  (let [suggested-candidates (rf/subscribe [:dashboard/suggested-candidates])]
    (fn []
      (let [most-recent-job-post (:most-recent-job @suggested-candidates)
            top-two-candidates (:top-two-candidates @suggested-candidates)]
        [layout/column
         :padding 0
         :children [[layout/row
                     :padding 0
                     :children [[h/header-5 "Most Recent Job Post"]]]
                    [layout/row-bottom
                     :padding-top 6
                     :children [[hl/hyperlink-href-medium
                                 :label (gs/unescapeEntities (str (:title most-recent-job-post)))
                                 :href (str "#/job/" (:job_id most-recent-job-post))]]]
                    (map
                      (fn [candidate]
                        ^{:key (str "saved-searches" (:jobseeker-id candidate))}
                        [layout/row
                         :padding 6
                         :children [[suggested-candidate-list candidate]]])
                      top-two-candidates)]]))))

(defn no-jobs-view
  []
  [rc/v-box
   :gap "15px"
   :class "no-jobs-view"
   :children [[:div.zero-state "You haven’t posted any jobs recently. Get your opportunities in front of thousands of qualified leads on Ladders."]
              [btn/primary-transaction-button
               :class "dashboard-action-btn"
               :on-click #(rf/dispatch [:dashboard/post-job])
               :label "Post a Job"]]])

(defn job-post-performance
  []
  (let [pjl-count (rf/subscribe [:recruiter/pjl-count])
        has-job-post-performance? (rf/subscribe [:dashboard/has-job-post-performance?])
        has-promotions? (rf/subscribe [:recruiter/has-promotions?])]
    (fn []
      [b/box
       :label "Job Post Performance"
       :top-right (if @has-promotions?
                    [:div (str @pjl-count " Promoted Jobs remaining  ")
                     [hl/hyperlink-small
                      :label "Buy More"
                      :on-click #(rf/dispatch [:go-to-shopify])]]
                    [hl/hyperlink-small
                     :class "top-right"
                     :label "Buy Promoted Jobs to boost your results"
                     :on-click #(rf/dispatch [:go-to-shopify])])
       :body (if @has-job-post-performance?
               [layout/column
                :padding 0
                :children [[layout/row-top
                            :children [[high-performance]]]
                           [layout/row
                            :children [[low-performance]]]
                           [layout/row
                            :justify :end
                            :children [[view-all "#/jobs"]]]]]
               [no-jobs-view])])))

(defn job-posts-list
  [label count]
  (let [tab (case label
              "Active" :tab0
              "Ending Soon" :tab0
              :tab4)]
    [layout/column
     :padding 0
     :children [[layout/row
                 :padding 6
                 :children [[h/header-5 label]]]
                [layout/row-top
                 :padding-bottom 6
                 :children [[hl/hyperlink-small
                             :label (str count)
                             :on-click #(rf/dispatch [:dashboard/go-to-manage-jobs tab])]]]]]))

(defn job-posts
  []
  (let [job-stats (rf/subscribe [:dashboard/job-stats])]
    (fn []
      (let [active (:active @job-stats)
            ending-soon (:expiring-soon @job-stats)
            all (:all @job-stats)]
        [b/box
         :label "Job Posts"
         :body [layout/column
                :padding 0
                :children [[job-posts-list "Active" active]
                           [job-posts-list "Ending Soon" ending-soon]
                           [job-posts-list "All Time" all]]]]))))

(defn engagement-usage-list
  [label this-week this-month]
  [rc/v-box
   :children [[h/header-5 label]
              [:div (str this-week " this week")]
              [:div (str this-month " this month")]]])

(defn engagement
  []
  (let [engagement (rf/subscribe [:dashboard/engagement])]
    (fn []
      (let [applicants-month (get-in @engagement [:applicants :applicants-this-month])
            applicants-week (get-in @engagement [:applicants :applicants-this-week])
            views-month (get-in @engagement [:views :views-this-month])
            views-week (get-in @engagement [:views :views-this-week])]
        [b/box
         :label "Engagement"
         :body [layout/column
                :padding 0
                :children [[layout/row-top
                            :padding-bottom 6
                            :children [[engagement-usage-list "Applicants" applicants-week applicants-month]]]
                           [layout/row-bottom
                            :padding-top 6
                            :children [[engagement-usage-list "Views" views-week views-month]]]]]]))))

(defn usage
  []
  (let [usage (rf/subscribe [:dashboard/usage])]
    (fn []
      (let [searches-month (:searches-this-month @usage)
            searches-week (:searches-this-week @usage)
            resume-month (:resume-views-this-month @usage)
            resume-week (:resume-views-this-week @usage)]
        [b/box
         :label "Usage"
         :body [rc/v-box
                :children [[layout/row-top
                            :padding-bottom 6
                            :children [[engagement-usage-list "Searches" searches-week searches-month]]]
                           [layout/row-bottom
                            :padding-top 6
                            :children [[engagement-usage-list "Resume Views" resume-week resume-month]]]]]]))))

(defn no-saved-searches-message
  []
  (fn []
    [layout/column
     :padding 0
     :gap "15px"
     :class "no-saved-searches-message"
     :children [[:div.zero-state "You currently have 0 saved searches."]
                [rc/button
                 :class "dashboard-action-btn"
                 :on-click #(rf/dispatch [:search/new-search])
                 :label "Start a New Search"]]]))

(defn saved-searches
  []
  (let [saved-searches (rf/subscribe [:dashboard/saved-searches])
        has-saved-searches? (rf/subscribe [:dashboard/has-saved-searches?])]
    (fn []
      [b/box
       :label "Saved Searches"
       :top-right [:div
                   (when @has-saved-searches?
                     [hl/hyperlink-href-small
                      :label "New Search"
                      :href "#/search"])]
       :body (if @has-saved-searches?
               [rc/v-box
                :children [(map
                             (fn [search] ^{:key (str "saved-searches" (:search-id search))} [saved-searches-list search])
                             @saved-searches)
                           [view-all "#/saved-searches"]]]
               [no-saved-searches-message])])))

(defn suggested-candidates
  []
  [b/box
   :label "Suggested Candidates"
   :class "saved-suggested-candidates"
   :body [most-recent-job-post]])

(defn ad
  []
  [b/box
   :label "CityHire"
   :class "ad-citihire"
   :body [rc/h-box
          :gap "8px"
          :children [[:div.ad-text-citihire "CityHire can help your company reach more of your target audience with direct, branded emails."]
                     [:img.ad-image-citihire {:src (img/url :cityhire)}]]]])

(defn team-members-summary
  []
  (let [team-members (rf/subscribe [:dashboard/team-members])]
    (fn []
      [b/box
       :class "team-members-summary"
       :label "Team Members Summary"
       :top-right [:div.sub-title "Last 30 Days"]
       :header-justify :start
       :body (if @team-members
               [table/table
                :headers [[table/header-cell
                           :label "Name"]
                          [table/header-cell
                           :label "Active Jobs"]
                          [table/header-cell
                           :label "Jobs Ending"]
                          [table/header-cell
                           :label "Applicants"]
                          [table/header-cell
                           :label "Views"]
                          [table/header-cell
                           :label "Searches"]
                          [table/header-cell
                           :label "Resume Views"]]
                :row-data @team-members])])))

(defn body
  []
  (let [team-summary (rf/subscribe [:dashboard/team-summary-data])]
    (fn []
      [rc/h-box
       :class "content"
       :children [[rc/h-box
                   :justify :between
                   :width "100%"
                   :children [[layout/col-left
                               :class "col-xs-2"
                               :children [[layout/row-top
                                           :children [[job-posts]]]
                                          [layout/row
                                           :children [[engagement]]]
                                          [layout/row-bottom
                                           :children [[usage]]]]]
                              [layout/col-right
                               :class "col-xs-10"
                               :children [(when @team-summary
                                            [layout/row-top
                                             :children [[team-members-summary]]
                                             :padding-bottom 18])
                                          [layout/row-top
                                           :children [[rc/h-box
                                                       :width "100%"
                                                       :children [[layout/col-left
                                                                   :class "col-xs-7"
                                                                   :children [[layout/row-top
                                                                               :children [[job-post-performance]]]
                                                                              [layout/row-bottom
                                                                               :children [[application-demographics]]]]]
                                                                  [layout/col-right
                                                                   :class "col-xs-5"
                                                                   :children [[layout/row-top
                                                                               :children [[saved-searches]]]
                                                                              [layout/row
                                                                               :children [[suggested-candidates]]]
                                                                              [layout/row-bottom
                                                                               :children [[ad]]]
                                                                              ]]]]]]]]]]]])))

(defn index
  []
  (let [is-fetching? (rf/subscribe [:dashboard/is-fetching?])]
    (rf/dispatch [:dashboard/get-initial-data])
    (fn []
      (if @is-fetching?
        [lo/loading-page]
        [rc/v-box
         :class "dashboard main"
         :children [[body] [jobs/checkout_modal]]]))))