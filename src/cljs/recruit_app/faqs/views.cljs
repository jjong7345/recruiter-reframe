(ns recruit-app.faqs.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [domina.events :as events]))

(def faq-information [{:title "Why is my account in pending?"
                    :text "To ensure only valid recruiters and jobseekers access the site, every profile is reviewed by our team. Once your information is approved, you will be able to use our site. This normally takes no longer than one hour."
                    :anchor "#one"}
                   {:title  "Why was my profile rejected?"
                    :text [:span "To ensure only valid recruiters and jobseekers access the site, every profile is reviewed by our team. While reviewing your recently submitted profile, we found that certain criteria did not meet our requirements. If you would like more information about why your profile was rejected you can contact us at " [:a {:href "mailto:approvals@theladders.com"} "approvals@theladders.com"] "."]
                    :anchor "#two"}
                   {:title "How do I get my verification code?"
                    :text "You will receive an email with the verification code once you sign up for an account. If you do not receive this email, you can request a resend of it on our site and we will be happy to verify your account for you."
                    :anchor "#three"}
                   {:title "Why is my job in pending and how long will it take to get approved?"
                    :text "All jobs on Ladders are reviewed by our teams to make sure all information has been input properly. After being submitted, your job is usually posted and approved within 30 minutes."
                    :anchor "#four"}
                   {:title "Will I be charged for posting?"
                    :text "You may post jobs for free.  However to boost your job's performance you can choose to promote it.  On average promoted jobs get 8 times more applies."
                    :anchor "#five"}
                   {:title "How many jobs can I post?"
                    :text "You can post as many jobs as you'd like to your Ladders Recruiter account."
                    :anchor "#six"}
                   {:title "Are there any types of roles I cannot post?"
                    :text "We do not permit any commision only, financial advisor, part-time/contract roles less than 12 months, or jobs with less than $80K in total compensation onto the site."
                    :anchor "#seven"}
                   {:title "I can't remember my password, can you help?"
                    :text "No problem! If you've forgotten your password, please click on \"I forgot my password\" on the login page and we'll send you a link to reset it."
                    :anchor "#eight"}
                   {:title "Does Ladders search require Boolean?"
                    :text "While we are fully Boolean compatible on Ladders, you are not required to use Boolean threads to search the database. Single keywords, job title, and locations will work just fine!"
                    :anchor "#nine"}
                   {:title "Why was my job rejected?"
                    :text [:span "Please be aware that any commission only, financial advisor, part-time and contract positions less than 12 months, and jobs with total compensation under $80K will not be approved onto our site.  If you would like more information as to why your job was rejected, please send us an email at " [:a {:href "mailto:approvals@theladders.com"} "approvals@theladders.com"] "."]
                    :anchor "#ten"}
                   {:title "Where can I view my applicants?"
                    :text "After logging in, you can access all candidates by going directly to 'Manage Jobs' and clicking on the number of 'Applies' to be directed to all applicants for your position."
                    :anchor "#eleven"}
                   {:title "How can I promote my job?"
                    :text "You may purchase promotions from our online store under \"products\" in the menu or while you post a job. If your job has already been approved and posted, you can promote it from the manage jobs page."
                    :anchor "#twelve"}
                   {:title "What is Full Access?"
                    :text [:span "Full Access is our annual premium seat license which allows you to be more efficient as a recruiter. To learn more, " [rc/hyperlink
                                                                                                                                                         :label "click here"
                                                                                                                                                         :on-click #(rf/dispatch [:go-to-pricing false])] "."]
                    :anchor "#thirteen"}])

(defn title
  []
  (fn []
    [rc/h-box
     :class "header-h1"
     :children [
                [:h1 {:class "title" :id "top"} "Recruiter FAQ"]]]))

(defn question [item]
  (fn [item]
    ^{:key item} [rc/hyperlink
                  :class "sublinks"
                  :label  (:title item)
                  :on-click #(rf/dispatch [:faqs/go-to-anchor (:anchor item)])]))

(defn questions []
  (fn []
    [rc/h-box
     :class "question-links"
     :children [[rc/v-box
                 :class "links"
                 :children (mapv (partial vector question) faq-information)]]]))

(defn answer [item]
  (fn [item]
    ^{:key item} [rc/v-box
                  :children [[:div {:id (:anchor item)}
                              [:div
                               [:h2 (:title item)]
                               [:p {:class "text"} (:text item)]]]]]))

(defn answers []
  (fn []
    [rc/h-box
     :class "answers-list"
     :children [[rc/v-box
                 :class "answers"
                 :children (mapv (partial vector answer) faq-information)]]]))

(defn back-to-top []
  (fn []
    [rc/hyperlink
     :class "sublinks"
     :label "Back to top"
     :style {:margin-top "30px"}
     :on-click #(rf/dispatch [:faqs/go-to-anchor "top"])]))

(defn body
  []
  (fn []
    [rc/h-box
     :class "content-holder"
     :children [
                [rc/v-box
                 :class "content"
                 :children [[title] [questions] [answers] [back-to-top]]]]]))

(defn index
  []
  (fn []
    [rc/h-box
     :class "faqs main"
     :children [[body]]]))