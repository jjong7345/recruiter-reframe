(ns recruit-app.projects.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [recruit-app.util.events :as ev]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.components.modal :as modal]
            [recruit-app.util.projects :as p]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [recruit-app.util.job :as ju]))

(ev/reg-events "projects" ["new-title" "curr-project-id"
                           "active-candidate-id" "page-loaded?"
                           "show-errors?" "editing-project"])
(ev/reg-events "projects" "editing-project" ["title"])
(ev/reg-toggle-event "projects" "show-create-project-input?")

(defn load-view
  "Retrieves projects and candidates data"
  [_ _]
  {:dispatch     [:projects/get-projects-data]
   :ga/page-view ["/projects" {}]})

(defn add-project
  "Returns http-xhrio request to add project"
  [_ [_ new-title]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :create-project)
                   :params          {:folderName new-title}
                   :timeout         5000
                   :format          (ajax/url-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:projects/add-project-success new-title]
                   :on-failure      [:projects/add-project-failure]}})

(defn add-project-success
  "Add project to db and dispatch alert"
  [{:keys [db]} [_ new-title response]]
  {:db       (update-in db [:projects :projects-map] p/with-new-project new-title response)
   :dispatch [:alerts/add-success (str new-title " Successfully Created.")]})

(defn add-project-failure
  "Dispatches alert to notify user of error"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Add Project."]})

(defn on-add-project
  "Clears new-title input and dispatches event to add project"
  [{:keys [db]} _]
  {:dispatch-n [[:projects/add-project (get-in db [:projects :new-title])]
                [:projects/toggle-show-create-project-input?]]
   :db         (assoc-in db [:projects :new-title] "")})

(defn handle-projects-data
  "Converts response from server to clojure seqs and adds to db"
  [{:keys [db]} [_ response]]
  {:dispatch [:projects/page-loaded?-change true]
   :db       (assoc-in db [:projects :projects-map] (p/projects response))})

(defn handle-candidates-data
  "Converts response from server to clojure seqs and adds to db"
  [{:keys [db]} [_ candidates]]
  {:dispatch [:project-list/page-loaded?-change true]
   :db       (assoc-in db [:projects :candidates-map] (zipmap (map :secureId (map :subscriber candidates)) candidates))})

(defn get-projects-failure
  "Dispatch error alert to notify user"
  [_ _]
  {:dispatch-n [[:projects/page-loaded?-change true]]})

(defn get-projects-data
  "Returns http-xhrio request to get projects data"
  [{:keys [db]} _]
  (when-not (-> db :projects :projects-map)
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :get-projects)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:projects/handle-projects-data]
                     :on-failure      [:projects/get-projects-failure]}}))

(defn get-candidates-failure
  "Dispatch error alert to notify user"
  [_ _]
  {:dispatch-n [[:project-list/page-loaded?-change true]
                [:alerts/add-error "Failed To Load Candidates For Project."]]})

(defn get-candidates-data
  "Returns http-xhrio request to get candidates data"
  [{:keys [db]} [_ project-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :get-candidates project-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:projects/handle-candidates-data]
                   :on-failure      [:projects/get-candidates-failure]}})

(defn open-save-candidate-modal
  "Dispatches events to set correct modal, set candidate id for modal and open"
  [{:keys [db]} [_ candidate-id]]
  {:dispatch-n [[:projects/fetch-projects-for-candidate candidate-id]
                [:projects/active-candidate-id-change candidate-id]
                [::modal/open-modal ::modal/save-candidate]]})

(defn edit-project-success
  "Updates project in db and dispatches success alert"
  [{:keys [db]} [_ new-title project-id]]
  {:db         (assoc-in db [:projects :projects-map project-id :title] new-title)
   :dispatch-n [[:projects/close-modal]
                [:alerts/add-success (str (-> db :projects :projects-map (get project-id) :title) " Successfully Renamed")]]})

(defn edit-project-failure
  "Dispatches error alert to notify user of failure"
  [_ _]
  {:dispatch-n [[:projects/close-modal]
                [:alerts/add-error "Failed To Update Project."]]})

(defn edit-project
  "Returns http-xhrio request to edit given project"
  [{:keys [db]}]
  (let [{:keys [projectId title]} (-> db :projects :editing-project)]
    {:ra-http-xhrio {:method          :put
                     :uri             (u/uri :rename-project projectId)
                     :params          {:name title}
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:projects/edit-project-success title projectId]
                     :on-failure      [:projects/edit-project-failure]}}))

(defn delete-project-success
  "Dispatches success alert and removes project from db"
  [{:keys [db]} [_ project-id]]
  {:db         (update-in db [:projects :projects-map] dissoc project-id)
   :dispatch-n [[:scroll-top]
                [::modal/close-modal ::modal/delete-project]
                [:projects/editing-project-change nil]
                [:alerts/add-success (str (-> db :projects :projects-map (get project-id) :title) " Successfully Deleted")]]})

(defn delete-project-failure
  "Dispatches event to show error to user"
  [_ _]
  {:dispatch-n [[:scroll-top]
                [::modal/close-modal ::modal/delete-project]
                [:projects/editing-project-change nil]
                [:alerts/add-error "Failed To Delete Project."]]})

(defn delete-project
  "Returns http-xhrio request to delete give project"
  [{:keys [db]}]
  (let [projectId (-> db :projects :editing-project :projectId)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :delete-project projectId)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:projects/delete-project-success projectId]
                     :on-failure      [:projects/delete-project-failure]}}))

(defn add-candidate-failure
  "Dispatches event to close modal and add error alert"
  [_ [_ project-id]]
  {:dispatch-n [[::modal/close-modal ::modal/save-candidate]
                [:projects/remove-from-disabled-projects project-id]
                [:alerts/add-error "Failed To Add Candidate To Project."]]})

(defn add-candidate-to-project
  "Returns http-xhrio request to add candidate to project"
  [{:keys [db]} [_ secure-id project-id]]
  {:dispatch      [:projects/add-to-disabled-projects project-id]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :update-project-candidates (-> db :recruiter :recruiter-id) secure-id project-id)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:projects/add-candidate-success secure-id project-id]
                   :on-failure      [:projects/add-candidate-failure project-id]}})

(defn increment-candidate-count
  "Increments candidate count for given project-id"
  [db [_ project-id]]
  (update-in db [:projects :projects-map project-id :candidate_count] inc))

(defn decrement-candidate-count
  "Increments candidate count for given project-id"
  [db [_ project-id]]
  (update-in db [:projects :projects-map project-id :candidate_count] dec))

(defn add-candidate-project
  "Removes project from list of a candidate's projects"
  [db [_ secure-id project-id]]
  (update-in db [:projects :candidate-projects secure-id] (fnil conj #{}) project-id))

(defn add-candidate-success
  "Dispatches events to increment candidate count and add project for candidate"
  [_ [_ secure-id project-id]]
  {:dispatch-n [[:projects/increment-candidate-count project-id]
                [:projects/add-candidate-project secure-id project-id]
                [:projects/remove-from-disabled-projects project-id]]})

(defn remove-candidate-project
  "Removes project from list of a candidate's projects"
  [db [_ secure-id project-id]]
  (update-in db [:projects :candidate-projects secure-id] disj project-id))

(defn remove-candidate-from-list
  "Removes candidate from project"
  [{:keys [db]} [_ candidate-id]]
  {:db (update-in db [:projects :candidates-map] dissoc candidate-id)})

(defn remove-candidate-success
  "Dispatches events to decrement candidate count and remove candidate from project"
  [_ [_ candidate-id project-id]]
  {:dispatch-n [[:projects/decrement-candidate-count project-id]
                [:projects/remove-candidate-project candidate-id project-id]
                [:projects/remove-candidate-from-list candidate-id]
                [:projects/remove-from-disabled-projects project-id]]})

(defn remove-candidate-failure
  "Dispatches event to close modal and add error alert"
  [_ [_ project-id]]
  {:dispatch-n [[::modal/close-modal ::modal/save-candidate]
                [:projects/remove-from-disabled-projects project-id]
                [:alerts/add-error "Failed To Remove Candidate From Project."]]})

(defn remove-candidate-from-project
  "Returns http-xhrio request to remove candidate from project"
  [{:keys [db]} [_ candidate-id project-id]]
  {:dispatch      [:projects/add-to-disabled-projects project-id]
   :ra-http-xhrio {:method          :delete
                   :uri             (u/uri :update-project-candidates (-> db :recruiter :recruiter-id) candidate-id project-id)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:projects/remove-candidate-success candidate-id project-id]
                   :on-failure      [:projects/remove-candidate-failure project-id]}})

(defn projects-for-candidate-success
  "Saves projects to db"
  [db [_ secure-id response]]
  (assoc-in db [:projects :candidate-projects] (p/candidate-projects response)))

(defn projects-for-candidate-failure
  "Saves projects to db"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Retrieve Projects For Candidate."]})

(defn fetch-projects-for-candidate
  "Returns http-xhrio request to fetch all projects for candidate"
  [{:keys [db]} [_ secure-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :projects-for-candidate (-> db :recruiter :recruiter-id) secure-id)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format)
                   :on-success      [:projects/projects-for-candidate-success secure-id]
                   :on-failure      [:projects/projects-for-candidate-failure]}})

(defn click-project
  "Route to project page for clicked project"
  [_ [_ project-id]]
  {:route (str "/projects/" project-id)})

(defn click-candidate
  "Route to candidate profile page"
  [{:keys [db]} [_ idx secure-id]]
  {:route (p/profile-route (:projects db) idx secure-id)})

(defn add-to-disabled-projects
  "When adding/removing candidate to project, we disabled it temporarily.
  This adds to a set of disabled projects that a user cannot edit"
  [db [_ project-id]]
  (update-in db [:projects :disabled-projects] ju/add-to-set project-id))

(defn remove-from-disabled-projects
  "Removes project-id from disabled projects set"
  [db [_ project-id]]
  (update-in db [:projects :disabled-projects] disj project-id))

(defn open-edit-modal
  "Sets project id and opens edit modal"
  [_ [_ project]]
  {:dispatch-n [[:projects/editing-project-change project]
                [::modal/open-modal ::modal/rename-project]]})

(defn open-delete-modal
  "Sets project id and opens delete modal"
  [_ [_ project]]
  {:dispatch-n [[:projects/editing-project-change project]
                [::modal/open-modal ::modal/delete-project]]})

(defn clear-project-form
  "Clears show input toggle, new title, and show errors toggle from db"
  [db]
  (update db :projects dissoc :show-create-project-input? :new-title :show-errors?))

(defn close-modal
  "Clears editing project and toggles show-modal?"
  []
  {:dispatch-n [[::modal/close-modal ::modal/rename-project]
                [:projects/show-errors?-change false]
                [:projects/editing-project-change nil]]})

(rf/reg-event-fx
  :projects/load-view
  load-view)

(rf/reg-event-fx
  :projects/add-project-success
  add-project-success)

(rf/reg-event-fx
  :projects/add-project-failure
  add-project-failure)

(rf/reg-event-fx
  :projects/add-project
  add-project)

(rf/reg-event-fx
  :projects/on-add-project
  on-add-project)

(rf/reg-event-fx
  :projects/handle-projects-data
  handle-projects-data)

(rf/reg-event-fx
  :projects/handle-candidates-data
  handle-candidates-data)

(rf/reg-event-fx
  :projects/get-projects-failure
  get-projects-failure)

(rf/reg-event-fx
  :projects/get-projects-data
  get-projects-data)

(rf/reg-event-fx
  :projects/get-candidates-data
  get-candidates-data)

(rf/reg-event-fx
  :projects/get-candidates-failure
  get-candidates-failure)

(rf/reg-event-fx
  :projects/edit-project-success
  edit-project-success)

(rf/reg-event-fx
  :projects/edit-project-failure
  edit-project-failure)

(rf/reg-event-fx
  :projects/edit-project
  edit-project)

(rf/reg-event-fx
  :projects/delete-project-success
  delete-project-success)

(rf/reg-event-fx
  :projects/delete-project-failure
  delete-project-failure)

(rf/reg-event-fx
  :projects/delete-project
  delete-project)

(rf/reg-event-fx
  :projects/add-candidate-to-project
  add-candidate-to-project)

(rf/reg-event-fx
  :projects/add-candidate-success
  add-candidate-success)

(rf/reg-event-fx
  :projects/add-candidate-failure
  add-candidate-failure)

(rf/reg-event-db
  :projects/add-candidate-project
  add-candidate-project)

(rf/reg-event-db
  :projects/increment-candidate-count
  increment-candidate-count)

(rf/reg-event-db
  :projects/decrement-candidate-count
  decrement-candidate-count)

(rf/reg-event-fx
  :projects/open-save-candidate-modal
  open-save-candidate-modal)

(rf/reg-event-fx
  :projects/remove-candidate-from-project
  remove-candidate-from-project)

(rf/reg-event-fx
  :projects/remove-candidate-success
  remove-candidate-success)

(rf/reg-event-fx
  :projects/remove-candidate-failure
  remove-candidate-failure)

(rf/reg-event-db
  :projects/remove-candidate-project
  remove-candidate-project)

(rf/reg-event-fx
  :projects/fetch-projects-for-candidate
  fetch-projects-for-candidate)

(rf/reg-event-db
  :projects/projects-for-candidate-success
  projects-for-candidate-success)

(rf/reg-event-fx
  :projects/projects-for-candidate-failure
  projects-for-candidate-failure)

(rf/reg-event-fx
  :projects/click-project
  click-project)

(rf/reg-event-fx
  :projects/click-candidate
  click-candidate)

(rf/reg-event-fx
  :projects/remove-candidate-from-list
  remove-candidate-from-list)

(rf/reg-event-db
  :projects/add-to-disabled-projects
  add-to-disabled-projects)

(rf/reg-event-db
  :projects/remove-from-disabled-projects
  remove-from-disabled-projects)

(rf/reg-event-fx
  :projects/open-edit-modal
  open-edit-modal)

(rf/reg-event-fx
  :projects/open-delete-modal
  open-delete-modal)

(rf/reg-event-db
  :projects/clear-project-form
  clear-project-form)

(rf/reg-event-fx
  :projects/close-modal
  close-modal)
