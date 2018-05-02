(ns recruit-app.teams.views
  (:require [recruit-app.components.security :as sec]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.table :as table]
            [recruit-app.components.form :as form]
            [recruit-app.components.button :as btn]
            [recruit-app.components.icon :as icon]
            [re-frame.core :as rf]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.components.loading :as loading]
            [recruit-app.modals.teams.views :as modals]
            [recruit-app.util.uri :as u]))

(defn- breadcrumb
  [& {:keys [label on-click]}]
  [layout/row-bottom
   :padding-top 8
   :children [[link/breadcrumb-hyperlink
               :label label
               :on-click on-click]]])

(defn member-edit-actions
  [member]
  [layout/row
   :padding 0
   :children [[layout/col-left
               :padding-right 5
               :children [[icon/pencil
                           :on-click #(rf/dispatch [:teams/edit-member-click member])]]]
              [layout/col-right
               :padding-left 5
               :children [[icon/x
                           :on-click #(rf/dispatch [:teams/remove-member-click member])]]]]])

(defn team-edit-actions
  [team]
  [layout/row
   :padding 0
   :children [[layout/col-left
               :padding-right 5
               :children [[icon/pencil
                           :on-click #(rf/dispatch [:teams/edit-team-click team])]]]
              [layout/col-right
               :padding-left 5
               :children [[icon/x
                           :on-click #(rf/dispatch [:teams/remove-team-click team])]]]]])

(defn team-edit-header
  []
  (let [team-name (rf/subscribe [:teams/active-team-team-name])
        team-id (rf/subscribe [:teams/active-team-team-id])]
    (fn []
      [layout/row
       :padding-top 12
       :padding-bottom 15
       :align :baseline
       :children [[layout/col-left
                   :padding-right 3
                   :children [[header/header-3 @team-name]]]
                  [layout/col-right
                   :padding-left 3
                   :children [[header/sub-header (str "(ID " @team-id ")")]]]]])))

(defn team-member-row-data
  "Returns table data for given member"
  [{:keys [email team-role] :as member}]
  [email team-role [member-edit-actions member]])

(defn team-member-table
  []
  (let [members (rf/subscribe [:teams/active-team-members])]
    (fn []
      [layout/row
       :padding 0
       :children [[table/table
                   :headers [[table/header-cell
                              :label "Email"]
                             [table/header-cell
                              :label "Role"]
                             [table/header-cell
                              :label "Edit"]]
                   :row-data (mapv team-member-row-data @members)]]])))

(defn add-members-form
  []
  (let [team-role (rf/subscribe [:teams/new-member-team-role])]
    (fn []
      [layout/column
       :padding 0
       :children [[layout/row
                   :padding-top 18
                   :padding-bottom 15
                   :children [[header/section-header "Add Members"]]]
                  [layout/row
                   :padding 0
                   :align :end
                   :children [[layout/column
                               :padding 0
                               :class "col-xs-4"
                               :children [[form/input-text
                                           :ns "teams"
                                           :type "new-member-email"
                                           :label "Email"]]]
                              [layout/column
                               :padding 23
                               :class "col-xs-3"
                               :children [[form/single-dropdown
                                           :ns "teams"
                                           :type "new-member-team-role"
                                           :choices dd/team-roles]]]
                              [layout/column
                               :padding 0
                               :class "col-xs-3"
                               :children [[btn/primary-dashboard-button
                                           :label "Add"
                                           :on-click #(rf/dispatch (if (= "Staff" @team-role)
                                                                     [:teams/add-member]
                                                                     [:teams/add-admin-by-email]))]]]]]]])))

(defn team-edit-panel
  []
  (rf/dispatch [:teams/load-team-view])
  (let [active-team (rf/subscribe [:teams/active-team])]
    (fn []
      (if @active-team
        [layout/column
         :padding 0
         :children [[breadcrumb
                     :label "< Back to All Teams"
                     :on-click #(rf/dispatch [:go-to-route "/teams"])]
                    [team-edit-header]
                    [team-member-table]
                    [add-members-form]]]
        [loading/loading-page]))))

(defn team-row-data
  [_ {:keys [team-name team-id] :as team}]
  [[link/hyperlink-href
    :label team-name
    :href (str "/#/teams/" team-id)]
   team-id
   [team-edit-actions team]])

(defn teams-table-header
  []
  [layout/row-bottom
   :padding-top 4
   :justify :between
   :align :end
   :children [[layout/column
               :padding 0
               :class "col-xs-6"
               :children [[layout/row-top
                           :padding-bottom 5
                           :children [[header/header-3 "All Teams"]]]]]
              [layout/column
               :padding 0
               :class "col-xs-6"
               :children [[layout/row
                           :padding 0
                           :justify :end
                           :align :end
                           :children [[layout/column
                                       :class "col-xs-8"
                                       :children [[form/input-text
                                                   :ns "teams"
                                                   :type "search-term"
                                                   :label "Team Name or Team ID"]]]
                                      [layout/col-right
                                       :children [[btn/primary-dashboard-button
                                                   :label "Search"
                                                   :on-click #(rf/dispatch [:teams/search])]]]]]]]]])

(def table-headers
  [{:label "Name"
    :width 39.9}
   {:label "Team ID"
    :width 53.7}
   {:label "Edit"
    :width 6.4}])

(defn teams-table
  []
  [layout/row
   :children [[table/backend-pagination-table
               :table-key ::table/teams
               :fetch-url (u/uri :fetch-teams)
               :headers table-headers
               :row-data-fn team-row-data]]])

(defn teams-panel
  []
  (rf/dispatch [:teams/load-view])
  [layout/column
   :padding 0
   :children [[breadcrumb
               :label "< Back to Main"
               :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])]
              [teams-table-header]
              [teams-table]
              [modals/edit-team-modal]
              [modals/remove-team-modal]]])

(defn- index
  [body]
  [sec/superuser-content
   [layout/column
    :padding 0
    :class "recruiter-admin main"
    :children [[header/page-header
                :header-text "Recruiter Teams"]
               [layout/page-content body]
               [modals/confirm-remove-modal]
               [modals/edit-member-modal]]]])

(defn teams-view
  "Displays content with teams-view panel"
  []
  [index [teams-panel]])

(defn team-edit-view
  "Displays content with team-edit-view-panel"
  []
  [index [team-edit-panel]])
