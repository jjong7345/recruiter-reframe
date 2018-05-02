(ns recruit-app.styles
  (:require [stylefy.core :as stylefy]
            [recruit-app.util.img :as img]))

(def content-width "1000px")

;; Fonts
(def font-light "LibreFranklin-Light")
(def font-regular "LibreFranklin-Regular")
(def font-bold "LibreFranklin-Medium")

;; Colors
(def black "#000000")
(def white "#fff")
(def light-grey "#cccccc")
(def medium-grey "#888888")
(def darker-grey "#979797")
(def background-grey "#F5F5F5")
(def text-link-teal "#09AEBC")
(def text-link-active "#0994A0")
(def highlight-teal "#eafeff")
(def bright-teal "#00C0D1")
(def dark-teal "#01828C")
(def dark-grey "#333333")
(def charcoal-light "#666666")
(def red "#ea0000")
(def promotion-orange "#f5a623")
(def error-red "#f8d8d8")

;; Layout
(def column {})
(def row {:width "100%"})
(def wrapping-row (merge row {:flex-wrap "wrap!important"}))
(def wrapping-row-child {:margin "6px 0"})
(def wrapping-row-with-children (merge wrapping-row {:margin "-6px 0"}))
(def page-content-container {:background-color background-grey})
(def page-content {:width      content-width
                   :min-height "500px"
                   :margin     "0 auto"})

(def header-height "50px")
(def footer-height "186px")

(def bordered {:border        (str "1px solid " light-grey "!important")
               :border-radius "2px!important"})

(def ellipsis-text {:white-space   "nowrap"
                    :overflow      "hidden"
                    :text-overflow "ellipsis"})

;; Box
(def box {:background-color white
          :padding          "12px 12px 30px"
          :width            "100%"})

(def box-header {:margin        "0 0 6px"
                 :line-height   "normal"
                 :border-bottom (str "solid 1px " light-grey)})

(def box-header-no-underline (merge box-header {:border-bottom "none"}))

;; Headers
(def header-1 {:font-family font-light
               :font-size   "36px"
               :line-height "36px"
               :color       black})
(def header-2 {:font-family font-light
               :font-size   "30px"
               :line-height "30px"
               :color       black})
(def header-3 {:font-family font-light
               :font-size   "24px"
               :line-height "24px"
               :color       black})
(def header-4 {:font-family font-light
               :font-size   "20px"
               :line-height "20px"
               :color       dark-grey})
(def header-5 {:font-family    font-light
               :font-size      "12px"
               :line-height    "12px"
               :color          dark-grey
               :text-transform "uppercase"
               :margin-bottom  "3px"})
(def form-header {:font-family font-regular
                  :font-size   "14px"
                  :color       dark-grey
                  :margin      "6px 0"})
(def info-header {:font-family    font-regular
                  :font-size      "12px"
                  :color          dark-grey
                  :text-transform "uppercase"})
(def section-header {:font-family font-regular
                     :font-size   "14px"
                     :line-height "14px"
                     :color       black})
(def page-header-container {:width      "100%"
                            :position   "relative"
                            :min-height "104px"
                            :background white})
(def page-header {:width  content-width
                  :margin "30px auto"})
(def sub-header {:font-size   "14px"
                 :line-height "14px"
                 :font-family font-light
                 :color       charcoal-light})

;; Typography
(def body-copy-grey {:color light-grey})
(def modal-copy {:font-size   "16px"
                 :line-height "16px"
                 :font-family font-regular
                 :color       black})
(def body-copy-light (merge modal-copy {:font-family font-light
                                        :line-height "1.25"}))
(def error-copy {:font-size   "14px"
                 :font-family font-light
                 :color       red})

;; Links
(def hyperlink {:cursor        "pointer"
                :font-family   font-regular
                :font-size     "14px"
                :line-height   "14px"
                :color         (str text-link-teal "!important")
                :max-width     "100%"
                ::stylefy/mode {:hover {:color (str text-link-active "!important")}}})
(def hyperlink-12 (merge
                    hyperlink
                    {:font-size   "12px"
                     :line-height "12px"}))

(def hyperlink-14 (merge
                    hyperlink
                    {:font-size   "14px"
                     :line-height "14px"}))
(def breadcrumb-link (merge
                       hyperlink
                       {:font-size   "12px"
                        :line-height "20px"}))

;; Form
(def form-label {:text-transform "uppercase"
                 :font-family    font-regular
                 :font-size      "12px"
                 :line-height    "12px"
                 :letter-spacing "0.5px"
                 :color          charcoal-light})

(def input-holder {:width "100%"})
(def input (merge bordered {:font-family font-light
                            :font-size   "14px"}))
(def standard-dropdown-input-holder {:width "490px"
                                     :margin "0px 20px 0px 0px"})
(def range-dropdown-input-holder {:width "180px"
                                  :margin "0px 0px 0px 0px"})
(def standard-dropdown-input {:border        (str "1px solid " darker-grey "!important")
                              :border-radius "2px!important"
                              :font-family   font-light
                              :font-size     "16px"})
(def input-text (merge
                  input
                  {:height  "30px"
                   :padding "7px 12px 6px"}))
(def input-text-tall (merge
                       input-text
                       {:height "40px"}))
(def input-textarea (merge
                      input-text
                      {:color         dark-grey
                       ::stylefy/mode {:disabled {:background-color white}}}))
(def character-limit {:font-size "11px"
                      :color     "#959595"})
(def input-error {:color       red
                  :font-family font-light
                  :font-size   "12px"
                  :margin      "6px 0 0"})
(def input-flag {:font-family font-light
                 :font-size   "12px"
                 :color       charcoal-light})
(def single-dropdown (merge
                       input
                       {:width      "100%"
                        :max-height "32px"}))
(def range-dropdown (merge
                         standard-dropdown-input
                         {:width  "180px"
                          :height "42px"}))
(def standard-dropdown (merge
                         standard-dropdown-input
                         {:width  "490px"
                          :height "42px"}))
(def secondary-dropdown (merge
                      standard-dropdown-input
                      {:width  "490px"
                       :height "42px"
                       :background "#cccccc"}))
(def tertiary-dropdown (merge
                      standard-dropdown-input
                      {:width  "180px"
                       :height "32px"}))
(def checkbox-container {:display             "block"
                         :font-size           "14px"
                         :position            "relative"
                         :padding-left        "20px"
                         :-webkit-user-select "none"
                         :-moz-user-select    "none"
                         :-ms-user-select     "none"
                         :user-select         "none"})
(def checkbox (merge
                bordered
                {:position "absolute"
                 :opacity  "0"
                 :width    "15px"
                 :height   "15px"
                 :margin   "2px 12px 0 0!important"
                 :cursor   "pointer"}))
(def checkbox-label {:font-family font-light
                     :font-size   "14px"
                     :color       black})
(def checkmark {:position      "absolute"
                :top           "3px"
                :left          "0"
                :height        "14px"
                :width         "14px"
                :border        (str "1px solid " light-grey)
                :cursor        "pointer"
                ::stylefy/mode {:after {:content           "''"
                                        :position          "absolute"
                                        :display           "none"
                                        :left              "4px"
                                        :top               "1px"
                                        :width             "5px"
                                        :height            "8px"
                                        :border-style      "solid"
                                        :border-color      "black"
                                        :border-width      "0 2px 2px 0"
                                        :-webkit-transform "rotate(45deg)"
                                        :-ms-transform     "rotate(45deg)"
                                        :transform         "rotate(45deg)"}}})
(def toggle {:width  "28px"
             :height "17px"
             :margin "0 12px 0 0"
             :cursor "pointer"})
(def toggle-on (merge
                 toggle
                 {:background (str "url(" (img/url :toggle-on) ")")}))
(def toggle-off (merge
                  toggle
                  {:background (str "url(" (img/url :toggle-off) ")")}))

(def slider {:-webkit-appearance "none"
             :position           "relative"
             :height             "1px"
             :width              "100%"
             :background         "transparent"})

(def slider-label {:color     black
                   :font-size "12px"})

(def multirange {:position "relative"})

(def ra-typeahead {:position "relative"})

(def inline-display {:margin "6px 0"})
(def inline-display-label {:font-family font-light
                           :font-size   "14px"
                           :color       charcoal-light
                           :margin      "0 6px 0 0"})
(def inline-display-value {:font-family font-regular
                           :font-size   "14px"
                           :color       black})
(def removable-option {:background-color white
                       :padding          "3px 6px"
                       :border-radius    "2px"
                       :height           "20px"})
(def removable-option-text (merge
                             ellipsis-text
                             {:font-size   "11px"
                              :line-height "11px"
                              :font-family font-regular
                              :color       dark-grey
                              :max-width   "400px"}))
(def input-with-dropdown-holder {:position "relative"
                                 :width    "100%"})

;; Buttons
(def button {:padding       "11px 30px 10px!important"
             :min-width     "130px!important"
             :height        "40px!important"
             :font-family   (str font-regular "!important")
             :font-size     "16px!important"
             :line-height   "16px!important"
             :border-radius "2px!important"
             :margin        "0!important"})
(def primary-button (merge
                      button
                      {:background    (str bright-teal "!important")
                       :color         (str white "!important")
                       ::stylefy/mode {:hover    {:background (str dark-teal "!important")
                                                  :color      (str white "!important")}
                                       :disabled {:background (str light-grey "!important")}}}))
(def secondary-button (merge
                        button
                        {:background    (str white "!important")
                         :color         (str bright-teal "!important")
                         :border        (str "1px solid " bright-teal "!important")
                         ::stylefy/mode {:hover {:background (str dark-teal "!important")
                                                 :color      (str white "!important")}}}))
(def transaction-button {:height      "40px!important"
                         :padding     "11px 30px 10px!important"
                         :font-size   "16px!important"
                         :line-height "16px!important"})
(def primary-transaction-button (merge
                                  primary-button
                                  transaction-button))
(def secondary-transaction-button (merge
                                    secondary-button
                                    transaction-button))

(def dashboard-button {:height      "30px!important"
                       :min-width   "100px!important"
                       :padding     "4px 20px!important"
                       :font-size   "14px!important"
                       :line-height "0!important"})
(def primary-dashboard-button (merge
                                primary-button
                                dashboard-button))
(def secondary-dashboard-button (merge
                                  secondary-button
                                  dashboard-button))
(def filter-button (merge
                     button
                     {:height           "24px"
                      :padding          "0 15px"
                      :font-family      font-regular
                      :font-size        "14px"
                      :line-height      "0"
                      :background-color (str bright-teal "!important")
                      :min-width        0}))


;; Job Promotions
(def job-promotion-element {:width  "200px"
                            :height "30px"})
(def job-promotion-btn (merge
                         primary-button
                         bordered
                         job-promotion-element
                         {:font-size     "14px!important"
                          :line-height   0
                          :border-radius 0}))
(def promoted-job-count (merge
                          job-promotion-element
                          {:background-color background-grey
                           :text-align       :center
                           :border-top       (str "1px solid " light-grey)
                           :border-left      (str "1px solid " light-grey)
                           :border-right     (str "1px solid " light-grey)
                           :padding          "8px 12px 6px"
                           :font-size        "13px"
                           :font-family      font-regular
                           :color            charcoal-light}))

;; Table
(def table {:background-color white
            :min-width        "100%"})
(def table-header-row {})
(def table-filter-bar {:width            "100%"
                       :height           "48px"
                       :padding          "12px"
                       :background-color white
                       :border-top       (str "2px solid " background-grey)
                       :border-bottom    (str "2px solid " background-grey)})
(def table-header-cell {:font-family    (str font-light "!important")
                        :font-size      "12px!important"
                        :text-transform "uppercase"
                        :padding        "12px!important"})
(def sortable-table-header-cell (merge
                                  table-header-cell
                                  {:cursor "pointer"}))
(def sort-arrow {:width  "9px"
                 :margin "0 0 0 8px"})
(def down-sort-arrow (merge
                       sort-arrow
                       {:height              "6px"
                        :background-size     "9px 6px"
                        :background-position "0 100%"
                        :background-image    (str "url(" (img/url :down-sort-arrow) ")")}))
(def up-sort-arrow (merge
                     sort-arrow
                     {:height           "6px"
                      :background-size  "9px 6px"
                      :background-image (str "url(" (img/url :up-sort-arrow) ")")}))
(def double-sort-arrow (merge
                         sort-arrow
                         {:width            "9px"
                          :height           "15px"
                          :background-size  "9px 15px"
                          :background-image (str "url(" (img/url :double-sort-arrow) ")")}))
(def table-cell-text {:font-family font-regular
                      :font-size   "14px"
                      :line-height "normal"
                      :margin      "0"})
(def table-cell (merge
                  {:padding        "12px"
                   :vertical-align "top"}
                  table-cell-text))
(def table-cell-link (merge
                       hyperlink
                       table-cell-text
                       {:width "100%"}))
(def table-row {:height           "36px"
                :border           (str "2px solid " background-grey)
                :background-color white
                ::stylefy/mode    {:hover {:background-color highlight-teal}}})
(def clickable-table-row (merge table-row {:cursor "pointer"}))
(def empty-table-message {:font-family font-regular
                          :font-size   "16px"
                          :color       black})
(def table-statistics {:font-size   "14px"
                       :line-height "14px"
                       :font-family font-regular
                       :color       dark-grey})
(def multiline-content {:font-size   "14px"
                        :color       black
                        :font-family font-regular
                        :line-height "20px"})
(def multiline-top (merge multiline-content {}))
(def multiline-bottom (merge multiline-content {:color medium-grey}))

;; Projects
(def create-project-bar (merge
                          table-row
                          bordered
                          {:height  "40px"
                           :padding "0 12px"
                           :cursor  "pointer"
                           :width   "100%"}))

;; Pagination
(def pagination {:padding    "29px 0 0"
                 :border-top (str "2px solid " white)})
(def pagination-btn-hover {:color            white
                           :background-color dark-teal})
(def pagination-btn-active {:color            white
                            :background-color light-grey})
(def pagination-btn (merge
                      bordered
                      {:font-size        "14px"
                       :line-height      "14px"
                       :font-family      font-regular
                       :background-color white
                       :color            dark-grey
                       :cursor           "pointer"
                       ::stylefy/mode    {:hover pagination-btn-hover}}))
(def page-number (merge
                   pagination-btn
                   {:width      "55px"
                    :height     "36px"
                    :text-align "center"
                    :padding    "10px 0 9px"
                    :margin     "0 3px"}))
(def active-page-number (merge
                          page-number
                          pagination-btn-active
                          {::stylefy/mode {:hover pagination-btn-active}}))
(def ellipsis (merge
                page-number
                {::stylefy/mode {:hover {}}}))
(def next-page (merge
                 pagination-btn
                 {:padding "10px 12px 9px 14px"
                  :margin  "0 0 0 27px"}))
(def prev-page (merge
                 pagination-btn
                 {:padding "10px 14px 9px 12px"
                  :margin  "0 27px 0 0"}))
(def arrow {:background-size "9px 12px"
            :width           "9px"
            :height          "12px"})
(def arrow-right (merge
                   arrow
                   {:background-image (str "url(" (img/url :arrow-right) ")")
                    :margin           "1px 0 0 12px"}))
(def arrow-left (merge
                  arrow
                  {:background-image (str "url(" (img/url :arrow-left) ")")
                   :margin           "1px 12px 0 0"}))
(def arrow-right-active (merge
                          arrow-right
                          {:background-image (str "url(" (img/url :arrow-right-active) ")")}))
(def arrow-left-active (merge
                         arrow-left
                         {:background-image (str "url(" (img/url :arrow-left-active) ")")}))

;; Icons
(def icon {:width    "9.5px!important"
           :height   "9.5px!important"
           :position "relative"})
(def clickable-icon (merge icon {:cursor "pointer"}))
(def icon-img {:position "absolute"
               :top      0})
(def pencil-icon-img (merge icon-img {}))
(def stroke-left (merge icon-img {}))
(def stroke-right (merge icon-img {:transform "rotate(90deg)"}))
(def promote-icon-img (merge icon-img {}))
(def modal-x {:width  "18px!important"
              :height "18px!important"})
(def modal-x-icon (merge clickable-icon modal-x))
(def modal-x-img (merge icon-img modal-x))
(def person-icon-img {:width  "18px"
                      :height "15px"})

;;loading animation
(def loading-cover {:position         "absolute"
                    :width            "100%"
                    :height           "100%"
                    :min-height       "300px"
                    :background-color white
                    :opacity          "0.8"
                    :z-index          "10"})
(def loading-page {:min-height (str "calc(100% - " header-height " - " footer-height ")")})
(def loading-circle-tiny {:width "23px"})
(def loading-circle-small {:width "32px"})
(def loading-circle-large {:width "200px"})
(def loading-overlay-wrapper {:width  "100%"
                              :height "100%"})
(def primary-button-loader (merge
                             loading-circle-tiny
                             {:margin     "0 auto"
                              :max-height "19px"}))

;; Miscellaneous
(def flag (merge
            bordered
            {:font-family    font-regular
             :font-size      "10px"
             :line-height    "14px"
             :text-transform "uppercase"
             :color          charcoal-light
             :text-align     "center"
             :padding        "3px 5px"}))
(def info-box {:background-color background-grey
               :padding          "8px 12px 11px"
               :min-width        "200px"})
(def info-box-display {:font-family font-regular
                       :font-size   "13px"
                       :color       charcoal-light})
(def dashboard-mobile-divider {:width         "265px"
                               :margin        "0 auto"
                               :border-bottom (str "1px solid " light-grey)})
(def overlay-holder {:position "relative"
                     :width    "100%"})
(def overlay (merge
               bordered
               {:width      "600px"
                :position   "absolute"
                :top        "100px"
                :background "#FFF"
                :padding    "24px"
                :margin     "0 200px"
                :z-index    10}))

;; Alerts
(def alert {:padding     "8px 12px 10px"
            :width       "100%"
            :font-size   "14px"
            :font-family font-light
            :color       black})
(def error-alert (merge alert {:background-color error-red}))
