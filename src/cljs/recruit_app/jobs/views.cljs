(ns recruit-app.jobs.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.string :as gs]
            [recruit-app.components.loading :as l]
            [recruit-app.components.table :as table]
            [recruit-app.components.form :as form]
            [recruit-app.modals.checkout.views :as checkout]
            [recruit-app.util.job :as ju]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.button :as btn]
            [recruit-app.components.header :as header]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.icon :as icon]
            [recruit-app.components.misc :as misc]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.util.date :as d]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.typography :as type]))

(defn loc-row
  [row]
  [layout/row
   :padding 3
   :children [row]])

(defn loc-cell-table [& {:keys [locations expanded job-id class-name first-row middle-row last-row]}]
  (fn [& {:keys [locations expanded job-id class-name first-row middle-row last-row]}]
    (let [[first-five rest-of-locations] (split-at 5 locations)]
      [layout/column
       :padding 0
       :children (cond-> [[layout/row-top
                           :padding-bottom 3
                           :children [first-row]]]

                         (and (> (count locations) 1) middle-row)
                         (conj [layout/row
                                :padding 0
                                :children [[layout/column
                                            :padding 0
                                            :children (vec (map (comp loc-row middle-row) first-five))]]])

                         (and (> (count locations) 1) expanded)
                         (conj [layout/row
                                :padding 0
                                :children [[layout/column
                                            :padding 0
                                            :children (vec (map (comp loc-row middle-row) rest-of-locations))]]])

                         (and (> (count locations) 1) last-row)
                         (conj [layout/row-bottom
                                :padding-top 3
                                :children [last-row]]))])))

(defn loc-data [locations expanded job-id]
  (fn [locations expanded job-id]
    [loc-cell-table
     :locations locations
     :expanded @expanded
     :job-id job-id
     :class-name "location"
     :first-row (if (< 1 (count locations)) (str (count locations) " Locations") (-> locations first :location))
     :middle-row (fn [loc] [:span (get-in loc [:location] "")])
     :last-row (when (< 5 (count locations))
                 [rc/hyperlink
                  :label [rc/h-box
                          :children [[:span (str "View " (if @expanded "Less" "All"))]
                                     [:span {:class (if @expanded "arrow-up-ra" "arrow-down-ra")}]]]
                  :on-click (handler-fn (reset! expanded (not @expanded)))])]))

(defn loc-app-link
  [num on-click]
  (fn [num on-click]
    (if (< 0 num)
      [rc/hyperlink
       :label (str num)
       :on-click on-click]
      [:div (str num)])))

(defn view-link
  [job-id loc]
  [loc-app-link
   (ju/jobseeker-count loc :views)
   #(rf/dispatch [:job/views-for-job job-id (:job_location_id loc)])])

(defn app-link
  [job-id loc]
  [loc-app-link
   (ju/jobseeker-count loc :applicants)
   #(rf/dispatch [:job/apps-for-job job-id (:job_location_id loc)])])

(defn view-data [locations expanded job-id]
  (fn [locations expanded job-id]
    (let [total (str (ju/total-jobseeker-count locations :views))]
      [loc-cell-table
       :locations locations
       :expanded @expanded
       :job-id job-id
       :class-name "view"
       :first-row (if (< 1 (count locations))
                    [:span total]
                    [loc-app-link total #(rf/dispatch [:job/views-for-job job-id (-> locations first :job_location_id)])])
       :middle-row (partial view-link job-id)])))

(defn app-data [locations expanded job-id]
  (fn [locations expanded job-id]
    (let [total (str (ju/total-jobseeker-count locations :applicants))]
      [loc-cell-table
       :locations locations
       :expanded @expanded
       :job-id job-id
       :class-name "apply"
       :first-row (if (< 1 (count locations))
                    [:span total]
                    [loc-app-link total #(rf/dispatch [:job/apps-for-job job-id (-> locations first :job_location_id)])])
       :middle-row (partial app-link job-id)])))

(defn title-link
  [job-id title]
  (fn [job-id title]
    [link/table-cell-hyperlink-href
     :label (gs/unescapeEntities title)
     :href (str "#/job/" job-id)]))

(defn is-expired?
  [status expire-time]
  (and expire-time (= status "Approved") (> (c/to-long (t/now)) (c/to-long expire-time))))

(defn status-data
  [status exp featured pending-promoted]
  (fn [status exp featured pending-promoted]
    (let [promotion (ju/current-promotion featured)]
      [:td.status (cond
                    (is-expired? status exp) "Expired"
                    (and (= status "Approved") promotion) [misc/flag "Promoted"]
                    (= status "Approved") "Active"
                    (and (= status "Pending") pending-promoted) [misc/flag "Promotion Pending"]
                    :else status)])))

(defn promote-action
  [job-id featured]
  (let [has-promotions? (rf/subscribe [:recruiter/has-promotions?])]
    (fn [job-id featured]
      (let [promotion (ju/current-promotion featured)]
        [icon/promote
         :disabled? (some? promotion)
         :on-click (cond
                     (and @has-promotions? (not promotion)) #(rf/dispatch [:jobs/promote-job-id-change job-id])
                     (not promotion) #(rf/dispatch [:checkout/promote-checkout-modal job-id])
                     :else #())]))))

(defn edit-action
  [job-id]
  [icon/pencil
   :on-click #(rf/dispatch [:jobs/edit-job job-id])])

(defn remove-action
  [job-id]
  [icon/x
   :on-click #(rf/dispatch [:jobs/remove-job-click job-id])])

(defn repost-action
  [job-id]
  (fn [job-id]
    [link/hyperlink
     :label "Repost"
     :on-click #(rf/dispatch [:jobs/repost-job job-id])]))

(defn approved-actions
  [job-id featured]
  (fn [job-id featured]
    [layout/row
     :padding 0
     :children [[layout/col-left
                 :padding-right 6
                 :children [[promote-action job-id featured]]]
                [layout/column
                 :padding 6
                 :children [[edit-action job-id]]]
                [layout/col-right
                 :padding-left 6
                 :children [[remove-action job-id]]]]]))

(defn pending-actions
  [job-id]
  (fn [job-id]
    [layout/row
     :children [[layout/col-left
                 :padding-right 6
                 :children [[edit-action job-id]]]
                [layout/col-right
                 :padding-left 6
                 :children [[remove-action job-id]]]]]))

(defn expired-actions
  [job-id]
  (fn [job-id]
    [rc/h-box
     :children [[repost-action job-id]]]))

(defn row-actions
  [job-status job-id expire-time featured]
  (fn [job-status job-id expire-time featured]
    [:td.actions (cond (is-expired? job-status expire-time) [expired-actions job-id]
                       (= job-status "Approved") [approved-actions job-id featured]
                       (= job-status "Pending") [pending-actions job-id])]))

(defn job-row-data
  [_ {:keys [locations job_id job_status title company_name featured
             pending-promoted]
      :as   job}]
  (let [expanded (reagent/atom nil)
        expire-time (ju/expire-time job)]
    [(ju/posted-str job)
     (ju/unparsed-date expire-time)
     [title-link job_id title]
     (gs/unescapeEntities company_name)
     [loc-data locations expanded job_id]
     [view-data locations expanded job_id]
     [app-data locations expanded job_id]
     [status-data job_status expire-time featured pending-promoted]
     [row-actions job_status job_id expire-time featured]]))

(def job-table-headers
  "Returns headers for job tables"
  [{:label "Posted"
    :sort  {:posted (sort-util/sort-fn ju/posted-date sort-util/before?)}
    :width 7.4}
   {:label "Expires"
    :sort  {:expired (sort-util/sort-fn ju/posted-date sort-util/before?)}
    :width 7.3}
   {:label "Title"
    :sort  {:title (sort-util/sort-fn :title compare)}
    :width 20.7}
   {:label "Company"
    :sort  {:company (sort-util/sort-fn :company_name compare)}
    :width 17.7}
   {:label "Location"
    :width 17.5}
   {:label "Views"
    :sort  {:views (sort-util/sort-fn #(transduce (map (comp count :views)) + (:locations %)) compare)}
    :width 5.8}
   {:label "Applied"
    :sort  {:applies (sort-util/sort-fn #(transduce (map (comp count :applicants)) + (:locations %)) compare)}
    :width 7.2}
   {:label "Status"
    :width 7.3}
   {:label "Edit"
    :width 9.1}])

(defn jobs-table
  "Renders job table for given type"
  [table-key data-sub]
  [table/frontend-pagination-table
   :table-key table-key
   :headers job-table-headers
   :initial-sort-col :posted
   :initial-sort-dir :desc
   :row-data-fn job-row-data
   :data-sub data-sub])

(defn active-jobs-table
  []
  [jobs-table ::table/active-jobs :jobs/active])

(defn pending-jobs-table
  []
  [jobs-table ::table/pending-jobs :jobs/pending])

(defn rejected-jobs-table
  []
  [jobs-table ::table/rejected-jobs :jobs/rejected])

(defn removed-jobs-table
  []
  [jobs-table ::table/removed-jobs :jobs/removed])

(defn all-jobs-table
  []
  [jobs-table ::table/jobs :jobs/all])

(defn promoted-job-banner
  []
  (let [pj-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      [layout/column
       :padding 0
       :children [[:div
                   (use-style styles/promoted-job-count)
                   (str "Promoted jobs left: " @pj-count)]
                  [btn/job-promotion-btn
                   :label "Purchase More"
                   :on-click #(rf/dispatch [:go-to-shopify])]]])))

(defn promoted-date-range
  []
  (let [formatter (f/formatter "M/dd")
        now (t/now)
        begin-date (f/unparse formatter now)
        end-date (f/unparse formatter (t/plus now (t/days 56)))]
    (str begin-date " - " end-date)))

(defn promote-job
  []
  (let [job (rf/subscribe [:jobs/promote-job])]
    (fn []
      (when @job
        (rf/dispatch [::modal/open-modal ::modal/promote-job])
        [modal/modal
         :modal-key ::modal/promote-job
         :class "promote-job"
         :on-close #(rf/dispatch [:jobs/promote-job-id-change nil])
         :title "Confirm job promotion"
         :body [(str "Get ready to enjoy up to 8x more qualified candidates for your "
                     (-> @job :title gs/unescapeEntities)
                     " role. Your promoted job will last from "
                     (promoted-date-range)
                     ".")
                [rc/button
                 :class "submit-btn"
                 :label "Promote Job"
                 :on-click #(rf/dispatch [:jobs/promote-job (:job_id @job)])]]]))))

(defn purchase-again
  []
  (let [job (rf/subscribe [:jobs/promote-job])]
    (fn []
      (when @job
        (rf/dispatch [::modal/open-modal ::modal/purchase-again])
        [modal/modal
         :modal-key ::modal/purchase-again
         :class "purchase-again"
         :on-close #(rf/dispatch [:jobs/promote-job-id-change nil])
         :title "Uh oh.. you're out of promotions"
         :body [(str "Looks like you used your last available job promotion. Want to purchase more and promote " (-> @job :title gs/unescapeEntities) "?")
                [rc/button
                 :class "submit-btn"
                 :label "Purchase Job Promotion and Promote Job"
                 :on-click #(rf/dispatch [:go-to-shopify])]]]))))

(def promote-job-list ["Appear at the top of search results"
                       "Enjoy 8 weeks of promoted status"
                       "Receive 8x more qualified applicants"])

(defn purchase-for-job []
  (let [job (rf/subscribe [:jobs/purchase-job])]
    (fn []
      (when @job
        (rf/dispatch [::modal/open-modal ::modal/purchase-and-promote])
        [modal/modal
         :modal-key ::modal/purchase-and-promote
         :class "purchase-for-job"
         :on-close #(rf/dispatch [:jobs/purchase-job-id-change nil])
         :title (str "Promote " (-> @job :title gs/unescapeEntities) " for only $6.25 a day")
         :body [(reduce #(conj %1 [:li %2]) [:ul] promote-job-list)
                [:div {:class "modal-list-disclaimer"} "(Based on past performance of promoted jobs on Ladders)"]
                [rc/v-box
                 :align :center
                 :children [[rc/button
                             :class "submit-btn"
                             :label "Purchase Job Promotion"
                             :on-click #(rf/dispatch [:go-to-shopify])]]]
                [:div {:class "modal-disclaimer"} "Qualified candidates are those that meet the role, specialty, years of experience and salary of the job posting."
                 [:br] "The total cost of a promoted job is $350 for 8 weeks of promoted status."]]]))))

(defn remove-job []
  (let [job (rf/subscribe [:jobs/remove-job])]
    (fn []
      (when @job
        [modal/modal
         :modal-key ::modal/remove-job
         :on-close #(rf/dispatch [:jobs/remove-job-id-change nil])
         :title "Remove Job"
         :body [[type/modal-copy (str "Are you sure you want to remove the "
                                      (-> @job :title gs/unescapeEntities)
                                      " job posted on "
                                      (ju/posted-str @job)
                                      "?")]]
         :action {:label    "Remove"
                  :on-click #(rf/dispatch [:jobs/remove-job (:job_id @job)])}]))))

(defn modal []
  (let [purchase-job-id (rf/subscribe [:jobs/purchase-job-id])
        promote-job-id (rf/subscribe [:jobs/promote-job-id])
        remove-job-id (rf/subscribe [:jobs/remove-job-id])
        has-promotions? (rf/subscribe [:recruiter/has-promotions?])]
    (fn []
      (cond @purchase-job-id [purchase-for-job]
            (and @promote-job-id @has-promotions?) [promote-job]
            @promote-job-id [purchase-again]
            @remove-job-id [remove-job]))))

(defn tab-filters
  []
  (let [active-tab (rf/subscribe [:jobs/active-tab])]
    (fn []
      [layout/row
       :align :center
       :children [[form/form-label
                   :label "View:"]
                  [layout/column
                   :padding 6
                   :children [[btn/filter-button
                               :label "All"
                               :selected? (= @active-tab :tab4)
                               :on-click #(rf/dispatch [:jobs/active-tab-change :tab4])]]]
                  [layout/column
                   :padding 6
                   :children [[btn/filter-button
                               :label "Active"
                               :selected? (= @active-tab :tab0)
                               :on-click #(rf/dispatch [:jobs/active-tab-change :tab0])]]]
                  [layout/column
                   :padding 6
                   :children [[btn/filter-button
                               :label "Pending"
                               :selected? (= @active-tab :tab1)
                               :on-click #(rf/dispatch [:jobs/active-tab-change :tab1])]]]
                  [layout/column
                   :padding 6
                   :children [[btn/filter-button
                               :label "Rejected"
                               :selected? (= @active-tab :tab2)
                               :on-click #(rf/dispatch [:jobs/active-tab-change :tab2])]]]
                  [layout/column
                   :padding 6
                   :children [[btn/filter-button
                               :label "Removed & Expired"
                               :selected? (= @active-tab :tab3)
                               :on-click #(rf/dispatch [:jobs/active-tab-change :tab3])]]]]])))

(defn search-filter
  []
  [layout/row
   :align :center
   :children [[layout/col-left
               :padding 0
               :children [[form/form-label
                           :label "Search in Title:"]]]
              [layout/col-right
               :padding-left 4
               :children [[form/input-text
                           :ns "jobs"
                           :type "filter"]]]]])

(defn tabs-body
  []
  (let [active-tab (rf/subscribe [:jobs/active-tab])
        filter (rf/subscribe [:jobs/filter])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding-top 50
                   :padding-bottom 0
                   :children [[table/filter-bar
                               :justify :between
                               :align :center
                               :children [[layout/column
                                           :padding 0
                                           :children [[tab-filters]]]
                                          [layout/column
                                           :padding 0
                                           :children [[search-filter]]]]]]]
                  [layout/row
                   :padding 0
                   :children [(case @active-tab
                                :tab0 [active-jobs-table]
                                :tab1 [pending-jobs-table]
                                :tab2 [rejected-jobs-table]
                                :tab3 [removed-jobs-table]
                                :tab4 [all-jobs-table]
                                [:div "Oops, something went wrong, please refresh the page."])]]]])))

(defn header
  []
  [header/page-header
   :header-text "Manage Jobs"
   :sub-header-text "Manage your job posts and review applicants."
   :right-element [promoted-job-banner]])

(defn checkout_modal []
  (let [open-modal? (rf/subscribe [:checkout/open-modal])]
    (fn []
      (when @open-modal?
        (checkout/checkout-modal)))))

(defn index
  []
  (let [loaded? (rf/subscribe [:jobs/loaded?])]
    (rf/dispatch [:jobs/load-view])
    (fn []
      (if @loaded?
        [rc/v-box
         :class "jobs main"
         :children [[header]
                    [layout/page-content
                     [tabs-body]]
                    [modal]
                    [checkout_modal]]]
        [l/loading-page]))))
