(ns recruit-app.saved-searches.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [clojure.set :refer [subset?]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [cljs-time.format :as f]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [recruit-app.util.input-view :as iv]
            [recruit-app.modals.saved-search.views :as edit]
            [recruit-app.util.saved_search :as ssu]
            [recruit-app.components.loading :as lo]
            [recruit-app.components.header :as header]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.table :as table]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.components.icon :as icon]
            [recruit-app.components.button :as btn]
            [recruit-app.components.form :as form]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.util.date :as d]))

(defn edit-actions
  [saved-search]
  [layout/row
   :padding 0
   :children [[icon/pencil
               :on-click #(rf/dispatch [:saved-searches/open-edit-modal saved-search])]]])

(defn saved-search-row
  [_ {:keys [search-id search-name update-time] :as saved-search}]
  [[link/table-cell-hyperlink
    :label search-name
    :on-click #(rf/dispatch [:saved-searches/saved-search-clicked search-id])]
   (ssu/email-frequency-display-name saved-search)
   (d/formatted-date :date (f/parse (f/formatters :date-time) update-time))
   [edit-actions saved-search]])

(defn stats-box
  []
  (let [stats (rf/subscribe [:saved-searches/stats])]
    (fn []
      [:div
       (use-style styles/info-box)
       [layout/row-top
        :padding 3
        :children [[:div
                    (use-style styles/info-box-display)
                    (str "Searches saved: " (:total-count @stats))]]]
       [layout/row-bottom
        :padding 3
        :children [[:div
                    (use-style styles/info-box-display)
                    (str "Searches emailed: " (:emailed-count @stats))]]]])))

(def table-headers
  [{:label "Name"
    :width 50
    :sort  {:name (sort-util/sort-fn :search-name compare)}}
   {:label "Email Frequency"
    :width 20}
   {:label "Last Updated"
    :sort  {:time-updated (sort-util/sort-fn (comp (partial f/parse (f/formatters :date-time)) :update-time) sort-util/before?)}
    :width 15}
   {:label "Edit"
    :width 5}])

(def table-actions
  [{:label    "Remove"
    :on-click #(rf/dispatch [:saved-searches/delete %])}])

(defn table
  []
  (let [loading? (rf/subscribe [:saved-searches/fetching-all?])]
    [table/frontend-pagination-table
     :table-key ::table/saved-searches
     :headers table-headers
     :row-data-fn saved-search-row
     :data-sub :saved-searches/saved-searches-vector
     :actions table-actions
     :loading? loading?]))

(defn body
  []
  (fn []
    [layout/column
     :padding 0
     :children [[layout/row
                 :padding-top 30
                 :children [[table]]]]]))

(defn index
  []
  (fn []
    (rf/dispatch [:saved-searches/load-view])
    [layout/column
     :padding 0
     :children [[header/page-header
                 :header-text "Saved Searches"
                 :sub-header-text "Save up to 100 searches. You can have 10 searches emailed to you daily or once a week."
                 :right-element [stats-box]]
                [layout/page-content [body]]
                [edit/edit-modal]]]))