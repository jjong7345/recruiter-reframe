(ns recruit-app.util.inventory
  (:require [clojure.set :refer [rename-keys]]))

(defn- with-action-info
  "Adds info based on action to inventory request"
  [{:keys [action] :as request}]
  (cond-> request
          (= action :purchase-inventory) (assoc :purchase-info "Superuser")
          (= action :use-inventory) (assoc :use-for "Superuser")))

(defn save-request
  "Creates request to save inventory from superuser map in db"
  [{:keys [inventory-action inventory-type inventory-quantity]
    :or   {inventory-action :purchase-inventory inventory-type :promoted-job}}]
  (with-action-info {:action       inventory-action
                     :product-type inventory-type
                     :quantity     (js/parseInt inventory-quantity)}))
