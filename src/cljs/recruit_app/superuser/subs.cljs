(ns recruit-app.superuser.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]
            [recruit-app.superuser.db :as db]
            [recruit-app.util.date :as d]))

(subs/reg-subs "superuser" [["active-tab" :change-user]
                            ["impersonate-recruiter-id" ""]
                            ["redact-permissions-recruiter-id" ""]
                            ["show-redact-permissions?" false]
                            ["redact-permissions" {}]
                            ["redact-start-date" nil] ["redact-end-date" nil]
                            ["inventory-recruiter-id" ""]
                            ["inventory-action" :purchase-inventory]
                            ["inventory-type" :promoted-job]
                            ["inventory-quantity" ""]
                            ["inventory" {}] ["show-inventory?" false]
                            ["feature-job-id" ""] ["feature-job-start-date" nil]
                            ["feature-job-end-date" nil]
                            ["feature-job-is-feature?" false]
                            ["repost-job-id" ""] ["feature-job" nil]])

(defn active-permissions
  "Retrieves permissions for recruiter-id"
  [[permissions recruiter-id] _]
  (get permissions (js/parseInt recruiter-id)))

(defn has-unlimited-redaction-permission?
  "Due to legacy data, some users have unredacted permission but no dates"
  [{:keys [can-view-redacted unredacted-start-date unredacted-end-date]} _]
  (and (= can-view-redacted "true")
       (not unredacted-start-date)
       (not unredacted-end-date)))

(defn active-redact-start-date
  "Returns start date from permissions map for active recruiter"
  [start-date _]
  (d/utc-date-time start-date))

(defn active-redact-end-date
  "Returns end date from permissions map for active recruiter"
  [end-date _]
  (d/utc-date-time end-date))

(defn inventory-form
  "Returns map of inventory inputs to be validated"
  [[recruiter-id action type quantity] _]
  {::db/recruiter-id (js/parseInt recruiter-id)
   ::db/action       action
   ::db/type         type
   ::db/quantity     (js/parseInt quantity)})

(defn active-inventory
  "Returns inventory from map for recruiter-id"
  [[inventory recruiter-id] _]
  (get inventory (js/parseInt recruiter-id)))

(defn active-inventory-list
  "Returns sales portion of inventory history"
  [{:keys [sales use]} _]
  (vec
    (concat
      (mapv #(assoc % :action "Purchase") sales)
      (mapv #(assoc % :action "Use") use))))

(defn show-cancel-promotion?
  "Only show cancel button if job is currently promoted"
  [promoted? _]
  promoted?)

(rf/reg-sub
  :superuser/active-redact-start-date
  :<- [:superuser/redact-start-date]
  active-redact-start-date)

(rf/reg-sub
  :superuser/active-redact-end-date
  :<- [:superuser/redact-end-date]
  active-redact-end-date)

(rf/reg-sub
  :superuser/active-permissions
  :<- [:superuser/redact-permissions]
  :<- [:superuser/redact-permissions-recruiter-id]
  active-permissions)

(rf/reg-sub
  :superuser/has-unlimited-redaction-permission?
  :<- [:superuser/active-permissions]
  has-unlimited-redaction-permission?)

(rf/reg-sub
  :superuser/inventory-form
  :<- [:superuser/inventory-recruiter-id]
  :<- [:superuser/inventory-action]
  :<- [:superuser/inventory-type]
  :<- [:superuser/inventory-quantity]
  inventory-form)

(rf/reg-sub
  :superuser/active-inventory
  :<- [:superuser/inventory]
  :<- [:superuser/inventory-recruiter-id]
  active-inventory)

(rf/reg-sub
  :superuser/active-inventory-list
  :<- [:superuser/active-inventory]
  active-inventory-list)

(rf/reg-sub
  :superuser/show-cancel-promotion?
  :<- [:superuser/feature-job-is-feature?]
  show-cancel-promotion?)
