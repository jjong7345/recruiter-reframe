(ns recruit-app.util.data-table
  (:require [re-com.core :as rc]))


(defn third
  [col]
  (nth col 2))


(defn data-row [row get-fns widths attrs]
  (let [member (:jobseeker row)]
    ^{:key (:count row)}
    [rc/h-box
     :class "rc-div-table-row"
     :attr (attrs row)
     :children (mapv (fn [get-fn width] [rc/label :label (get-fn member) :width width]) get-fns widths)]))

(defn data-table
  " * cols : vector of column-infos
      * column-info: vector of column info
         * title  :  Column title
         * get-fn :  Function that takes a data item and returns value
         * width  :  Column width
    * rows : Map of table data for row
    * attrs (optional) : fn that takes the row and returns an :attr map to be added to each row"
  ([cols rows]
    (data-table cols rows {}))
  ([cols rows attrs]
   (when rows
     (let [titles (map first cols)
           get-fns (map second cols)
           widths (map third cols)]

       [rc/v-box
        :class "rc-div-table"
        :children [[rc/h-box
                    :class "rc-div-table-header"
                    :children (mapv (fn [%1 %2] [rc/label :label %1 :width %2]) titles widths)]
                   [rc/v-box
                    :children (mapv (fn [row] [data-row row get-fns widths attrs]) rows)]]]))))
