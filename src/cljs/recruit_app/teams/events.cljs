(ns recruit-app.teams.events
  (:require [re-frame.core :as rf]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [recruit-app.util.ajax :as a]
            [recruit-app.util.events :as ev]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.table :as table]))

(ev/reg-events "teams" ["teams" "active-team-id" "search-term"
                        "remove-member" "edit-member" "edit-team" "remove-team"
                        "search-results" "show-errors?" "new-team-email-valid?"
                        "creating-team?" "new-team"])
(ev/reg-events "teams" "new-member" ["email" "team-role"])
(ev/reg-events "teams" "new-team" ["team-name"])
(ev/reg-events "teams" "edit-team" ["team-name"])
(ev/reg-events "teams" "edit-member" ["team-role"])

(defn load-view
  "Logs page view and loads teams"
  [_ _]
  {:ga/page-view ["/#/teams" {}]})

(defn load-team-view
  "Logs page view and loads all team information"
  [{:keys [db]} _]
  (when-let [team-id (-> db :teams :active-team-id)]
    {:ga/page-view [(str "/#/teams/" team-id) {}]
     :dispatch     [:teams/fetch-team team-id]}))

(defn clear-db
  "Dissociates teams from db"
  [db]
  (dissoc db :teams))

(defn fetch-team
  "Calls API to fetch team by id"
  [_ [_ team-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :fetch-team team-id)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:teams/fetch-team-success team-id]
                   :on-failure      [:teams/fetch-team-failure]}})

(defn fetch-team-success
  "Assocs team into teams map"
  [db [_ team-id team]]
  (assoc-in db [:teams :teams team-id] team))

(defn fetch-team-failure
  "Displays alert of failure to user"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Fetch Team"]})

(defn add-member
  "Calls API to add member to team by email"
  [{:keys [db]} _]
  (let [team-id (-> db :teams :active-team-id)
        email (-> db :teams :new-member :email)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :add-member team-id)
                     :params          {:email email}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/add-member-success team-id]
                     :on-failure      [:teams/add-member-failure]}}))

(defn add-member-success
  "Dispatches event to fetch team by ID"
  [_ [_ team-id]]
  {:dispatch-n [[:teams/fetch-team team-id]
                [:teams/clear-new-member-form]
                [:alerts/add-success "Successfully Added Member To Team!"]]})

(defn add-member-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch [:alerts/add-error (or (:error response) "Failed To Add Member")]})

(defn make-admin
  "Calls API to make existing member an admin"
  [{:keys [db]} [_ {:keys [recruiter_id]}]]
  (let [team-id (-> db :teams :active-team-id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :make-admin team-id)
                     :params          {:member-id recruiter_id}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/make-admin-success team-id]
                     :on-failure      [:teams/make-admin-failure]}}))

(defn make-admin-success
  "Dispatches event to fetch team by ID"
  [_ [_ team-id]]
  {:dispatch-n [[:teams/fetch-team team-id]
                [::modal/close-modal ::modal/edit-team-member]
                [:teams/edit-member-change nil]
                [:alerts/add-success "Successfully Added Admin To Team!"]]})

(defn make-admin-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch-n [[::modal/close-modal ::modal/edit-team-member]
                [:teams/edit-member-change nil]
                [:alerts/add-error (or (:error response) "Failed To Make Member An Admin")]]})

(defn add-admin-by-email
  "Calls API to add admin to team by email"
  [{:keys [db]} _]
  (let [team-id (-> db :teams :active-team-id)
        email (-> db :teams :new-member :email)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :add-admin-by-email team-id)
                     :params          {:email email}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/add-admin-success team-id]
                     :on-failure      [:teams/add-admin-failure]}}))

(defn add-admin-success
  "Dispatches event to fetch team by ID"
  [_ [_ team-id]]
  {:dispatch-n [[:teams/fetch-team team-id]
                [:teams/clear-new-member-form]
                [:alerts/add-success "Successfully Added Admin To Team!"]]})

(defn add-admin-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch [:alerts/add-error (or (:error response) "Failed To Add Admin")]})

(defn clear-new-member-form
  "Dissociates :new-member from teams db"
  [db _]
  (update db :teams dissoc :new-member))

(defn remove-member
  "Calls API to add member to team by email"
  [{:keys [db]} _]
  (let [team-id (-> db :teams :active-team-id)
        recruiter-id (-> db :teams :remove-member :recruiter_id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :remove-member team-id)
                     :params          {:member-id recruiter-id}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/remove-member-success team-id]
                     :on-failure      [:teams/remove-member-failure]}}))

(defn remove-member-success
  "Dispatches event to fetch team by ID"
  [_ [_ team-id]]
  {:dispatch-n [[:teams/fetch-team team-id]
                [::modal/close-modal ::modal/remove-team-member]
                [:teams/remove-member-change nil]
                [:alerts/add-success "Successfully Removed Admin From Team!"]]})

(defn remove-member-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch-n [[::modal/close-modal ::modal/remove-team-member]
                [:teams/remove-member-change nil]
                [:alerts/add-error (or (:error response) "Failed To Remove Member")]]})

(defn remove-admin
  "Calls API to add admin to team by email"
  [{:keys [db]} [_ recruiter-id]]
  (let [team-id (-> db :teams :active-team-id)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :remove-admin team-id)
                     :params          {:member-id recruiter-id}
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/remove-admin-success team-id]
                     :on-failure      [:teams/remove-admin-failure]}}))

(defn remove-admin-success
  "Dispatches event to fetch team by ID"
  [_ [_ team-id]]
  {:dispatch-n [[:teams/fetch-team team-id]
                [::modal/close-modal ::modal/edit-team-member]
                [:teams/edit-member-change nil]
                [:alerts/add-success "Successfully Removed Admin From Team!"]]})

(defn remove-admin-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch-n [[::modal/close-modal ::modal/edit-team-member]
                [:teams/edit-member-change nil]
                [:alerts/add-error (or (:error response) "Failed To Remove Admin")]]})

(defn create-team
  "Calls API to add admin to team by email"
  [{:keys [db]} _]
  {:dispatch      [:teams/creating-team?-change true]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :create-team)
                   :params          (-> db :teams :new-team)
                   :format          (ajax/json-request-format)
                   :response-format (a/json-response-format {:keywords? true})
                   :on-success      [:teams/create-team-success]
                   :on-failure      [:teams/create-team-failure]}})

(defn create-team-success
  "Dispatches event to fetch team by ID"
  [_ [_ {:keys [team-id]}]]
  {:dispatch-n [[:teams/close-create-team-modal]
                [:teams/creating-team?-change false]
                [:alerts/add-success "Successfully Created Team!"]]
   :route      (str "/teams/" team-id)})

(defn create-team-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response status]}]]
  (if (= status 404)
    {:dispatch-n [[:teams/show-errors?-change true]
                  [:teams/creating-team?-change false]
                  [:teams/new-team-email-valid?-change false]]}
    {:dispatch-n [[:teams/close-create-team-modal]
                  [:teams/creating-team?-change false]
                  [:alerts/add-error (or (:error response) "Failed To Create Team")]]}))

(defn edit-team
  "Calls API to add admin to team by email"
  [{:keys [db]} _]
  (let [{:keys [team-id] :as params} (-> db :teams :edit-team)]
    {:ra-http-xhrio {:method          :put
                     :uri             (u/uri :edit-team team-id)
                     :params          params
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/edit-team-success]
                     :on-failure      [:teams/edit-team-failure]}}))

(defn edit-team-success
  "Dispatches event to fetch team by ID

  Note: For some reason the ES index is slightly delayed and the dispatch-later
  was required to ensure the team was removed."
  [_ [_ {:keys [team-id]}]]
  {:dispatch-n     [[:teams/edit-team-change nil]
                    [::modal/close-modal ::modal/edit-team]
                    [:alerts/add-success "Successfully Renamed Team!"]]
   :dispatch-later [{:ms 499 :dispatch [(table/reset-event ::table/teams)]}
                    {:ms 500 :dispatch [(table/pagination-event ::table/teams) 0]}]
   :route          (str "/teams/" team-id)})

(defn edit-team-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch-n [[:teams/edit-team-change nil]
                [::modal/close-modal ::modal/edit-team]
                [:alerts/add-error (or (:error response) "Failed To Rename Team")]]})

(defn remove-team
  "Calls API to add admin to team by email"
  [{:keys [db]} _]
  (let [{:keys [team-id] :as params} (-> db :teams :remove-team)]
    {:ra-http-xhrio {:method          :delete
                     :uri             (u/uri :remove-team team-id)
                     :params          params
                     :format          (ajax/json-request-format)
                     :response-format (a/json-response-format {:keywords? true})
                     :on-success      [:teams/remove-team-success]
                     :on-failure      [:teams/remove-team-failure]}}))

(defn remove-team-success
  "Clears remove team modal and resets table

  Note: For some reason the ES index is slightly delayed and the dispatch-later
  was required to ensure the team was removed."
  [{:keys [db]} [_ {:keys [team-id]}]]
  (let [search-results-count (-> db :teams :search-results count)]
    {:dispatch-n     (cond-> [[::modal/close-modal ::modal/remove-team]
                              [:teams/remove-team-change nil]
                              [:alerts/add-success "Successfully Deleted Team!"]]
                             (= search-results-count 1) (conj [:teams/clear-search]))
     :dispatch-later (cond-> [{:ms 999 :dispatch [(table/reset-event ::table/teams)]}
                              {:ms 1000 :dispatch [(table/pagination-event ::table/teams) 0]}]
                             (> search-results-count 1) (conj {:ms 1000 :dispatch [:teams/search]}))}))

(defn remove-team-failure
  "Shows alert message based on response"
  [_ [_ {:keys [response]}]]
  {:dispatch-n [[::modal/close-modal ::modal/remove-team]
                [:teams/remove-team-change nil]
                [:alerts/add-error (or (:error response) "Failed To Delete Team")]]})

(defn close-create-team-modal
  "Clears form and closes modal"
  [{:keys [db]} _]
  {:db       (update db :teams dissoc :new-team)
   :dispatch [::modal/close-modal ::modal/create-team]})

(defn search
  "Run search by name/id"
  [{:keys [db]}]
  (let [search-term (-> db :teams :search-term)]
    (if (seq search-term)
      {:ra-http-xhrio {:method          :post
                       :uri             (u/uri :team-search)
                       :params          {:query search-term}
                       :format          (ajax/json-request-format)
                       :response-format (a/json-response-format {:keywords? true})
                       :on-success      [:teams/search-success]
                       :on-failure      [:teams/search-failure]}}
      {:dispatch [:teams/search-results-change nil]})))

(defn search-success
  "Either route to given team or display results"
  [_ [_ response]]
  (cond
    (map? response) {:dispatch [:teams/search-results-change [response]]}
    (> (count response) 0) {:dispatch [:teams/search-results-change response]}
    :else {:dispatch [:teams/search-failure]}))

(defn search-failure
  "Display failure message to user"
  [_ _]
  {:dispatch [:alerts/add-error "Could not find team by given name or ID"]})

(defn new-team-email-change
  "Associates value in db and clears errors"
  [{:keys [db]} [_ val]]
  {:db       (assoc-in db [:teams :new-team :email] val)
   :dispatch [:teams/new-team-email-valid?-change true]})

(defn clear-search
  "Clears search term and search results from db"
  []
  {:dispatch-n [[:teams/search-term-change ""]
                [:teams/search-results-change nil]]})

(defn edit-member-click
  "Sets member as editing member and opens modal"
  [_ [_ member]]
  {:dispatch-n [[:teams/edit-member-change member]
                [::modal/open-modal ::modal/edit-team-member]]})

(defn remove-member-click
  "Sets member as remove member and opens modal"
  [_ [_ member]]
  {:dispatch-n [[:teams/remove-member-change member]
                [::modal/open-modal ::modal/remove-team-member]]})

(defn edit-team-click
  "Sets team as editing team and opens modal"
  [_ [_ team]]
  {:dispatch-n [[:teams/edit-team-change team]
                [::modal/open-modal ::modal/edit-team]]})

(defn remove-team-click
  "Sets team as editing team and opens modal"
  [_ [_ team]]
  {:dispatch-n [[:teams/remove-team-change team]
                [::modal/open-modal ::modal/remove-team]]})

(rf/reg-event-fx
  :teams/load-view
  load-view)

(rf/reg-event-fx
  :teams/load-team-view
  load-team-view)

(rf/reg-event-db
  :teams/clear-db
  clear-db)

(rf/reg-event-fx
  :teams/fetch-team
  fetch-team)

(rf/reg-event-db
  :teams/fetch-team-success
  fetch-team-success)

(rf/reg-event-fx
  :teams/fetch-team-failure
  fetch-team-failure)

(rf/reg-event-fx
  :teams/add-member
  add-member)

(rf/reg-event-fx
  :teams/add-member-success
  add-member-success)

(rf/reg-event-fx
  :teams/add-member-failure
  add-member-failure)

(rf/reg-event-fx
  :teams/make-admin
  make-admin)

(rf/reg-event-fx
  :teams/make-admin-success
  make-admin-success)

(rf/reg-event-fx
  :teams/make-admin-failure
  make-admin-failure)

(rf/reg-event-fx
  :teams/add-admin-by-email
  add-admin-by-email)

(rf/reg-event-fx
  :teams/add-admin-success
  add-admin-success)

(rf/reg-event-fx
  :teams/add-admin-failure
  add-admin-failure)

(rf/reg-event-db
  :teams/clear-new-member-form
  clear-new-member-form)

(rf/reg-event-fx
  :teams/remove-admin
  remove-admin)

(rf/reg-event-fx
  :teams/remove-admin-success
  remove-admin-success)

(rf/reg-event-fx
  :teams/remove-admin-failure
  remove-admin-failure)

(rf/reg-event-fx
  :teams/remove-member
  remove-member)

(rf/reg-event-fx
  :teams/remove-member-success
  remove-member-success)

(rf/reg-event-fx
  :teams/remove-member-failure
  remove-member-failure)

(rf/reg-event-fx
  :teams/create-team
  create-team)

(rf/reg-event-fx
  :teams/create-team-success
  create-team-success)

(rf/reg-event-fx
  :teams/create-team-failure
  create-team-failure)

(rf/reg-event-fx
  :teams/edit-team
  edit-team)

(rf/reg-event-fx
  :teams/edit-team-success
  edit-team-success)

(rf/reg-event-fx
  :teams/edit-team-failure
  edit-team-failure)

(rf/reg-event-fx
  :teams/close-create-team-modal
  close-create-team-modal)

(rf/reg-event-fx
  :teams/remove-team
  remove-team)

(rf/reg-event-fx
  :teams/remove-team-success
  remove-team-success)

(rf/reg-event-fx
  :teams/remove-team-failure
  remove-team-failure)

(rf/reg-event-fx
  :teams/search
  search)

(rf/reg-event-fx
  :teams/search-success
  search-success)

(rf/reg-event-fx
  :teams/search-failure
  search-failure)

(rf/reg-event-fx
  :teams/new-team-email-change
  new-team-email-change)

(rf/reg-event-fx
  :teams/clear-search
  clear-search)

(rf/reg-event-fx
  :teams/edit-member-click
  edit-member-click)

(rf/reg-event-fx
  :teams/remove-member-click
  remove-member-click)

(rf/reg-event-fx
  :teams/edit-team-click
  edit-team-click)

(rf/reg-event-fx
  :teams/remove-team-click
  remove-team-click)
