(ns recruit-app.page_404.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]))

(defn message
  []
  (fn[]
    [rc/v-box
     :class "info"
     :children [[:p "Unfortunately the page you were looking for either doesn't exist, or has moved on to a brighter future."]
                [:p "Of course, you can always" [:span.link {:on-click #(rf/dispatch [:go-to-route "/"])} " go back to Home >>"]]]]))

(defn body
  []
  (fn []
    [rc/v-box
     :class "content"
     :children [[rc/h-box
                 :justify :end
                 :children [[message]]]]]))

(defn index
  []
  (fn []
    [rc/h-box
     :class "page-404 main"
     :children [[body]]]))