(ns recruit-app.post-job.publish.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [recruit-app.util.date :as f]
            [recruit-app.modals.checkout.views :as checkout]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs.spec.alpha :as s]
            [hickory.core :as h]
            [goog.string :as gs]
            [recruit-app.util.img :as img]))

(defn header
  []
  (let [variation (rf/subscribe [:variation])
        pjl-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :class "progress-status"
                   :children
                   [[:div.bar]]]
                  [rc/h-box
                   :class "dark"
                   :children [[:div.bar]
                              [:div.arrow]]]

                  [rc/h-box
                   :class "banner-container"
                   :justify :between
                   :children [(if (or (> (int @pjl-count) 0) (= 0 @variation))
                                [rc/h-box
                                 :class "title-box"
                                 :children [[rc/label
                                             :class "banner-title"
                                             :label "Publish Job Post"]]]
                                [rc/h-box
                                 :class "title-box"
                                 :justify :between
                                 :children [[rc/v-box
                                             :children [[rc/label
                                                         :class "banner-title promote"
                                                         :label "Promote Your Job"]
                                                        [rc/label
                                                         :class "banner-subtitle"
                                                         :label "Appear at the top of search for only $6.25/day"]]]
                                            [rc/button :class "promote-btn" :attr {:id "publish-promote-btn"} :label "Promote & Publish" :on-click #(rf/dispatch [:checkout/promote-checkout-modal])]]])]]]])))

(defn promote-list []
  (fn []
    [rc/v-box
     :children [[rc/label :class "bullet" :label [:span [:span.blt "-"] "Appear at the top of search results"]]
                [rc/label :class "bullet" :label [:span [:span.blt "-"] "Enjoy 8 weeks of promoted status"]]
                [rc/label :class "bullet" :label [:span [:span.blt "-"] "Receive 8x more qualified applicants"]]]]))

(defn publish-view []
  (fn []
    [rc/v-box
     :class "publish-view"
     :children [[rc/label :class "title" :label "Or, publish without promoting"]
                [rc/label :class "bullet" :label [:span [:span.blt "-"] "Appear in search results"]]
                [rc/label :class "bullet" :label [:span [:span.blt "-"] "Enjoy 8 weeks of posting"]]
                [rc/button :class "publish-btn" :attr {:id "publish-regular-btn"} :label "Publish without Paid Promotion" :on-click #(rf/dispatch [:post-job.publish/thank-you-passhthru])]]]))

(defn promote-view []
  (let [pjl-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (let [pjl-cnt (int @pjl-count)]
        [rc/v-box
         :class "promote-view"
         :children [(cond
                      (= 0 pjl-cnt) [:div.title "Get the best applicants with a promoted job for only $6.25 a day "]
                      (= 1 pjl-cnt) [:div.title "Use your " [:span.pjl-count 1] " remaining job promotion to promote this job"]
                      :else [:div.title "Use one of your " [:span.pjl-count pjl-cnt] " available job promotions to promote this job"])
                    [promote-list]
                    [rc/label :class "details" :label "(Based on past performance of promoted jobs on Ladders)"]
                    [rc/button :class "publish-btn" :attr {:id "publish-promote-btn"} :label "Publish & Promote" :on-click (if (> pjl-cnt 0) #(rf/dispatch [:post-job.publish/promote-from-publish])
                                                                                                                                             #(rf/dispatch [:checkout/promote-checkout-modal]))]]]))))

(defn promote-pub-view []
  (let [variation (rf/subscribe [:variation])
        pjl-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (let [pjl-cnt (int @pjl-count)]
        (if (= 0 @variation)
          [rc/h-box
           :class "content-holder"
           :children [[rc/h-box
                       :class "content"
                       :children [[rc/v-box
                                   :class "promote-pub"
                                   :children [[rc/label :class "title" :label "Get the best applicants with a promoted job for only $6.25 a day"]
                                              [rc/v-box :children [[rc/label :class "bullet" :label [:span [:span.blt "-"] "Appear at the top of search results"]]
                                                                   [rc/label :class "bullet" :label [:span [:span.blt "-"] "Enjoy 8 weeks of promoted status"]]
                                                                   [rc/label :class "bullet" :label [:span [:span.blt "-"] "Recieve 8x more qualified applicants"]]
                                                                   [rc/label :class "details" :label "(Based on past performance of promoted jobs on Ladders)"]]]
                                              [rc/button :class "promote-btn" :attr {:id "publish-promote-btn"} :label "Publish & Promote" :on-click (if (> pjl-cnt 0) #(rf/dispatch [:post-job.publish/promote-from-publish])
                                                                                                                                                                       #(rf/dispatch [:checkout/promote-checkout-modal]))]
                                              [rc/button :class "publish-btn" :attr {:id "publish-regular-btn"} :label "or Publish without Promotion" :on-click #(rf/dispatch [:post-job.publish/thank-you-passhthru])]
                                              [rc/label :class "qual details" :label [:span "Qualified candidates are those that meet the role, specialty, years of experience and salary of the job posting. " [:br] "The total cost of a promoted job is $350 for 8 weeks of promoted status."]]]]
                                  [rc/box :child [:img.promote-banner {:src (img/url :promote-banner)}]]]]]]
          [rc/h-box
           :class "content-holder"
           :children [[rc/h-box
                       :class "content"
                       :children [[rc/v-box
                                   :class "promote-pub"
                                   :children [[rc/label :class "sub-title" :label "Or publish without promoting, and receive fewer applicants."]
                                              [rc/v-box :children ["Appear beneath promoted jobs in search listings for two months"]]
                                              [rc/button :class "publish-btn underline" :attr {:id "publish-regular-btn"} :label "Publish without Promoting" :on-click #(rf/dispatch [:post-job.publish/thank-you-passhthru])]
                                              [rc/label :class "qual details" :label [:span "The total cost of a promoted job is $350 for 8 weeks of promoted status."]]]]]]]])))))

(defn body
  []
  (let [pjl-count (rf/subscribe [:recruiter/pjl-count])]
    (fn []
      (if (> (int @pjl-count) 0)
        [rc/h-box
         :class "content-holder"
         :children [[rc/v-box
                     :class "content"
                     :children [[rc/h-box
                                 :class "promote-holder"
                                 :children [[promote-view] [publish-view]]]
                                [rc/label :class "detail" :label "Qualified candidates are those that meet the role, specialty, years of experience and salary of the job posting.\n"]
                                [rc/label :class "detail" :label "The total cost of a promoted job is $350 for 8 weeks of promoted status."]]]]]
        [promote-pub-view]))))

(defn modal []
  (let [open-modal? (rf/subscribe [:checkout/open-modal])]
    (fn []
      (when @open-modal?
        (checkout/checkout-modal)))))

(defn index
  []
  (fn []
    (rf/dispatch [:post-job.publish/load-view])
    [rc/v-box
     :class "post-job-publish job main"
     :children [[header] [body] [modal]]]))