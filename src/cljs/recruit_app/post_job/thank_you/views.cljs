(ns recruit-app.post-job.thank-you.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [recruit-app.util.education :as eu]
            [recruit-app.util.date :as f]
            [recruit-app.util.img :as img]))

;alone
(defn approved-promoted-header
  []
  (fn []
    [rc/h-box
     :class "banner-container"
     :align :center
     :justify :between
     :children [[rc/v-box
                 :class "title-box"
                 :children [[:div {:class "banner-title"} "Thanks for posting your job. Your job is pending approval."]
                            [:div {:class "banner-text "} "Rest assured, your job will be approved and promoted within approximately 30 minutes. Once your job post is approved, you will receive a confirmation email. You may also check the status of your post in "
                             [rc/hyperlink
                              :label "Manage Jobs"
                              :on-click #(rf/dispatch [:go-to-manage-jobs])] "."]]]]]))

;with promote-this-job and with promote-boost-job
(defn approved-header
  []
  (fn []
    [rc/h-box
     :class "banner-container"
     :align :center
     :justify :between
     :children [[rc/v-box
                 :class "title-box"
                 :children [[:div {:class "banner-title"} "Thanks for posting your job. Your job is pending approval."]
                            [:div {:class "banner-text "} "Rest assured, your job will be approved within approximately 30 minutes. Once your job post is approved, you will receive a confirmation email. You may also check the status of your post in "
                             [rc/hyperlink
                              :label "Manage Jobs"
                              :on-click #(rf/dispatch [:go-to-manage-jobs])] "."]]]]]))

(defn progress-bar
  []
  [rc/h-box
   :class "progress-bar"
   :children []])

(defn candidate-title
  []
  (let [title (rf/subscribe [:post-job.thank-you/job-title])]
    [rc/h-box
     :justify :between
     :class "candidate-title"
     :children [[:p "Suggested candidates for " [:span.extra-bold @title]]]]))

(defn formatted-string [input]
  (take 1 (clojure.string/split input #"-")))

(defn table-list [items row-type]
  (fn []
    (let [rows (map #(vector row-type %) items)]
      [:table {:style {:margin 0}} (into [:tbody] rows)])))

(defn company-row [company-item]
  (fn []
    [:tr [:td (:companyName company-item)]]))

(defn title-row [title-item]
  (fn []
    [:tr [:td (:title title-item)]]))

(defn time-row [time-item]
  (fn []
    [:tr [:td (f/year-of-ms-date (:startDate time-item)) " - " (if (:endDate time-item) (f/year-of-ms-date (:endDate time-item)) "Present")]]))

(defn table-row-default [candidate]
  (fn [candidate]
    (let [candidate-name (:candidateName candidate)
          candidate-id (:secureId candidate)
          location (:location candidate)
          education (int (:educationDegreeId candidate))
          desired (:compensation candidate)
          exp-list (take 3 (get-in candidate [:profile :experience]))

          date-list (get-in candidate [:jobSeekerHistory])]
      ^{:key (:jobSeekerId candidate)} [:tr
                                        [:td [:a {:href (str "/resumeviewer?jobseekerId=" candidate-id)} candidate-name]]
                                        [:td location]
                                        [:td (eu/edu-name education)]
                                        [:td desired]
                                        [:td.title.ellipsis [table-list exp-list title-row]]
                                        [:td.company.ellipsis [table-list exp-list company-row]]
                                        [:td [table-list exp-list time-row]]])))

(defn styled-table-row [candidate]
  (fn [candidate]
    [table-row-default candidate]))

(defn table-header []
  [:tr.rc-div-table-header
   [:th "Name"]
   [:th "Location"]
   [:th "Education"]
   [:th "Desired"]
   [:th "Title"]
   [:th "Company"]
   [:th.tir {:style {:width "110px" :display "block"}} "Time in Role"]])

(defn table [candidates row-cmp]
  [:table.table
   [:tbody
    [table-header]
    (for [item @candidates]
      ^{:key (:id item)}
      [row-cmp item])]])

(defn styled-table
  []
  (let [candidates (rf/subscribe [:post-job.thank-you/suggested-candidates])]
    (fn []
      [table candidates styled-table-row])))

(defn suggested-candidates
  []
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [[rc/v-box
                 :class "content"
                 :children [[candidate-title] [styled-table]]]]]))

(defn promote-with-shopify-redirect-box
  [title desc btn-text]
  (fn []
    [rc/v-box
     :class "promo-box"
     :children [[:h2 {:class "banner-title"} title]
                [:div {:class "banner-text"} desc]
                [rc/button
                 :class "orange"
                 :label btn-text
                 :on-click #(rf/dispatch [:go-to-shopify])]]]))

(defn promote-boost-job-box
  [title desc btn-text]
  (fn []
    [rc/v-box
     :class "promo-box"
     :children [[:h2 {:class "banner-title"} title]
                [:div {:class "banner-text"} desc]
                [rc/button
                 :class "orange"
                 :label btn-text
                 :on-click #(rf/dispatch [:post-job.thank-you/promote-pending-job])]]]))

(defn promote-with-managejobs-redirect-box
  [title desc btn-text]
  (fn []
    [rc/v-box
     :class "promo-box"
     :children [[:h2 {:class "banner-title"} title]
                [:div {:class "banner-text"} desc]
                [rc/button
                 :class "orange"
                 :label btn-text
                 :on-click #(rf/dispatch [:post-job/goto-managejobs])]]]))

(defn promote-this-job []
  (fn []
    (let [title ((fn [] "Are you sure you don’t want to boost your job’s performance?"))
          desc ((fn [] "Get the best applicants by promoting your job for only $6.25 a day.  "))
          btn-text ((fn [] "Promote This Job"))]
      [promote-with-shopify-redirect-box title desc btn-text])))

(defn promote-boost-job []
  (let [pj-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (let [title ((fn [] "Are you sure you don’t want to boost your job’s performance?"))
            desc ((fn [] [:div {:class "job-prom-container"} [:span "You have "] [:span {:class "you-have-job-prom"} @pj-count] [:span " job promotions available."]]))
            btn-text ((fn [] "Promote This Job"))]
        [promote-boost-job-box title desc btn-text]))))

(defn promote-more-jobs []
  (fn []
    (let [title ((fn [] "Want to promote more jobs?"))
          desc ((fn [] "Hire the best candidates by promoting your jobs for only $6.25 a day. "))
          btn-text ((fn [] "Purchase Job Promotions"))]
      [promote-with-shopify-redirect-box title desc btn-text])))

(defn promote-boost-more-jobs []
  (let [pj-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (let [title ((fn [] "Want to promote more jobs?"))
            desc ((fn [] [:div {:class "job-prom-container"} [:span "You have "] [:span {:class "you-have-job-prom"} @pj-count] [:span " job promotions available."]]))
            btn-text ((fn [] "Promote More Jobs"))]
        [promote-with-managejobs-redirect-box title desc btn-text]))))

(defn show-header
  []
  (let [promoted? (rf/subscribe [:post-job.thank-you/promoted?])]
    (fn []
      [rc/v-box
       :class "banner-container"
       :children [(if @promoted? [approved-promoted-header]
                                 [approved-header])]])))

(defn show-not-promoted-banner
  []
  (let [promoted? (rf/subscribe [:post-job.thank-you/promoted?])
        pj-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (cond (and (not @promoted?) (> @pj-count 0))
            [rc/h-box
             :class "banner-wrapper"
             :align :center
             :children [[rc/h-box
                         :class "banner-content"
                         :justify :between
                         :children [[promote-boost-job]
                                    [:img {:width 450 :height 281 :src (img/url :promo-img-src)}]]]]]
            (and (not @promoted?) (= @pj-count 0))
            [rc/h-box
             :class "banner-wrapper"
             :align :center
             :children [[rc/h-box
                         :class "banner-content"
                         :justify :between
                         :children [[promote-this-job]
                                    [:img {:width 450 :height 281 :src (img/url :promo-img-src)}]]]]]))))

(defn show-promoted-banner
  []
  (let [promoted? (rf/subscribe [:post-job.thank-you/promoted?])
        pj-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (cond (and @promoted? (> @pj-count 0))
            [rc/h-box
             :class "banner-wrapper-bottom"
             :align :center
             :children [[rc/h-box
                         :class "banner-content"
                         :justify :between
                         :children [[promote-boost-more-jobs]
                                    [:img {:width 400 :height 250 :src (img/url :promo-img-src)}]]]]]
            (and @promoted? (= @pj-count 0))
            [rc/h-box
             :class "banner-wrapper-bottom"
             :align :center
             :children [[rc/h-box
                         :class "banner-content"
                         :justify :between
                         :children [[promote-more-jobs]
                                    [:img {:width 400 :height 250 :src (img/url :promo-img-src)}]]]]]))))
(defn index
  []
  (let [show-suggested-candidates? (rf/subscribe [:post-job.thank-you/show-suggested-candidates?])]
    (fn []
      (rf/dispatch [:post-job.thank-you/load-view])
      [rc/v-box
       :class "post thank-you main"
       :children [[progress-bar]
                  [show-header]
                  [show-not-promoted-banner]
                  (when @show-suggested-candidates?
                    [suggested-candidates])
                  [show-promoted-banner]]])))
