(ns recruit-app.shopify.webhooks
  (:require [config.core :refer [env]]
            [taoensso.timbre :as log]
            [ring.util.response :as resp]
            [clj-http.client :as http]
            [recruit-app.sales-leads.api :as leads]
            [recruit-app.recruiter.api :as rec-api]
            [buddy.core.mac :as mac])
  (:import (java.util Base64)))

(defn- trusted-shopify-callback?
  "Checks integrity of a shopify webhook callback request by verifying its signature
  against shopify's notification secret.
  -type: the webhook request type
  -req: the request map"
  [type {{x-shopify-hmac-sha256      "x-shopify-hmac-sha256"
          http_x_shopify_hmac_sha256 "http_x_shopify_hmac_sha256"} :headers
         body                                                      :json-string}]
  (try
    (let [secret (get-in env [:shopify :webhook-secrets type])
          signature (->> (or x-shopify-hmac-sha256 http_x_shopify_hmac_sha256)
                         (.decode (Base64/getDecoder)))]
      (mac/verify body signature {:key secret
                                  :alg :hmac+sha256}))
    (catch Exception e
      (log/warn e)
      false)))

(defn- trigger-inventory-update
  [body]
  "forwards order info to inventory."
  (http/post (-> env :lambda-server :shopify-order-listener)
             {:content-type :json
              :body         body}))

(defn- promotion-product? [product-id]
  "checks if a given product is of a 'promotion-credit' product type."
  ((-> env :shopify :products-ids-by-type :promotion-credit set) product-id))

(defn- submit-promotion-sales-lead
  "Submits a sales lead for orders that contain promotion-credit content."
  [{purchases :line_items
    rec-email :email}]
  (when (some (comp promotion-product? :product_id) purchases)
    (->> rec-email
         (rec-api/recruiter-id-by-email)
         (leads/submit-recruiter-info :promotion-credit-purchase))))

(defn order-created
  "Callback to handle order_created shopify webhook triggers.
  Asynchronously sends order info to lambda inventory and submits recruiter info to hubspot."
  [{:keys [params json-string] :as req}]
  (when (trusted-shopify-callback? :order-created req)
    (future
      (try (trigger-inventory-update json-string)
           (catch Exception e (log/error e)))
      (try (submit-promotion-sales-lead params)
           (catch Exception e (log/error e)))))
  (resp/response nil))