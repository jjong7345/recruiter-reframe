(ns recruit-app.teams.subs
  (:require [recruit-app.util.subscription :as subs]
            [re-frame.core :as rf]
            [recruit-app.teams.db :as db]
            [cljs.spec.alpha :as s]))

(subs/reg-subs "teams" [["teams" []] ["active-team-id" nil] ["search-term" ""]
                        ["new-member" {}] ["remove-member" nil] ["new-team" {}]
                        ["edit-member" nil] ["edit-team" nil]
                        ["remove-team" nil] ["search-results" nil]
                        ["show-errors?" false] ["new-team-email-valid?" true]
                        ["creating-team?" false]])
(subs/reg-nested-subs "teams" "active-team" [["team-id" nil] ["team-name" nil]])
(subs/reg-nested-subs "teams" "new-member" [["email" ""] ["team-role" "Staff"]])
(subs/reg-nested-subs "teams" "remove-member" [["email" ""] ["team-role" "Staff"]])
(subs/reg-nested-subs "teams" "edit-member" [["email" ""] ["team-role" "Staff"]
                                             ["recruiter_id" nil]])
(subs/reg-nested-subs "teams" "new-team" [["team-name" ""] ["email" ""]])
(subs/reg-nested-subs "teams" "edit-team" [["team-name" ""]])
(subs/reg-nested-subs "teams" "remove-team" [["team-name" ""]])

(defn teams-list
  "Returns vector of teams given teams map"
  [[teams-map search-results] _]
  (if search-results
    search-results
    (sort-by :time > (vals teams-map))))

(defn active-team
  "Returns team given map of teams and active team id"
  [[teams team-id] _]
  (get teams team-id))

(defn- with-team-role
  "Assocs team role to given member"
  [team-role member]
  (assoc member :team-role team-role))

(defn team-members
  "Returns formatted vector of team members given team"
  [{:keys [admins members]} _]
  (concat
    (map (partial with-team-role "Administrator") admins)
    (map (partial with-team-role "Staff") members)))

(defn team-valid?
  "Validates new team map against spec"
  [team _]
  (s/valid? ::db/team team))

(rf/reg-sub
  :teams/teams-list
  :<- [:teams/teams]
  :<- [:teams/search-results]
  teams-list)

(rf/reg-sub
  :teams/active-team
  :<- [:teams/teams]
  :<- [:teams/active-team-id]
  active-team)

(rf/reg-sub
  :teams/active-team-members
  :<- [:teams/active-team]
  team-members)

(rf/reg-sub
  :teams/new-team-valid?
  :<- [:teams/new-team]
  team-valid?)

(rf/reg-sub
  :teams/edit-team-valid?
  :<- [:teams/edit-team]
  team-valid?)
