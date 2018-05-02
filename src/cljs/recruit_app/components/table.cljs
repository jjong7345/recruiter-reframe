(ns recruit-app.components.table
  (:require [re-frame.core :as rf]
            [recruit-app.events :as events]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.components.util :as util]
            [reagent.core :as r]
            [recruit-app.util.img :as img]
            [recruit-app.util.subscription :as subs]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.loading :as loading]
            [ajax.core :as ajax]
            [recruit-app.util.ajax :as a]
            [re-com.validate :refer-macros [validate-args-macro]]
            [recruit-app.components.button :as btn]
            [recruit-app.util.input-view :as iv]
            [clojure.set :refer [union subset?]]
            [cljs.spec.alpha :as s]))

(s/def ::page-data coll?)
(s/def ::pages-data (s/map-of ::page ::page-data))
(s/def ::total nat-int?)
(s/def ::per-page pos-int?)
(s/def ::page nat-int?)
(s/def ::loading? boolean?)
(s/def ::fetching-pages (s/coll-of ::page :kind? set?))
(s/def ::pagination (s/keys :opt [::page ::per-page ::total ::page-data
                                  ::loading?]))
(s/def ::dir #{:asc :desc})
(s/def ::col keyword?)
(s/def ::sort (s/keys :opt [::col ::dir]))
(s/def ::checked-rows set?)
(s/def ::table-config (s/keys :opt [::sort ::pagination ::checked-rows
                                    ::registry]))

(s/def ::jobs ::table-config)
(s/def ::active-jobs ::table-config)
(s/def ::pending-jobs ::table-config)
(s/def ::rejected-jobs ::table-config)
(s/def ::removed-jobs ::table-config)
(s/def ::job-applicants ::table-config)
(s/def ::job-viewers ::table-config)
(s/def ::projects ::table-config)
(s/def ::project-candidates ::table-config)
(s/def ::teams ::table-config)
(s/def ::team-members ::table-config)
(s/def ::pending-recruiters ::table-config)
(s/def ::pending-escalated-recruiters ::table-config)
(s/def ::recently-approved-recruiters ::table-config)
(s/def ::recruiter-search ::table-config)
(s/def ::saved-searches ::table-config)
(s/def ::search-results ::table-config)
(s/def ::table (s/keys :opt [::jobs
                             ::active-jobs
                             ::pending-jobs
                             ::rejected-jobs
                             ::removed-jobs
                             ::job-applicants
                             ::job-viewers
                             ::projects
                             ::project-candidates
                             ::teams
                             ::team-members
                             ::pending-recruiters
                             ::pending-escalated-recruiters
                             ::recently-approved-recruiters
                             ::recruiter-search
                             ::saved-searches
                             ::search-results]))

;; Subs won't actually enforce the spec, but the predicate will be the output
(s/def ::page-sub ::page)
(s/def ::current-page-data-sub ::page-data)
(s/def ::frontend-total-sub ::total)
(s/def ::sorted-data-sub ::page-data)
(s/def ::page-map-sub ::pages-data)
(s/def ::sort-col-sub ::col)
(s/def ::sort-dir-sub ::dir)
(s/def ::sorted-by? boolean?)
(s/def ::checked-rows-sub ::checked-rows)
(s/def ::checked-total-sub nat-int?)
(s/def ::all-checked? boolean?)

(s/def ::pagination-registered? boolean?)
(s/def ::sortable-registered? boolean?)
(s/def ::backend-pagination-registered? boolean?)
(s/def ::frontend-pagination-registered? boolean?)
(s/def ::actions-registered? boolean?)
(s/def ::registry (s/keys :opt [::pagination-registered?
                                ::sortable-registered?
                                ::backend-pagination-registered?
                                ::frontend-pagination-registered?
                                ::actions-registered?]))

;; Not sure what the predicate should be for event specs
(s/def ::set-page-event any?)
(s/def ::sort-event any?)
(s/def ::fetch-success-event any?)
(s/def ::fetch-failure-event any?)
(s/def ::backend-paginate-event any?)
(s/def ::reset-backend-pagination-event any?)
(s/def ::on-check any?)
(s/def ::on-check-all any?)
(s/def ::on-clear-checked any?)
(s/def ::pagination-registered any?)
(s/def ::sortable-registered any?)
(s/def ::backend-pagination-registered any?)
(s/def ::frontend-pagination-registered any?)
(s/def ::actions-registered any?)

(def table-namespace "table")
(def table-keyword (partial keyword table-namespace))

(defn- table-keyed-keyword
  "Returns fully qualified key in table namespace for given key and table-key
  e.g.
  => (table-keyed-keyword ::jobs-table ::sorted-data-sub)
  => :recruit-app.components.table/jobs-table-sorted-data-sub"
  [k table-key]
  (table-keyword (str (name table-key) "-" (name k))))

;; Subs

(def sorted-data-sub (partial table-keyed-keyword ::sorted-data-sub))
(def page-data-sub (partial table-keyed-keyword ::page-data-sub))
(def page-number-sub (partial table-keyed-keyword ::page-sub))
(def sort-dir-sub (partial table-keyed-keyword ::sort-dir-sub))
(def sort-col-sub (partial table-keyed-keyword ::sort-col-sub))
(def sorted-by-sub (partial table-keyed-keyword ::sorted-by?))
(def page-map-sub (partial table-keyed-keyword ::page-map-sub))
(def total-sub (partial table-keyed-keyword ::total-sub))
(def loading-sub (partial table-keyed-keyword ::loading?))
(def checked-sub (partial table-keyed-keyword ::checked-rows-sub))
(def checked-total-sub (partial table-keyed-keyword ::checked-total-sub))
(def all-checked-sub (partial table-keyed-keyword ::all-checked?))

;; Events

(def set-page-event (partial table-keyed-keyword ::set-page-event))
(def sort-event (partial table-keyed-keyword ::sort-event))
(def pagination-event (partial table-keyed-keyword ::backend-paginate-event))
(def fetch-success-event (partial table-keyed-keyword ::fetch-success-event))
(def fetch-failure-event (partial table-keyed-keyword ::fetch-failure-event))
(def reset-event (partial table-keyed-keyword ::reset-backend-pagination-event))
(def on-check-event (partial table-keyed-keyword ::on-check))
(def on-check-all-event (partial table-keyed-keyword ::on-check-all))
(def clear-checked-event (partial table-keyed-keyword ::on-clear-checked))

(defn- add-or-remove
  "Adds or removes val from set depending on if it is already a member"
  [coll val]
  (if (contains? coll val)
    (disj coll val)
    (conj coll val)))

(defn register-pagination
  [table-key page-map-sub]
  (let [registered? (rf/subscribe [::pagination-registered? table-key])]
    (when-not @registered?
      (rf/reg-sub
        (page-number-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::pagination ::page] 0)))

      (rf/reg-sub
        (page-data-sub table-key)
        :<- [page-map-sub]
        :<- [(page-number-sub table-key)]
        (fn [[page-map page-number]]
          (get page-map page-number [])))

      (events/reg-event-fx
        (set-page-event table-key)
        (fn [{:keys [db]} [_ page]]
          {:db       (assoc-in db [::table table-key ::pagination ::page] page)
           :dispatch [:scroll-top]}))

      (rf/dispatch [::pagination-registered table-key]))))

(defn register-sortable-headers
  "Will create subs and events for sortable headers"
  [table-key initial-sort-col initial-sort-dir]
  (let [registered? (rf/subscribe [::sortable-registered? table-key])]
    (when-not @registered?
      (rf/reg-sub
        (sort-col-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::sort ::col] initial-sort-col)))

      (rf/reg-sub
        (sort-dir-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::sort ::dir] initial-sort-dir)))

      (rf/reg-sub
        (sorted-by-sub table-key)
        :<- [(sort-col-sub table-key)]
        (fn [sorted-by [_ sort-key]]
          (= sorted-by sort-key)))

      (events/reg-event-fx
        (sort-event table-key)
        (fn [{:keys [db]} [_ sort-col sort-dir]]
          {:db       (-> db
                         (assoc-in [::table table-key ::sort ::col] sort-col)
                         (assoc-in [::table table-key ::sort ::dir] sort-dir))
           :dispatch [(set-page-event table-key) 0]}))

      (rf/dispatch [::sortable-registered table-key]))))

(defn register-backend-pagination
  [table-key {:keys [fetch-url per-page]
              :or   {per-page 10}}]
  (let [registered? (rf/subscribe [::backend-pagination-registered? table-key])]
    (when-not @registered?
      (rf/reg-sub
        (page-map-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::pagination ::pages-data] {})))

      (rf/reg-sub
        (total-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::pagination ::total] 0)))

      (rf/reg-sub
        (loading-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::pagination ::loading?] false)))

      (events/reg-event-db
        (fetch-success-event table-key)
        (fn [db [_ page on-success {:keys [total results] :as response}]]
          (when on-success
            (on-success response))
          (-> db
              (assoc-in [::table table-key ::pagination ::pages-data page] results)
              (assoc-in [::table table-key ::pagination ::total] total)
              (assoc-in [::table table-key ::pagination ::loading?] false)
              (update-in [::table table-key ::pagination ::fetching-pages] disj page))))

      (rf/reg-event-db
        (fetch-failure-event table-key)
        (fn [db [_ page]]
          (-> db
              (assoc-in [::table table-key ::pagination ::loading?] false)
              (update-in [::table table-key ::pagination ::fetching-pages] disj page))))

      (events/reg-event-fx
        (pagination-event table-key)
        (fn [{:keys [db]} [_ page fetch-params on-success]]
          (when-not (or (get-in db [::table table-key ::pagination ::pages-data page])
                        (contains? (get-in db [::table table-key ::pagination ::fetching-pages] #{}) page))
            {:db            (-> db
                                (assoc-in [::table table-key ::pagination ::loading?] true)
                                (update-in [::table table-key ::pagination ::fetching-pages] (fnil conj #{}) page))
             :ra-http-xhrio {:method          :post
                             :uri             fetch-url
                             :params          (assoc fetch-params :limit per-page :offset (* per-page page))
                             :format          (ajax/json-request-format)
                             :response-format (a/json-response-format {:keywords? true})
                             :on-success      [(fetch-success-event table-key) page on-success]
                             :on-failure      [(fetch-failure-event table-key) page]}})))

      (events/reg-event-db
        (reset-event table-key)
        (fn [db]
          (update-in db [::table table-key] dissoc ::pagination)))

      (rf/dispatch [::backend-pagination-registered table-key]))))

(defn register-frontend-pagination
  [table-key data-sub sorters]
  (let [registered? (rf/subscribe [::frontend-pagination-registered? table-key])]
    (when-not @registered?
      (rf/reg-sub
        (total-sub table-key)
        :<- [data-sub]
        (fn [data]
          (count data)))

      (rf/reg-sub
        (sorted-data-sub table-key)
        :<- [data-sub]
        :<- [(sort-col-sub table-key)]
        :<- [(sort-dir-sub table-key)]
        (fn [[data sort-col sort-dir]]
          (let [sort-fn (when sort-col ((get sorters sort-col) sort-dir))]
            (if sort-fn (sort-fn data) data))))

      (rf/reg-sub
        (page-map-sub table-key)
        :<- [(sorted-data-sub table-key)]
        (fn [sorted-data]
          (let [pages (partition-all 10 sorted-data)]
            (zipmap (take (count pages) (range)) pages))))

      (rf/dispatch [::frontend-pagination-registered table-key]))))

(defn- register-table-actions
  "Registers subs/events for ability to check row data and perform action"
  [table-key]
  (let [registered? (rf/subscribe [::actions-registered? table-key])]
    (when-not @registered?
      (rf/reg-sub
        (checked-sub table-key)
        (fn [db]
          (get-in db [::table table-key ::checked-rows] #{})))

      (rf/reg-sub
        (checked-total-sub table-key)
        :<- [(checked-sub table-key)]
        (fn [checked]
          (count checked)))

      (rf/reg-sub
        (all-checked-sub table-key)
        :<- [(page-data-sub table-key)]
        :<- [(checked-sub table-key)]
        (fn [[page-data checked]]
          (and (not-empty page-data) (subset? page-data checked))))

      (events/reg-event-db
        (on-check-event table-key)
        (fn [db [_ val]]
          (update-in db [::table table-key ::checked-rows] (fnil add-or-remove #{}) val)))

      (events/reg-event-fx
        (on-check-all-event table-key)
        (fn [{:keys [db]} [_ vals checked?]]
          (if checked?
            {:dispatch [(clear-checked-event table-key)]}
            {:db (update-in db [::table table-key ::checked-rows] (fnil union #{}) (set vals))})))

      (events/reg-event-db
        (clear-checked-event table-key)
        (fn [db]
          (update-in db [::table table-key] dissoc ::checked-rows)))

      (rf/dispatch [::actions-registered table-key]))))

;; End Subs/Events

(defn filter-bar
  "An additional bar that can appear at the top of a table with styling of row"
  [& params]
  [util/recom-component-with-styles
   rc/h-box
   params
   styles/table-filter-bar])

(defn- style-with-width
  "Associates width to given styles if width is given"
  [styles width]
  (if width
    (assoc styles :width (str width "%"))
    styles))

(defn header-cell
  "Renders table header cell"
  [& {:keys [label width]}]
  [:th
   (use-style (style-with-width styles/table-header-cell width))
   label])

(defn- header-row
  "Renders header row for table"
  [& headers]
  (into [:tr (use-style styles/table-header-row)] headers))

(defn- cell
  "Renders table cell"
  [& {:keys [content colspan]
      :or   {colspan 1}}]
  [:td
   (merge
     (use-style styles/table-cell)
     {:colSpan colspan})
   content])

(defn- row
  "Renders table row"
  [& {:keys [cells on-click]}]
  (reduce
    conj
    [:tr (merge
           (use-style (if on-click styles/clickable-table-row styles/table-row))
           (when on-click {:on-click on-click}))]
    cells))

(defn multiline-content
  "Renders content for table cell with styling for multiline"
  [& {:keys [top bottom]}]
  [layout/column
   :padding 0
   :width "100%"
   :children [(when (seq top)
                [layout/row
                 :padding 0
                 :children [[:div
                             (use-style styles/multiline-top)
                             top]]])
              (when (seq bottom)
                [layout/row
                 :padding 0
                 :children [[:div
                             (use-style styles/multiline-bottom)
                             bottom]]])]])

(def down-arrow [:div (use-style styles/down-sort-arrow)])
(def up-arrow [:div (use-style styles/up-sort-arrow)])
(def double-arrow [:div (use-style styles/double-sort-arrow)])

(defn sortable-header-cell
  "Creates a header that can sort a table along with subs/events needed"
  [& {:keys [table-key sort label width]}]
  (let [sort-key (-> sort keys first)
        selected? (rf/subscribe [(sorted-by-sub table-key) sort-key])
        sort-dir (rf/subscribe [(sort-dir-sub table-key)])]
    (fn []
      [:th
       (merge
         (use-style (style-with-width styles/sortable-table-header-cell width))
         {:on-click #(rf/dispatch [(sort-event table-key) sort-key (if (and @selected? (= @sort-dir :asc)) :desc :asc)])})
       [rc/h-box
        :align :center
        :children [label
                   (cond
                     (and @selected? (= @sort-dir :desc)) up-arrow
                     @selected? down-arrow
                     :else double-arrow)]]])))

(defn- header-cell-component?
  "Checks if given component is header-cell or sortable-header-cell"
  [[header-cell-fn]]
  (or (= header-cell-fn header-cell)
      (= header-cell-fn sortable-header-cell)))

(def headers? (partial every? header-cell-component?))

(def table-args
  "Arguments for table component"
  [{:name :headers :required true :type "vector" :validate-fn headers? :description "Vector of header components"}
   {:name :row-data :required true :type "vector" :validate-fn sequential? :description "Collection of vectors of data to be displayed in each row"}
   {:name :loading? :required false :type "boolean" :validate-fn boolean? :description "Whether or not the table data is still loading"}
   {:name :loading-component :required false :type "boolean" :validate-fn loading/loading-circle-component? :description "Whether or not the loading circle component is valid"}])

(def empty-table-message
  "Renders row displaying that no results were found"
  [:div
   (use-style styles/empty-table-message)
   "No results were found"])

(defn- wrap-cell
  "Wraps given data in cell component if not already wrapped"
  [cell-data]
  (if (= cell cell-data)
    cell-data
    [cell
     :content cell-data]))

(defn wrap-row
  "Wraps all row data into cells and row if not passed a row component"
  [cells-data]
  (if (and (vector? cells-data) (= row (first cells-data)))
    cells-data
    [row
     :cells (mapv wrap-cell cells-data)]))

(defn table
  "Renders table"
  [& {:keys [headers row-data loading? loading-component]
      :or   {loading? false loading-component loading/loading-circle-large}
      :as   args}]
  {:pre [(validate-args-macro table-args args "table")]}
  (let [header [(into [header-row] headers)]]
    [layout/column
     :padding 0
     :width "100%"
     :children [(reduce
                  conj
                  [:table (use-style styles/table)]
                  (if loading?
                    header
                    (into
                      header
                      (mapv wrap-row row-data))))
                (cond
                  loading? [layout/row
                            :justify :center
                            :children [[loading-component]]]
                  (empty? row-data) [layout/row-bottom
                                     :padding-top 30
                                     :justify :center
                                     :children [empty-table-message]])]]))

(defn table-statistics
  "Renders message about number of results and which results shown"
  [& {:keys [total first last]}]
  (let [total-label (if (> total 999) "999+" total)]
    [:div
     (use-style styles/table-statistics)
     (str total-label " items found, displaying " first "-" last " of " total-label)]))

(defn table-header
  "Will create either sortable/unsortable header cell depending on :sort key"
  [table-key {:keys [sort] :as header}]
  (into
    [(if sort sortable-header-cell header-cell)]
    (mapcat identity (assoc header :table-key table-key))))

;; Pagination

(defn- page
  [num selected? on-page-change]
  [:div
   (assoc
     (use-style (if selected? styles/active-page-number styles/page-number))
     :on-click
     on-page-change)
   (str (+ 1 num))])

(def arrow-right [:div (use-style styles/arrow-right)])
(def arrow-right-active [:div (use-style styles/arrow-right-active)])
(def arrow-left [:div (use-style styles/arrow-left)])
(def arrow-left-active [:div (use-style styles/arrow-left-active)])

(defn- prev-btn
  [current-page on-change-page]
  (let [hovered? (r/atom false)]
    (fn [current-page on-change-page]
      [:div
       {:on-click (partial on-change-page (- current-page 1))}
       [util/recom-component-with-styles
        rc/h-box
        [:justify :between
         :children [(if @hovered?
                      arrow-left-active
                      arrow-left) [:div "Prev"]]
         :attr {:on-mouse-enter (handler-fn (reset! hovered? true))
                :on-mouse-leave (handler-fn (reset! hovered? false))}]
        styles/prev-page]])))

(defn- next-btn
  [current-page on-change-page]
  (let [hovered? (r/atom false)]
    (fn [current-page on-change-page]
      [:div
       {:on-click (partial on-change-page (+ current-page 1))}
       [util/recom-component-with-styles
        rc/h-box
        [:justify :between
         :children [[:div "Next"] (if @hovered?
                                    arrow-right-active
                                    arrow-right)]
         :attr {:on-mouse-enter (handler-fn (reset! hovered? true))
                :on-mouse-leave (handler-fn (reset! hovered? false))}]
        styles/next-page]])))

(def ellipsis [:div
               (use-style styles/ellipsis)
               "..."])

(defn pagination
  "Renders pagination component given:
    - current-page
    - pages: Vector of page numbers
    - on-page-change: Function to be called on page change"
  [current-page pages on-page-change]
  (let [last-page (last pages)
        make-page #(vector page % (= % current-page) (partial on-page-change %))
        middle-vec (when (< 1 current-page (- last-page 1)) (range (- current-page 1) (+ current-page 2)))]
    [util/recom-component-with-styles
     rc/h-box
     [:justify :center
      :children (cond-> []
                        (< 0 current-page) (conj [prev-btn current-page on-page-change])
                        true (conj (make-page 0))
                        (< 2 current-page) (conj ellipsis)
                        (and (<= current-page 1) (< 1 last-page)) (conj (make-page 1))
                        (and (= current-page 1) (< 2 last-page)) (conj (make-page 2))
                        (not-empty middle-vec) ((fn [page-coll] (reduce #(conj %1 (make-page %2)) page-coll middle-vec)))
                        (and (= (- last-page 1) current-page) (< 0 (- last-page 2))) (conj (make-page (- last-page 2)))
                        (and (<= (- last-page 1) current-page) (< 1 current-page) (< 0 (- last-page 1))) (conj (make-page (- last-page 1)))
                        (< current-page (- last-page 2)) (conj ellipsis)
                        (< 0 last-page) (conj (make-page last-page))
                        (< current-page last-page) (conj [next-btn current-page on-page-change]))]
     styles/pagination]))

;; Actions

(defn- table-action-btn
  "Renders action button to be displayed above table"
  [table-key {:keys [label on-click]}]
  (let [checked-total (rf/subscribe [(checked-total-sub table-key)])
        checked (rf/subscribe [(checked-sub table-key)])]
    (fn []
      [layout/column
       :padding-left 0
       :children [[btn/primary-button
                   :label (str label " (" @checked-total ")")
                   :disabled? (= 0 @checked-total)
                   :on-click #(on-click @checked)]]])))

(defn- check-all
  "Renders checkbox to check all rows in table"
  [table-key]
  (let [checked? (rf/subscribe [(all-checked-sub table-key)])
        data (rf/subscribe [(page-data-sub table-key)])]
    (fn []
      [iv/checkbox
       :model checked?
       :name table-key
       :on-change #(rf/dispatch [(on-check-all-event table-key) @data @checked?])])))

(defn- check-row
  "Renders checkbox for given data row"
  [table-key row-data checked]
  [iv/checkbox
   :model (contains? checked row-data)
   :name table-key
   :on-change #(rf/dispatch [(on-check-event table-key) row-data])])

(defn- row-data-with-checkbox
  "Returns function to return row data with checkbox appended"
  [row-data-fn table-key checked]
  (fn [idx data]
    (into
      [[check-row table-key data checked]]
      (row-data-fn idx data))))

(defn- with-page-index
  "Returns function that will call row-data-fn with correct index based on page"
  [row-data-fn page per-page]
  (fn [idx data]
    (row-data-fn (+ idx (* page per-page)) data)))

(defn- paginated-table
  "Renders paginated table

  Params
   * :table-key      Fully qualified key for table
   * :per-page       Optional value to be used to limit table results
   * :headers        Headers to be sent to table component
   * :row-data-fn    Function that will be given a result and return vector of row data
   * :on-page-change Optional function to be run when page changes
   * :page-map-sub   Keyword for subscription of map of results keyed by page
   * :total          Total results in table
   * :actions        Optional vector of action buttons to display above table"
  [& {:keys [table-key per-page headers row-data-fn on-page-change
             page-map-sub total show-top-statistics? actions loading?]
      :or   {per-page 10 on-page-change #() show-top-statistics? false}}]
  (register-table-actions table-key)
  (register-pagination table-key page-map-sub)

  (let [current-page-data (rf/subscribe [(page-data-sub table-key)])
        page-number (rf/subscribe [(page-number-sub table-key)])
        checked (rf/subscribe [(checked-sub table-key)])]
    (fn [& {:keys [headers row-data-fn per-page on-page-change total
                   show-top-statistics? actions loading?]
            :or   {per-page 10 on-page-change #() show-top-statistics? false}}]
      (let [row-data-fn (if actions (row-data-with-checkbox row-data-fn table-key @checked) row-data-fn)]
        [layout/column
         :padding 0
         :width "100%"
         :children [(when-not (empty? actions)
                      [layout/row-top
                       :padding-bottom 19
                       :children (mapv (partial vector table-action-btn table-key) actions)])
                    (when show-top-statistics?
                      [layout/row-top
                       :padding-bottom 24
                       :children [[table-statistics
                                   :total @total
                                   :first (min @total (+ 1 (* @page-number per-page)))
                                   :last (min @total (+ per-page (* @page-number per-page)))]]])
                    [table
                     :headers (into (if-not (empty? actions) [[header-cell
                                                               :label [check-all table-key]
                                                               :width 3.7]] []) headers)
                     :loading? (if loading? @loading? false)
                     :row-data (vec (map-indexed (with-page-index row-data-fn @page-number per-page) @current-page-data))]
                    (when (not-empty @current-page-data)
                      [layout/row
                       :padding-top 24
                       :padding-bottom 34
                       :children [[table-statistics
                                   :total @total
                                   :first (min @total (+ 1 (* @page-number per-page)))
                                   :last (min @total (+ per-page (* @page-number per-page)))]]])
                    (when (not-empty @current-page-data)
                      [pagination
                       @page-number
                       (take (js/Math.ceil (/ @total per-page)) (range))
                       #(do (on-page-change %)
                            (rf/dispatch [(set-page-event table-key) %]))])]]))))

(defn frontend-pagination-table
  "Renders a paginated table in which all data is available in the frontend.
  Since all the data is available, sorting is also allowable."
  [& {:keys [table-key data-sub per-page headers initial-sort-col initial-sort-dir
             actions loading?]
      :or   {per-page 10 initial-sort-dir :desc}
      :as   args}]
  (let [sorters (reduce merge (keep :sort headers))]
    (register-sortable-headers
      table-key
      (or initial-sort-col (-> sorters keys first))
      initial-sort-dir)

    (register-frontend-pagination table-key data-sub sorters)

    (let [total (rf/subscribe [(total-sub table-key)])]
      (fn [& {:keys [table-key headers actions]
              :as   args}]
        (into
          [paginated-table]
          (mapcat
            identity
            (assoc
              args
              :page-map-sub
              (page-map-sub table-key)
              :total
              total
              :headers
              (mapv (partial table-header table-key) headers))))))))

(defn backend-pagination-table
  "Creates table with backend pagination capabilities

  Params
    * :table-key      Fully qualified key for table
    * :per-page       Optional value to be used to limit table results
    * :fetch-url      URL to fetch data. Must accept limit/offset query params and return map of :total and :results
    * :fetch-params   Optional map of params to be sent with fetch request
    * :headers        Headers to be sent to table component
    * :row-data-fn    Function that will be given a result and return vector of row data
    * :initial-page   Optional page to begin the table on when loaded
    * :on-page-change Optional additional callback to be called when page is changed"
  [& {:keys [table-key fetch-url fetch-params headers initial-page
             on-page-change]
      :or   {initial-page 0 on-page-change #()}
      :as   args}]
  (register-backend-pagination table-key args)
  (rf/dispatch [(pagination-event table-key) initial-page (when fetch-params @fetch-params)])
  (rf/dispatch [(set-page-event table-key) initial-page])

  (let [total (rf/subscribe [(total-sub table-key)])
        loading? (rf/subscribe [(loading-sub table-key)])]
    (into
      [paginated-table]
      (mapcat
        identity
        (assoc
          args
          :headers
          (mapv (partial table-header table-key) headers)
          :page-map-sub
          (page-map-sub table-key)
          :total
          total
          :loading?
          loading?
          :on-page-change
          #(do (rf/dispatch [(pagination-event table-key) % (when fetch-params @fetch-params)])
               (rf/dispatch [(set-page-event table-key) %])
               (on-page-change %)))))))
