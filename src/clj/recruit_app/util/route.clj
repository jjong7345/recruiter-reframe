(ns recruit-app.util.route
  "Functions for defining custom types of routes."
  (:require [compojure.response :as response]
            [ring.util.response :refer [status header]]))

(defn not-found-redirect
  "Returns a route that always returns a 302 and redirect."
  [body]
  (fn [request]
    (-> (response/render body request)
        (status 302)
        (header "Location" body)
        (assoc :body nil))))
