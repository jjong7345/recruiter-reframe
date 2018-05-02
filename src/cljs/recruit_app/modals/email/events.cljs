(ns recruit-app.modals.email.events
  (:require [re-frame.core :as rf]
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

(rf/reg-event-db
  :email/process-response
  (fn
    [db [_ response]]
    (let [resp (->> response
                    (keywordize-keys)
                    (map convert-keys)
                    (key-by-id))]
      (assoc-in db [:email :templates] resp))))

(rf/reg-event-fx
  :email/get-templates
  (fn [{:keys [db]} _]
    (when-not (get-in db [:email :templates])
      {:ra-http-xhrio {:method          :get
                       :uri             (u/uri :get-templates)
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:email/process-response]
                       :on-failure      [:email/process-response]}})))

(rf/reg-event-db
  :email/togg-email-modal
  (fn [db _]
    (assoc-in db [:email :show-email-modal?] (not (get-in db [:email :show-email-modal?])))))

(rf/reg-event-db
  :email/clear-email-modal
  (fn [db _]
    (update db :email dissoc :recipients :subject :greetings :msg-body :name :active-template :show-errors?)))

(rf/reg-event-fx
  :email/close-email-modal
  (fn [_ _]
    {:dispatch-n [[:email/togg-email-modal]
                  [:email/clear-email-modal]]}))

(rf/reg-event-fx
  :email/template-click
  (fn [_ [_ template]]
    {:dispatch-n [[:email/active-template-change (:id template)]
                  [:email/merge-template template]]}))

(rf/reg-event-db
  :email/merge-template
  (fn [db [_ template]]
    (update-in db [:email] merge (select-keys template [:subject :msg-body :name]))))

(rf/reg-event-db
  :email/process-update-response
  (fn [db [_ response]]
    (let [{:keys [active-template msg-body subject name]} (:email db)]
      (-> db
          (assoc-in [:email :templates active-template :name] name)
          (assoc-in [:email :templates active-template :subject] subject)
          (assoc-in [:email :templates active-template :msg-body] msg-body)))))

(rf/reg-event-db
  :email/process-update-fail
  (fn [db [_ response]]
    db))

(rf/reg-event-fx
  :email/update-template
  (fn [{:keys [db]} _]
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
                       :on-failure      [:email/process-update-fail]}})))

(rf/reg-event-db
  :email/add-template
  (fn [db [_ {:keys [id] :as template}]]
    (assoc-in db [:email :templates id] template)))

(rf/reg-event-fx
  :email/process-create-response
  (fn [db [_ template response]]
    (let [template-id (->> response
                           (keywordize-keys)
                           :template-id)]
      {:dispatch-n [[:email/add-template (assoc template :id template-id)]
                    [:email/active-template-change template-id]]})))

(rf/reg-event-fx
  :email/save-template
  (fn [{:keys [db]} _]
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
                       :on-failure      [:email/process-create-response template]}})))

(rf/reg-event-db
  :email/dissoc-template
  (fn [db [_ id]]
    (update-in db [:email :templates] dissoc id)))

(rf/reg-event-fx
  :email/process-delete-response
  (fn [{:keys [db]} [_ id response]]
    (let [active-template (get-in db [:email :active-template])
          dispatch (cond-> (into '() [[:email/dissoc-template id]])
                           (= id active-template) (conj [:email/active-template-change nil]))]
      (if response
        {:dispatch-n (into [] dispatch)}
        {}))))

(rf/reg-event-fx
  :email/delete-template
  (fn [_ [_ id]]
    {:ra-http-xhrio {:method          :delete
                     :uri             (u/uri :delete-template id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:email/process-delete-response id]
                     :on-failure      [:email/process-delete-response id]}}))

(rf/reg-event-fx
  :email/process-send-response
  (fn [_ _]
    {:dispatch-n [[:email/togg-email-modal]
                  [:alerts/add-success "Successfully Sent Email To Candidate!"]
                  [:email/toggle-is-sending?]]}))

(rf/reg-event-fx
  :email/send
  (fn [{:keys [db]} _]
    {:dispatch-n [[:email/toggle-is-sending?]
                  [:email/show-errors?-change false]]
     :ra-http-xhrio {:method          :post
                     :uri             (u/uri :send-email)
                     :params          (-> db :email eu/email-send-data)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:email/process-send-response]
                     :on-failure      [:email/process-send-response]}}))

(rf/reg-event-db
  ;; Sets given recipients to email recipients
  :email/set-email-recipients
  (fn [db [_ recipients]]
    (assoc-in db [:email :recipients] recipients)))
