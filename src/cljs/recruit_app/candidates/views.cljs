(ns recruit-app.candidates.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as r]
            [recruit-app.modals.email.views :as email]
            [recruit-app.modals.candidate-notes.views :as notes]
            [recruit-app.components.modal :as modal]
            [recruit-app.modals.share-resume.views :as sr]
            [recruit-app.util.candidate :as cu]
            [recruit-app.util.uri :as u]
            [recruit-app.modals.save_candidate.views :as sc]
            [recruit-app.search.views :as s]
            [recruit-app.modals.confirm-email.views :as ce]
            [recruit-app.modals.create-account.views :as ca]
            [recruit-app.modals.ats-select-job.views :as ats]
            [cljs-time.format :as f]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.img :as img]
            [recruit-app.components.loading :as l]
            [recruit-app.components.typography :as type]
            [recruit-app.components.layout :as layout]
            [recruit-app.search-results.views :as search-results]
            [recruit-app.components.misc :as misc]))

(defn previous-candidate
  []
  (let [route (rf/subscribe [:candidates/previous-candidate-route])
        previous-page-loaded? (rf/subscribe [:candidates/previous-page-loaded?])
        previous-page (rf/subscribe [:candidates/previous-page])]
    (fn []
      (when-not @previous-page-loaded?
        (rf/dispatch [:candidates/load-page @previous-page]))
      (if @route
        [rc/hyperlink
         :class "previous-candidate"
         :label "Previous"
         :on-click #(rf/dispatch [:go-to-route @route])]
        [:span.previous-candidate "Previous"]))))

(defn candidate-view-info
  []
  (let [title (rf/subscribe [:candidates/list-title])
        current (rf/subscribe [:candidates/current-candidate-number])
        total (rf/subscribe [:candidates/total-candidates])
        back-route (rf/subscribe [:candidates/back-route])]
    (fn []
      [rc/hyperlink-href
       :label (str "<- Back to Search Results "
                   (when @title (str @title " - "))
                   "Candidate " @current " of " (if (< @total 1000) @total "999+"))
       :href @back-route])))

(defn next-candidate
  []
  (let [next-route (rf/subscribe [:candidates/next-candidate-route])
        next-page-loaded? (rf/subscribe [:candidates/next-page-loaded?])
        next-page (rf/subscribe [:candidates/next-page])]
    (fn []
      (when-not @next-page-loaded?
        (rf/dispatch [:candidates/load-page @next-page]))
      (if @next-route
        [rc/hyperlink
         :class "next-candidate"
         :label "Next"
         :on-click #(rf/dispatch [:go-to-route @next-route])]
        [:span.next-candidate "Next"]))))

(defn candidate-scroller
  []
  (let [prev-route (rf/subscribe [:candidates/previous-candidate-route])
        next-route (rf/subscribe [:candidates/next-candidate-route])]
    (fn []
      (rf/dispatch [:candidates/register-candidate-keypress @prev-route @next-route])
      [rc/h-box
       :class "candidate-scroller"
       :justify :between
       :children [[previous-candidate]
                  [candidate-view-info]
                  [next-candidate]]])))

(def divider [:div.candidate-info-divider "|"])

(defn history-row
  [{:keys [details time-in-role]}]
  (fn [{:keys [details time-in-role]}]
    [rc/h-box
     :children [[:div.details details]
                divider
                [:div.time-in-role time-in-role]]]))

(defn candidate-history
  []
  (let [history (rf/subscribe [:candidates/active-candidate-history])]
    (fn []
      [rc/v-box
       :class "candidate-history"
       :children (reduce #(conj %1 [history-row %2]) [] (take 3 @history))])))

(defn candidate-desired
  [desired]
  (fn [desired]
    [:div.candidate-desired desired]))

(defn candidate-location
  [location]
  (fn [location]
    [:div.candidate-location location]))

(defn work-experience
  [years-exp]
  [:div (str years-exp " years experience")])

(defn candidate-second-row
  []
  (let [location (rf/subscribe [:candidates/active-candidate-location])
        desired (rf/subscribe [:candidates/active-candidate-desired])
        years-experience (rf/subscribe [:candidates/active-candidate-years-experience])]
    (fn []
      [rc/h-box
       :children [[candidate-location @location]
                  (when (and (seq @location) (seq @desired))
                    divider)
                  [candidate-desired @desired]
                  (when (and (seq @years-experience) (or (seq @location) (seq @desired)))
                    divider)
                  (when (seq @years-experience)
                    [work-experience @years-experience])]])))

(defn candidate-name
  []
  (let [name (rf/subscribe [:candidates/active-candidate-name])]
    (fn []
      [:div.candidate-name @name])))

(defn candidate-last-active
  []
  (let [last-active-date (rf/subscribe [:candidates/last-active-date])]
    (fn []
      (when @last-active-date
        [:div.candidate-last-active @last-active-date]))))

(defn candidate-info
  []
  (fn []
    [rc/v-box
     :class "candidate-info"
     :children [[candidate-name]
                [candidate-last-active]
                [candidate-second-row]
                [candidate-history]]]))

(defn contact-buttons
  []
  (let [candidate (rf/subscribe [:candidates/active-candidate])
        secure-id (rf/subscribe [:candidates/active-id])
        guest? (rf/subscribe [:recruiter/guest?])]
    (fn []
      [rc/h-box
       :justify :end
       :class "action-btn-holder"
       :children [[rc/button
                   :class "action-btn"
                   :label "Email"
                   :on-click #(rf/dispatch (if @guest?
                                             [:confirm-email/click-button-unauthenticated]
                                             [:candidates/email-candidate @candidate]))]]])))

(defn more-button
  [is-hovered?]
  (fn [is-hovered?]
    [rc/button
     :label "More ..."
     :class "more-button btn-border"
     :attr {:on-mouse-enter (handler-fn (reset! is-hovered? true))}]))

(defn export-to-ats-btn
  []
  (let [secure-id (rf/subscribe [:candidates/active-id])
        ats-job-required? (rf/subscribe [:recruiter/ats-job-required?])]
    (fn []
      [rc/hyperlink
       :label "Export to ATS"
       :class "more-actions-link"
       :on-click #(rf/dispatch (if @ats-job-required?
                                 [:ats-select-job/open-modal @secure-id]
                                 [:candidates/export-to-ats @secure-id]))])))

(defn setup-ats-btn
  []
  (let [guest? (rf/subscribe [:recruiter/guest?])]
    (fn []
      [rc/hyperlink
       :label "Setup ATS"
       :class "more-actions-link"
       :on-click #(rf/dispatch (if @guest?
                                 [:confirm-email/click-button-unauthenticated]
                                 [:recruiter/go-to-ats-settings]))])))

(defn candidate-notes-btn
  []
  (let [secure-id (rf/subscribe [:candidates/active-id])
        note (rf/subscribe [:candidate-notes/active-candidate-notes])
        guest? (rf/subscribe [:recruiter/guest?])]
    (fn []
      [rc/hyperlink
       :label (if (seq @note) "View Note" "Add Note")
       :class "more-actions-link"
       :on-click #(rf/dispatch (if @guest?
                                 [:confirm-email/click-button-unauthenticated]
                                 [:candidate-notes/open-modal @secure-id]))])))

(defn save-candidate-btn
  []
  (let [secure-id (rf/subscribe [:candidates/active-id])
        guest? (rf/subscribe [:recruiter/guest?])]
    (fn []
      [rc/hyperlink
       :label "Save candidate"
       :class "more-actions-link"
       :on-click #(rf/dispatch (if @guest?
                                 [:confirm-email/click-button-unauthenticated]
                                 [:projects/open-save-candidate-modal @secure-id]))])))

(defn more-actions
  [is-hovered?]
  (let [show-export-to-ats-btn? (rf/subscribe [:candidates/show-export-to-ats-btn?])
        show-setup-ats-btn? (rf/subscribe [:candidates/show-setup-ats-btn?])]
    (fn [is-hovered?]
      [rc/v-box
       :class "more-actions btn-border"
       :attr {:on-mouse-leave (handler-fn (reset! is-hovered? false))}
       :children [[save-candidate-btn]
                  (cond
                    @show-export-to-ats-btn? [export-to-ats-btn]
                    @show-setup-ats-btn? [setup-ats-btn])
                  [candidate-notes-btn]]])))

(defn sub-action-buttons
  []
  (let [is-hovered? (r/atom false)]
    (fn []
      [rc/h-box
       :class "more-toggle"
       :justify :end
       :children [(if @is-hovered?
                    [more-actions is-hovered?]
                    [more-button is-hovered?])]])))

(defn actions
  []
  (fn []
    [rc/v-box
     :class "candidate-actions"
     :children [[contact-buttons] [sub-action-buttons]]]))

(defn header
  []
  [rc/h-box
   :class "candidate-header"
   :children [[candidate-info] [actions]]])

(defn resume-actions
  []
  (let [secure-id (rf/subscribe [:candidates/active-id])
        candidate (rf/subscribe [:candidates/active-candidate])
        guest? (rf/subscribe [:recruiter/guest?])
        job-location-id (rf/subscribe [:job/active-job-loc-id])
        resume-version (rf/subscribe [:candidates/active-candidate-resume-version])]
    (fn []
      [rc/h-box
       :justify :end
       :class "resume-actions"
       :children [[rc/hyperlink
                   :label "Download Resume"
                   :class "resume-action"
                   :on-click #(rf/dispatch [:candidates/download-resume @candidate @job-location-id @resume-version])]
                  (when-not @guest? divider)
                  (when-not @guest?
                    [rc/hyperlink
                     :label "Share"
                     :class "resume-action"
                     :on-click #(rf/dispatch [:share-resume/open-modal @secure-id @job-location-id @resume-version])])]])))

(def no-resume-view [:div.no-resume "This candidate has not provided a resume."])

(defn resume-iframe
  []
  (let [resume-url (rf/subscribe [:candidates/resume-url])]
    (fn []
      [rc/h-box
       :class "resume-holder"
       :width "100%"
       :children [[:iframe {:class  "resume-iframe"
                            :width  "100%"
                            :height "700px"
                            :src    (str (u/uri :pdf-viewer) "?file=" @resume-url)}]]])))

(defn resume
  "Note: This was split up into so many sub-components to ensure view only tracked once"
  []
  (let [has-resume? (rf/subscribe [:candidates/active-candidate-has-resume?])
        secure-id (rf/subscribe [:candidates/active-id])
        resume-version (rf/subscribe [:candidates/active-candidate-resume-version])
        candidate-id (rf/subscribe [:candidates/active-candidate-id])
        job-location-id (rf/subscribe [:job/active-job-loc-id])
        job-id (rf/subscribe [:job/active-job-id])]
    (fn []
      (rf/dispatch [:candidates/track-candidate-view @secure-id @resume-version @candidate-id @job-location-id @job-id])
      (if @has-resume?
        [resume-iframe]
        no-resume-view))))

(defn resume-viewer
  []
  (let [secure-id (rf/subscribe [:candidates/active-id])
        metadata-fetched? (rf/subscribe [:candidates/active-candidate-metadata-fetched?])]
    (fn []
      (when @secure-id
        (if @metadata-fetched?
          [resume]
          (rf/dispatch [:resume/get-resume-metadata @secure-id]))))))

(defn resume-upload-date-tag
  []
  (let [upload-date (rf/subscribe [:candidates/resume-upload-date])]
    (fn []
      (when @upload-date
        [:div.resume-upload-date-tag (str "Uploaded " @upload-date)]))))

(defn resume-holder
  []
  (let [has-resume? (rf/subscribe [:candidates/active-candidate-has-resume?])]
    (fn []
      [rc/v-box
       :class "content"
       :style {:position "relative"}
       :children [(when @has-resume? [resume-actions]) [resume-viewer] [resume-upload-date-tag]]])))

(defn exporting-modal
  []
  [modal/modal
   :modal-key ::modal/exporting-to-ats
   :body [[layout/row
           :justify :center
           :children [[l/loading-circle-large
                       :class "loading-icon-holder"]]]]])

(defn export-success-modal
  []
  [modal/modal
   :modal-key ::modal/export-to-ats-success
   :title "Success!"
   :body [[type/modal-copy "The candidate was successfully saved."]]
   :action {:label    "Continue"
            :on-click #(rf/dispatch [::modal/close-modal ::modal/export-to-ats-success])}])

(def lock [:img.lock {:src (img/url :lock-url)}])

(defn get-full-access-bar
  []
  (let [show-full-access-bar? (rf/subscribe [:candidates/show-full-access-bar?])]
    (fn []
      (when @show-full-access-bar?
        [rc/h-box
         :class "get-full-access-bar"
         :justify :center
         :children [lock
                    [:div "For full contact information, get"]
                    [rc/hyperlink
                     :label "Full Access"
                     :on-click #(rf/dispatch [:search/click-fa])]]]))))

(defn blurred-header
  []
  [rc/box
   :class "candidate-header"
   :child [:img {:src (img/url :blurred-header-url)}]])

(defn candidate-header
  []
  (let [show-candidate-scroller? (rf/subscribe [:candidates/show-candidate-scroller?])
        blur-candidate? (rf/subscribe [:candidates/blur-active-candidate?])]
    (fn []
      [rc/v-box
       :class "candidate-header-holder"
       :children [(when @show-candidate-scroller?
                    [candidate-scroller])
                  [rc/box
                   :class "candidate-info-holder"
                   :child (if @blur-candidate?
                            [blurred-header]
                            [header])]]])))

(defn application-info
  []
  (let [job (rf/subscribe [:job/active-job])
        location (rf/subscribe [:job/active-job-location])
        apply-time (rf/subscribe [:candidates/active-candidate-apply-time])
        secure-id (rf/subscribe [:candidates/active-id])
        jobseeker-id (rf/subscribe [:candidates/active-candidate-id])
        dismissed? (rf/subscribe [:candidates/active-candidate-is-dismissed?])
        dismissing? (rf/subscribe [:candidates/dismissing?])]
    (fn []
      [rc/h-box
       :class "application-info"
       :justify :between
       :align :center
       :children [[rc/box
                   :child (str
                            "Applied on "
                            (f/unparse (f/formatter "MMMM dd, YYYY") @apply-time)
                            " for "
                            (:job-title @job)
                            " (" (:name @location) ")"
                            (when @dismissed?
                              ". You dismissed this candidate."))]
                  (when-not @dismissed?
                    [iv/submit-btn
                     :class "dismiss-candidate"
                     :label "Dismiss Candidate"
                     :submitting? @dismissing?
                     :on-click #(rf/dispatch [:candidates/dismiss-candidate @secure-id @jobseeker-id (:job-id @job) (:job_location_id @location)])])]])))

(defn application-bar
  []
  (let [show-application-bar? (rf/subscribe [:candidates/show-application-bar?])]
    (fn []
      (when @show-application-bar?
        [rc/box
         :class "application-bar"
         :child [application-info]]))))

(defn blurred-body
  "Returns blurred image when user does not have access to view candidate"
  []
  (let [page (rf/subscribe [:candidates/current-page])]
    (fn []
      [rc/box
       :class "content"
       :child [misc/overlay-holder
               :overlay [search-results/full-access-page-overlay @page]
               :anchor [:img {:src (img/url :blurred-body-url)}]]])))

(defn index
  []
  (rf/dispatch [:candidates/load-view])
  (let [page-loaded? (rf/subscribe [:candidates/page-loaded?])
        blur-candidate? (rf/subscribe [:candidates/blur-active-candidate?])
        guest? (rf/subscribe [:recruiter/guest?])
        show-resume? (rf/subscribe [:candidates/show-resume?])
        candidate-name (rf/subscribe [:candidates/active-candidate-name])]
    (fn []
      (rf/dispatch [:set-page-title (str " Ladders | " @candidate-name " Profile")])
      (if @page-loaded?
        [rc/v-box
         :class "candidate-profile main"
         :children [[application-bar]
                    [candidate-header]
                    [get-full-access-bar]
                    (when @show-resume?
                      [rc/box
                       :class "content-holder"
                       :child (if @blur-candidate?
                                [blurred-body]
                                [resume-holder])])
                    (when-not @guest?
                      [email/email-modal])
                    (when-not @guest?
                      [notes/modal])
                    [exporting-modal]
                    [export-success-modal]
                    [sr/modal]
                    (when-not @guest?
                      [sc/projects-modal])
                    (when-not @guest?
                      [ats/modal])
                    (when @guest? [ce/modal])
                    (when @guest? [ca/modal])]]
        [l/loading-page]))))
