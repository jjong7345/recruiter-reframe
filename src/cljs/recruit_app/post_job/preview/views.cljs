(ns recruit-app.post-job.preview.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.util.job :as ju]
            [recruit-app.util.date :as f]
            [recruit-app.util.html :as html]
            [cljs-time.core :as t]
            [cljs.spec.alpha :as s]
            [hickory.core :as h]
            [goog.string :as gs]
            [goog.dom :as dom]
            [recruit-app.util.number_format :as nf]
            [cljs-time.format :as tf]))

(defn preview-btns []
  (let [job (rf/subscribe [:preview-job/job])]
    (fn []
      [rc/h-box
       :justify :end
       :class "button-group"
       :children [[rc/button
                   :class "btn-border"
                   :label "Edit Post"
                   :attr {:id "edit-btn"}
                   :on-click #(rf/dispatch [:post-job/see-post-job])]
                  [rc/button
                   :attr {:id "view-publish-btn"}
                   :class "publish-btn"
                   :label (if (and (:id @job) (:promoted? @job)) "Publish" "Continue with Posting")
                   :on-click #(rf/dispatch [:post-job/post-job])]]])))

(defn header
  []
  (fn []
    [rc/v-box
     :class "header-container"
     :children [[rc/h-box
                 :class "progress-status"
                 :children
                 [[:div.bar]
                  [:div.arrow]]]
                [rc/h-box
                 :class "dark"
                 :children [[:div.bar]
                            [:div.arrow]]]
                [rc/h-box
                 :class "banner-container"
                 :align :center
                 :justify :between
                 :children [[rc/h-box
                             :class "title-box"
                             :align :center
                             :children [[rc/label
                                         :class "banner-title"
                                         :label "Preview Job Post"]
                                        [preview-btns]]]]]]]))

(defn bottom-banner []
  (fn []
    [rc/h-box
     :class "banner-container bottom"
     :align :center
     :justify :end
     :children [[preview-btns]]]))

(defn job-details []
  (let [job (rf/subscribe [:preview-job/job])]
    (fn []
      (let [loc-count (count (:locations @job))]
        [rc/h-box
         :children [[rc/v-box
                     :class "job-details"
                     :children [[:div (str (if (:hide-company @job) "Confidential Company" (gs/unescapeEntities (:company @job))) " | "
                                           (-> @job :locations first :name)
                                           (when (> loc-count 1) (str " and " (dec loc-count) " other location"
                                                                      (when (> loc-count 2) "s "))))]
                                [:div (str (when-not (= (:exp-string @job) "Not Specified")
                                             (str (:exp-string @job) " Years Experience")))]]]]]))))

(defn salary-range []
  (let [job (rf/subscribe [:preview-job/job])]
    (fn []
      (if (:hide-salary @job)
        [:div.salary "Salary depends on experience"]
        [:div.salary
         [:span.total-salary (ju/base-salary-preview-string (:min-total-comp @job) (:max-total-comp @job))]
         [:span.salary-details (ju/salary-details-string @job)]]))))

(defn- posted-date
  "Formats date string into MM/YY"
  [date-string]
  (try
    (tf/unparse
      (tf/formatter "M/dd")
      (tf/parse (tf/formatters :date-time-no-ms) date-string))
    (catch js/Error e "")))

(defn recruiter []
  (let [recruiter-name (rf/subscribe [:recruiter/full-name])
        job (rf/subscribe [:preview-job/job])
        rec-profile-img (rf/subscribe [:recruiter/profile-img])]
    (fn []
      (when (not (:hide-recruiter @job))
        [rc/h-box
         :children [[:div (str "Posted on " (posted-date (:posted_date @job)) " by ")
                     [rc/hyperlink
                      :label @recruiter-name
                      :on-click #(rf/dispatch [:recruiter/go-to-profile])]
                     [:img {:src   @rec-profile-img
                            :class "circle-img"}]]]]))))

(defn job-header []
  (let [job (rf/subscribe [:preview-job/job])]
    (fn []
      [rc/v-box
       :class "job-header"
       :children [[rc/h-box
                   :class "cols"
                   :justify :between
                   :children [[rc/v-box
                               :class "left-col"
                               :children [[rc/label
                                           :class "job-title"
                                           :label (gs/unescapeEntities (:job-title @job))]
                                          [job-details]]]
                              [rc/v-box
                               :class "right-col"
                               :children [[:div.apply-btn "Quick Apply"] [:div.save-job "Save Job"]]]]]
                  [rc/h-box
                   :class "sal-rec"
                   :justify :between
                   :children [[salary-range] [recruiter]]]]])))

(defn job-desc []
  (let [job (rf/subscribe [:preview-job/job])]
    (fn []
      [rc/v-box
       :class "job-desc"
       :children (html/html-to-hiccup (:job-desc @job))])))

(defn preview []
  (fn []
    [rc/v-box
     :children [[job-header]
                [:div.divider]
                [job-desc]]]))

(defn body
  []
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [[rc/v-box
                 :class "content"
                 :children [[preview]]]]]))

(defn index
  []
  (fn []
    (rf/dispatch [:post-job.preview/load-view])
    [rc/v-box
     :class "post-job-preview job main"
     :children [[header] [body] [bottom-banner]]]))