(ns recruit-app.modals.email.events
  (:require [recruit-app.events :as events]
            [ajax.core :as ajax]
            [recruit-app.util.events :as ev]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.email :as eu]
            [recruit-app.util.uri :as u]
            [recruit-app.components.modal :as modal]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.response :as r]))

(def template-keys [:template-id :template-name :template-subject :template-body])
(def db-keys [:id :name :subject :msg-body])

(ev/reg-events "email" ["subject" "greetings" "msg-body" "active-template" "name" "show-errors?"])
(ev/reg-toggle-event "email" "is-sending?")

(defn convert-to-vector
  [template]
  (reduce #(conj %1 (get template %2)) [] template-keys))

(defn key-by-id
  [templates]
  (reduce #(merge %1 (hash-map (:id %2) %2)) {} templates))

(defn convert-keys
  [template]
  (->> template
       convert-to-vector
       (zipmap db-keys)))

(defn process-response
  "Processes response from getting email templates"
  [db [_ response]]
  (let [resp (->> response
                  (keywordize-keys)
                  (map convert-keys)
                  (key-by-id))]
    (assoc-in db [:email :templates] resp)))

(defn get-templates
  "Calls API to fetch templates if not already fetched"
  [{:keys [db]} _]
  (when-not (get-in db [:email :templates])
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :get-templates)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:email/process-response]
                     :on-failure      [:email/process-response]}}))

(defn togg-email-modal
  "Toggles value for show-email-modal?"
  [db _]
  (assoc-in db [:email :show-email-modal?] (not (get-in db [:email :show-email-modal?]))))

(defn clear-email-modal
  "Dissocs values from email db"
  [db _]
  (update db :email dissoc :recipients :subject :greetings :msg-body :name :active-template :show-errors?))

(defn close-email-modal
  "Clears modal and closes"
  [_ _]
  {:dispatch-n [[:email/togg-email-modal]
                [:email/clear-email-modal]]})

(defn template-click
  "Sets active template"
  [_ [_ template]]
  {:dispatch-n [[:email/active-template-change (:id template)]
                [:email/merge-template template]]})

(defn merge-template
  "Sets current template values in db"
  [db [_ template]]
  (update-in db [:email] merge (select-keys template [:subject :msg-body :name])))

(defn process-update-response
  "Associates new values for template"
  [db [_ response]]
  (let [{:keys [active-template msg-body subject name]} (:email db)]
    (-> db
        (assoc-in [:email :templates active-template :name] name)
        (assoc-in [:email :templates active-template :subject] subject)
        (assoc-in [:email :templates active-template :msg-body] msg-body))))

(defn update-template
  "Calls API to update template"
  [{:keys [db]} _]
  (let [{:keys [active-template msg-body subject name]} (:email db)
        data {:name     name
              :subject  subject
              :msg-body msg-body}]
    {:ra-http-xhrio {:method          :put
                     :uri             (u/uri :update-template active-template)
                     :params          data
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:email/process-update-response]
                     :on-failure      [:http-no-on-failure]}}))

(defn add-template
  "Associates new template to db"
  [db [_ {:keys [id] :as template}]]
  (assoc-in db [:email :templates id] template))

(defn process-create-response
  "Associates new template to db and makes active"
  [db [_ template response]]
  (let [template-id (->> response
                         (keywordize-keys)
                         :template-id)]
    {:dispatch-n [[:email/add-template (assoc template :id template-id)]
                  [:email/active-template-change template-id]]}))

(defn save-template
  "Calls API to save new template"
  [{:keys [db]} _]
  (let [{:keys [msg-body subject name] :as template} (:email db)
        data {:name     name
              :subject  subject
              :msg-body msg-body}]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :create-template)
                     :params          data
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:email/process-create-response template]
                     :on-failure      [:email/process-create-response template]}}))

(defn dissoc-template
  "Dissocs template from db"
  [db [_ id]]
  (update-in db [:email :templates] dissoc id))

(defn process-delete-response
  "Processes response from delete call"
  [{:keys [db]} [_ id response]]
  (let [active-template (get-in db [:email :active-template])
        dispatch (cond-> (into '() [[:email/dissoc-template id]])
                         (= id active-template) (conj [:email/active-template-change nil]))]
    (if response
      {:dispatch-n (into [] dispatch)}
      {})))

(defn delete-template
  "Makes call to API to delete template"
  [_ [_ id]]
  {:ra-http-xhrio {:method          :delete
                   :uri             (u/uri :delete-template id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:email/process-delete-response id]
                   :on-failure      [:email/process-delete-response id]}})

(defn process-send-response
  "Closes modal and displays alert"
  [_ _]
  {:dispatch-n [[:email/togg-email-modal]
                [:alerts/add-success "Successfully Sent Email To Candidate!"]
                [:email/toggle-is-sending?]]})

(defn send
  "Calls API to send email"
  [{:keys [db]} _]
  {:dispatch-n [[:email/toggle-is-sending?]
                [:email/show-errors?-change false]]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :send-email)
                   :params          (-> db :email eu/email-send-data)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:email/process-send-response]
                   :on-failure      [:email/process-send-response]}})

(defn set-email-recipients
  "Sets given recipients to email recipients"
  [db [_ recipients]]
  (assoc-in db [:email :recipients] recipients))

(events/reg-event-db
  :email/process-response
  process-response)

(events/reg-event-fx
  :email/get-templates
  get-templates)

(events/reg-event-db
  :email/togg-email-modal
  togg-email-modal)

(events/reg-event-db
  :email/clear-email-modal
  clear-email-modal)

(events/reg-event-fx
  :email/close-email-modal
  close-email-modal)

(events/reg-event-fx
  :email/template-click
  template-click)

(events/reg-event-db
  :email/merge-template
  merge-template)

(events/reg-event-db
  :email/process-update-response
  process-update-response)

(events/reg-event-fx
  :email/update-template
  update-template)

(events/reg-event-db
  :email/add-template
  add-template)

(events/reg-event-fx
  :email/process-create-response
  process-create-response)

(events/reg-event-fx
  :email/save-template
  save-template)

(events/reg-event-db
  :email/dissoc-template
  dissoc-template)

(events/reg-event-fx
  :email/process-delete-response
  process-delete-response)

(events/reg-event-fx
  :email/delete-template
  delete-template)

(events/reg-event-fx
  :email/process-send-response
  process-send-response)

(events/reg-event-fx
  :email/send
  send)

(events/reg-event-db
  :email/set-email-recipients
  set-email-recipients)
