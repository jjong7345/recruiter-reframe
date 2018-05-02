(ns recruit-app.modals.teams.views
  (:require [re-frame.core :as rf]
            [recruit-app.components.modal :as m]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.form :as form]
            [recruit-app.components.typography :as type]
            [recruit-app.components.button :as btn]
            [recruit-app.teams.db :as db]
            [recruit-app.util.dropdowns :as dd]))

(defn create-team-modal
  "Renders modal to create new team"
  []
  (let [show-errors? (rf/subscribe [:teams/show-errors?])
        email-valid? (rf/subscribe [:teams/new-team-email-valid?])
        valid? (rf/subscribe [:teams/new-team-valid?])
        creating-team? (rf/subscribe [:teams/creating-team?])]
    (fn []
      [m/modal
       :modal-key ::m/create-team
       :title "Create Team"
       :on-close #(rf/dispatch [:teams/new-team-change nil])
       :body [[layout/row-top
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/input-text
                                       :ns "teams"
                                       :type "new-team-team-name"
                                       :label "Team Name"
                                       :spec ::db/team-name
                                       :error-msg "Please enter a team name"]]]]]
              [layout/row-bottom
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/input-text
                                       :ns "teams"
                                       :type "new-team-email"
                                       :label "Team Administrator Email"
                                       :spec ::db/email
                                       :error-msg "Please enter a valid email address"]
                                      (when (and @show-errors? (not @email-valid?))
                                        [form/input-error "Could not find active recruiter with given email"])]]]]]
       :action {:label       "Save"
                :submitting? @creating-team?
                :on-click    #(rf/dispatch (if @valid?
                                             [:teams/create-team]
                                             [:teams/show-errors?-change true]))}])))

(defn edit-team-modal
  "Renders modal to edit team"
  []
  (let [valid? (rf/subscribe [:teams/edit-team-valid?])]
    (fn []
      [m/modal
       :modal-key ::m/edit-team
       :title "Rename Team"
       :on-close #(rf/dispatch [:teams/edit-team-change nil])
       :body [[layout/row
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/input-text
                                       :ns "teams"
                                       :type "edit-team-team-name"
                                       :label "Team Name"
                                       :spec ::db/team-name
                                       :error-msg "Please enter a team name"]]]]]]
       :action {:label    "Save"
                :on-click #(rf/dispatch (if @valid?
                                          [:teams/edit-team]
                                          [:teams/show-errors?-change true]))}])))

(defn remove-team-modal
  "Renders modal to confirm removing team"
  []
  [m/modal
   :modal-key ::m/remove-team
   :title "Remove Team"
   :on-close #(rf/dispatch [:teams/remove-team-change nil])
   :body [[layout/row-bottom
           :padding-top 10
           :children [[layout/column
                       :padding 20
                       :size "100%"
                       :children [[form/input-text
                                   :ns "teams"
                                   :type "remove-team-team-name"
                                   :label "Team Name"
                                   :disabled? true]]]]]
          [layout/row-bottom
           :padding-top 29
           :justify :center
           :children [[type/modal-copy "Please confirm you want to remove the above team."]]]]
   :action {:label    "Remove"
            :on-click #(rf/dispatch [:teams/remove-team])}])

(defn edit-member-modal
  "Renders modal to edit team member"
  []
  (let [role (rf/subscribe [:teams/edit-member-team-role])
        edit-member (rf/subscribe [:teams/edit-member])
        recruiter-id (rf/subscribe [:teams/edit-member-recruiter_id])]
    (fn []
      [m/modal
       :modal-key ::m/edit-team-member
       :title "Edit Team Member"
       :on-close #(rf/dispatch [:teams/edit-member-change nil])
       :body [[layout/row-bottom
               :padding-top 10
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/input-text
                                       :ns "teams"
                                       :type "edit-member-email"
                                       :label "Email"
                                       :disabled? true]]]]]
              [layout/row-bottom
               :padding-top 12
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/single-dropdown
                                       :ns "teams"
                                       :type "edit-member-team-role"
                                       :label "Role"
                                       :choices dd/team-roles]]]]]]
       :action {:label    "Save"
                :on-click #(rf/dispatch (if (= @role "Staff")
                                          [:teams/remove-admin @recruiter-id]
                                          [:teams/make-admin @edit-member]))}])))

(defn confirm-remove-modal
  "Renders modal to confirm removing team member"
  []
  (let [team-role (rf/subscribe [:teams/remove-member-team-role])]
    (fn []
      [m/modal
       :modal-key ::m/remove-team-member
       :title "Remove Team Member"
       :on-close #(rf/dispatch [:teams/remove-member-change nil])
       :body [[layout/row-bottom
               :padding-top 10
               :children [[layout/column
                           :padding 20
                           :size "100%"
                           :children [[form/input-text
                                       :ns "teams"
                                       :type "remove-member-email"
                                       :label "Email"
                                       :disabled? true]]]]]
              [layout/row-bottom
               :padding-top 29
               :justify :center
               :children [[type/modal-copy "Please confirm you want to remove the above recruiter."]]]]
       :action {:label    "Remove"
                :on-click #(rf/dispatch [:teams/remove-member])}])))
