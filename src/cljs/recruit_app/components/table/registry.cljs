(ns recruit-app.components.table.registry
  (:require [re-frame.core :as rf]
            [recruit-app.components.table :as table]))

;; Subs

(defn- registered?
  "Checks if given registry-key is true for given table-key"
  [db table-key registry-key]
  (get-in db [::table/table table-key ::table/registry registry-key] false))

(defn pagination-registered?
  "Returns whether or not pagination subs have been registered for given table"
  [db [_ table-key]]
  (registered? db table-key ::table/pagination-registered?))

(defn sortable-registered?
  "Returns whether or not sortable subs have been registered for given table"
  [db [_ table-key]]
  (registered? db table-key ::table/sortable-registered?))

(defn backend-pagination-registered?
  "Returns whether or not backend pagination subs have been registered for given table"
  [db [_ table-key]]
  (registered? db table-key ::table/backend-pagination-registered?))

(defn frontend-pagination-registered?
  "Returns whether or not frontend-pagination subs have been registered for given table"
  [db [_ table-key]]
  (registered? db table-key ::table/frontend-pagination-registered?))

(defn actions-registered?
  "Returns whether or not actions subs have been registered for given table"
  [db [_ table-key]]
  (registered? db table-key ::table/actions-registered?))

(rf/reg-sub
  ::table/pagination-registered?
  pagination-registered?)

(rf/reg-sub
  ::table/sortable-registered?
  sortable-registered?)

(rf/reg-sub
  ::table/backend-pagination-registered?
  backend-pagination-registered?)

(rf/reg-sub
  ::table/frontend-pagination-registered?
  frontend-pagination-registered?)

(rf/reg-sub
  ::table/actions-registered?
  actions-registered?)

;; Events

(defn- registered
  "Sets given registry-key to true for given table-key"
  [db table-key registry-key]
  (assoc-in db [::table/table table-key ::table/registry registry-key] true))

(defn pagination-registered
  "Sets pagination-registered? to true for given table-key"
  [db [_ table-key]]
  (registered db table-key ::table/pagination-registered?))

(defn sortable-registered
  "Sets sortable-registered? to true for given table-key"
  [db [_ table-key]]
  (registered db table-key ::table/sortable-registered?))

(defn backend-pagination-registered
  "Sets backend-pagination-registered? to true for given table-key"
  [db [_ table-key]]
  (registered db table-key ::table/backend-pagination-registered?))

(defn frontend-pagination-registered
  "Sets frontend-pagination-registered? to true for given table-key"
  [db [_ table-key]]
  (registered db table-key ::table/frontend-pagination-registered?))

(defn actions-registered
  "Sets actions-registered? to true for given table-key"
  [db [_ table-key]]
  (registered db table-key ::table/actions-registered?))

(rf/reg-event-db
  ::table/pagination-registered
  pagination-registered)

(rf/reg-event-db
  ::table/sortable-registered
  sortable-registered)

(rf/reg-event-db
  ::table/backend-pagination-registered
  backend-pagination-registered)

(rf/reg-event-db
  ::table/frontend-pagination-registered
  frontend-pagination-registered)

(rf/reg-event-db
  ::table/actions-registered
  actions-registered)
