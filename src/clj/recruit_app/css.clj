(ns recruit-app.css
  (:require [garden.def :refer [defstyles]]
            [garden.selectors :as s]
            [garden.stylesheet :as st]
            [recruit-app.util.urls :refer [img-url]]
            [garden.def :refer [defrule defkeyframes]]))

(s/defpseudoelement -webkit-slider-thumb)
(s/defpseudoelement -moz-range-thumb)
(s/defpseudoelement -moz-range-track)
(s/defpseudoelement -ms-range-thumb)
(s/defpseudoelement -ms-range-track)
(s/defpseudoelement -webkit-slider-runnable-track)
(s/defpseudoelement -webkit-scrollbar)
(s/defpseudoelement -webkit-scrollbar-thumb)
(s/defpseudoelement before)
(s/defpseudoelement after)
(s/defpseudoelement placeholder)
(s/defpseudoelement -webkit-input-placeholder)

(def content-width "1000px")
(def ladders-teal "#3fb2bb")
(def ladders-turquoise-blue "#00c0d1")
(def ladders-teal-dark "#2f8c92")
(def ladders-greeny-blue "#3fb2bb")
(def white "#fff")
(def ladders-black "#231f20")
(def charcoal "#333333")
(def charcoal-light "#666666")
(def dark-grey "#4D4D4B")
(def darker-grey "#979797")
(def grey "#aaa")

(def gray-90 "#e5e5e5")
(def light-grey "#999")
(def lighter-grey "#e3e3e3")
(def lightest-grey "#f4f4f4")
(def even-lighter-grey "#f8f8f8")
(def dark-white "#fafafa")
(def ladders-bg-grey "#f5f5f5")
(def ladders-dark-tan "#e3e3e3")
(def ladders-orange "#ef8f1c")
(def ladders-text-dark "#444444")
(def error-red "#ed4e3d")
(def alert-success "#b4e5b7")
(def alert-error "#f8d7da")
(def bright-teal "#00C0D1")
(def orange "#f87b31")

(def ladders-font "LibreFranklin-Light")
(def ladders-font-bold "LibreFranklin-Regular")
(def ladders-font-boldest "LibreFranklin-Medium")

(def none "none")
(def relative "relative")
(def absolute "absolute")

(def header-height "50px")
(def footer-height "186px")

(defn dropdown-container-results
  [font-size]
  [:.chosen-container
    [:.chosen-results
     [:li {:font-family ladders-font
           :font-size   font-size
           :color "#000000"}
      [:&:hover {:background "#cccccc"
                 :color      white}]
      [:&.highlighted {:background bright-teal
                       :color      white}]]]])
(defn rc-dropdown
  ([font-size]
   (rc-dropdown font-size nil))
  ([font-size background]
   (rc-dropdown font-size background nil))
  ([font-size background font-style]
    [:.rc-dropdown
      [:.chosen-single {:height  "40px"
                        :padding "8px 0px 8px 4px"
                        :background background}
        [:span {:padding     "6px 4px 6px 10px"
                :height      "40px"
                :max-height  "28px"
                :font-size   font-size
                :font-style  font-style
                :line-height "14px"
                :color       "#000000"}]]]))

(defmacro defbreakpoint [name media-params]
  `(defn ~name [& rules#]
     (st/at-media ~media-params
                  [:& rules#])))

(defkeyframes slideInFromLeftFirst
              [["0%" {:transform "translateX(-100%)"}]
               ["100%" {:transform "translateX(0)"}]])

(defkeyframes slideInFromLeftSecond
              [["0%" {:transform "translateX(-70%)"}]
               ["100%" {:transform "translateX(0)"}]])

(defkeyframes slideInFromLeftThird
              [["0%" {:transform "translateX(-70%)"}]
               ["100%" {:transform "translateX(0)"}]])

(defkeyframes progressing
              [["0%" {:opacity "0.2"}]
               ["50%" {:opacity "0.9"}]
               ["100%" {:opacity "0.2"}]])

(defstyles
  screen
  {:vendors ["webkit"]}

  [slideInFromLeftFirst slideInFromLeftSecond slideInFromLeftThird progressing

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+ Web Fonts
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [(st/at-import "https://fast.fonts.net/lt/1.css?apiType=css&c=cd2d8db6-5f56-4ddc-a945-bc5a906f2e72&fontids=674411,694015,694021")]
   [(st/at-font-face {:font-family   ladders-font
                      :src           (str "local('Libre Franklin Light'), local('LibreFranklin-Light'), url(https://fonts.gstatic.com/s/librefranklin/v2/1_DGDtljMiPWFs5rl_p0yGISN6_59ECOhaitw-i87uk.woff2) format('woff2')")
                      :font-weight   300
                      :font-style    "normal"
                      :unicode-range "U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2212, U+2215"})]

   [(st/at-font-face {:font-family   ladders-font-bold
                      :src           (str "local('Libre Franklin'), local('LibreFranklin-Regular'), url(https://fonts.gstatic.com/s/librefranklin/v2/PFwjf3aDdAQPvNKUrT3U77v_weys7n7jZxU_6MdmmbI.woff2) format('woff2')")
                      :font-weight   400
                      :font-style    "normal"
                      :unicode-range "U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2212, U+2215"})]

   [(st/at-font-face {:font-family   ladders-font-boldest
                      :src           (str "local('Libre Franklin Medium'), local('LibreFranklin-Medium'), url(https://fonts.gstatic.com/s/librefranklin/v2/1_DGDtljMiPWFs5rl_p0yH0EyttSSCsW3UCOJCoIBvw.woff2) format('woff2')")
                      :font-weight   500
                      :font-style    "normal"
                      :unicode-range "U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2212, U+2215"})]

   (defbreakpoint small-screen
                  {:min-width "320px"
                   :max-width "767px"})

   (defbreakpoint large-screen
                  {:min-width "768px"})

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+ General Styles
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app {:background  ladders-bg-grey
           :color       charcoal
           :font-size   "20px"
           :font-family ladders-font
           :height      "100%"}
    [:button {:outline "none"}]
    [:a {:color  ladders-teal
         :height "inherit"}
     [:&:hover {:text-decoration "none"
                :color           ladders-teal-dark}]]
    [:.btn {:color         white
            :background    ladders-teal
            :border-radius "0"
            :border        none
            :align-self    "flex-start"
            :padding       "10px"}
     [:&:hover {:color      white
                :background ladders-teal-dark}]
     [:&:focus {:color      white
                :background ladders-teal-dark}]]

    [:.btn-border {:color         charcoal
                   :background    white
                   :border-radius "0"
                   :border        "solid 1px"
                   :border-color  ladders-teal
                   :padding       "9px"
                   :margin-right  "10px"}
     [:&:hover {:background white
                :color      charcoal}]
     [:&:focus {:background white
                :color      charcoal}]]

    [:.divider {:width         "100%"
                :height        "1px"
                :background    white
                :margin-bottom "10px"}]

    [:.btn-border {:width        "auto"
                   :background   "transparent"
                   :padding      "16px 0"
                   :color        ladders-teal
                   :border       "solid 1px"
                   :border-color ladders-teal}
     [:&:hover {:color      ladders-teal
                :background "transparent"}]]
    [:.holder
     [:.labels {:margin-bottom "10px"}]]
    [:.error-msg {:font-size   "12px"
                  :font-family ladders-font-bold
                  :color       error-red}]

    [:.form-control {:border (str "1px solid " darker-grey)}
     [:&:focus :&:active {:border     (str "1px solid  " ladders-teal)
                          :box-shadow none}]
     [:&.error {:border (str "1px solid " error-red)}]]
    [:.rc-dropdown
     [:&:focus :&:active {:box-shadow none
                          :outline    none}]
     [:.chosen-single {:height "40px"}
      [:span {:padding     "2px 4px"
              :font-size   "16px"
              :font-weight "100"
              :color       "#444444"}]]
     [:&.error {:border (str "1px solid " error-red)}]
     [:a {:border             (str "1px solid " lighter-grey)
          :-webkit-box-shadow none}]]
    [:.chosen-container-active.chosen-container-active
     [:.chosen-single {:box-shadow    none
                       :border        (str "1px solid " bright-teal)
                       :border-bottom white}]
     [:.chosen-drop {:box-shadow    none
                     :border        (str "1px solid " bright-teal)
                     :border-top    none
                     :border-radius 0}]]
    [:.chosen-container [:.chosen-results [:li.highlighted {:background bright-teal}]]]
    [:.quill-holder {:position relative}
     [:&.error [:.ql-container.ql-snow {:border-color error-red}]
      [:.ql-toolbar.ql-snow {:border-color        error-red
                             :border-bottom-color "#ccc"
                             :border-width        "1px"}]]]
    [:.ql-toolbar {:background "#f0f0f0"}]
    [:.ql-editor {:background  white
                  :height      "500px"
                  :font-size   "16px"
                  :font-family ladders-font}
     [:&focus {:border (str "1px solid " ladders-teal)}]
     [:&.ql-blank
      [:&:before {:font-weight "300"
                  :color       "#aaa"
                  :opacity     "0.8"
                  :font-family ladders-font-bold}]]]


    [:.ql-blank {:font-weight "100"
                 :font-style  "italic"}
     [(s/& -webkit-input-placeholder) {:font-weight "100"
                                       :font-style  "italic"
                                       :color       "red"}]]
    [:.rc-typeahead-suggestions-container {:width      "490px"
                                           :border     (str "1px solid " ladders-teal)
                                           :padding    "0 5px"
                                           :font-size  "14px"
                                           :background white
                                           :border-top none
                                           :color      "#444"
                                           :z-index    20}

     [:.rc-typeahead-suggestion {:background white}
      [:&.active {:background lightest-grey}]]]

    [:.check-holder {:position "relative"}
     [:.info {:margin-left "10px"
              :font-family ladders-font-bold
              :font-size   "14px"
              :font-weight 300}]
     [:span {:width      "16px"
             :height     "16px"
             :cursor     "pointer"
             :position   "absolute"
             :top        0
             :left       0
             :background white
             :border     "1px solid #aaaaaa"}
      [:&:after {:content      "''"
                 :width        "9px"
                 :height       "5px"
                 :position     "absolute"
                 :top          "3px"
                 :left         "3px"
                 :border       (str "2px solid " ladders-teal)
                 :border-top   none
                 :border-right none
                 :background   "transparent"
                 :opacity      0
                 :transform    "rotate(-45deg)"}]]
     [(s/input (s/attr= :type :checkbox)) {:visibility "hidden"}
      [:&:checked [:+ [:span:after {:opacity 1}]]]]]
    [:.popover {:border-radius 0
                :font-family   ladders-font-bold}
     [:.popover-content {:padding 0}]]
    [:.tooltip {:font-family ladders-font}]




    [:.ellipsis {
                 :text-overflow "ellipsis"
                 :overflow      "hidden"
                 :white-space   "nowrap"}]


    [(s/input placeholder) (s/textarea placeholder) {:font-weight "100"
                                                     :font-style  "italic"
                                                     :color       "#999999"}]
    [:.dropdown {:height "auto"}]

    [:th {:font-size   "13px"
          :font-family ladders-font-boldest
          :font-weight "400"
          :line-height "24px"
          :border-top  "none"}
     [:&.day-enabled {:font-size "10px"}]                   ;; for re-com datepicker
     [:&.sort-col {:background-color "rgba(255,255,255,0.6)"}]
     [:.col-header {:color       "#000"
                    :font-weight "400"}]
     [:.arrow-down-ra :.arrow-up-ra {:width        "0px"
                                     :height       "0px"
                                     :border-left  "6px solid transparent"
                                     :border-right "6px solid transparent"
                                     :margin       "0 0 0 5px"}
      [:&.disabled {:border-top    "6px solid transparent"
                    :border-bottom "6px solid transparent"}]]
     [:.arrow-down-ra {:border-top    "6px solid #9a9a9a"
                       :border-bottom "6px solid transparent"
                       :position      "relative"
                       :top           "8px"}]
     [:.arrow-up-ra {:border-top    "6px solid transparent"
                     :border-bottom "6px solid #9a9a9a"
                     :padding       "2px 0 0"}]]

    [:.checkbox-container
     [(s/input (s/attr= :type :checkbox))
      [:&:checked [:+ [:.checkmark:after {:display "block"}]]]]
     [(s/input (s/attr= :type :checkbox))
      [:&:disabled [:+ [:.checkmark {:cursor "not-allowed"}]]]]]
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Alerts
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.alerts {:width    "100%"
               :position absolute
               :top      header-height}
     [:.ra-alert {:height      "43px"
                  :font-size   "16px"
                  :text-align  "center"
                  :font-family ladders-font-bold
                  :z-index     10}
      [:&.success {:background-color alert-success}]
      [:&.error {:background-color alert-error}]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Header
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.logo {:width  "162px"
             :height "30px"
             :margin "10px 30px 10px 0"}]
    [:.lo-header-holder {:background-color "#231f20"
                         :width            "100%"}
     [:.lo-header {:margin "0 auto"
                   :width  content-width}]
     [:.logo-holder {:margin-left "15px"}]

     [:.logo [:a [:img {:width "162px"}]]
      {:float         "left"
       :margin-bottom "9px"}]]
    [:.login-button {:margin "15px 19px 0 auto"}]
    [:.login-button [:a {:font-size      "16px"
                         :letter-spacing "1px"
                         :padding-right  "4px"
                         :color          "#aaaaaa"}]]
    [:.chosen-container-single
     [:.chosen-single {:border        none
                       :box-shadow    none
                       :border-radius "0"}]]
    [:.header {:font-family ladders-font-bold
               :background  ladders-black
               :height      header-height}
     (small-screen [:& {:display "none"}])
     [:&.mobile
      (small-screen [:& {:flex-flow "column nowrap" :display "flex"}])
      (large-screen [:& {:display "none"}])]
     [:.header-content {:background ladders-black
                        :color      white
                        :width      content-width
                        :margin     "0 auto"}]
     [:.left-menu {:margin "0 0 0 15px"}]
     [:.left-menu-dropdown {:margin-left "-15px"}]
     [:.right-menu {:margin-left "auto"}]
     [:img
      [:&.profile-picture {:width     "21px"
                           :height    "21px"
                           :clip-path "circle(50% at 50% 50%)"
                           :overflow  "hidden"
                           :margin    "0 20px 0 0"}
       (small-screen [:& {:width  "25px"
                          :height "25px"}])]]
     [:.home-icon {:color           white
                   :text-decoration none}]
     [:.menu-holder {:z-index 100
                     :width   "100px"}
      [:&.superuser {:width "125px"}
       [:.arrow-up-ra :.arrow-down-ra {:left "95px"}]
       (small-screen {:width "200px"})]
      (small-screen {:width            "200px"
                     :height           "50px"
                     :transition       "height 0.5s"
                     :background-color "#231f20"})
      [:&.account-menu {:width "auto"}
       (small-screen {:width "200px" :background-color ladders-black :align-items "flex-start"})
       (large-screen [:& {:align-items "flex-end"}])
       [:&.active {:background ladders-black}
        [:.profile-name-holder {:background dark-grey}]]
       [:.dropdown {:margin      "0"
                    :width       "286px"
                    :padding     "10px 0 0"
                    :font-family ladders-font-bold}
        (small-screen {:width "200px"})
        [:.btn {:background ladders-teal
                :width      "260px"
                :margin     "26px 13px"
                :font-size  "16px"}
         (small-screen {:width "175px"})
         [:&:hover {:color      white
                    :background ladders-teal-dark}]]]
       [:.menu-stripe {:width "266px"}
        (small-screen {:width "180px"})]
       [:.profile-name-holder
        (small-screen {:width "200px"})
        [:button {:padding "10px 20px"}
         (small-screen [:& {:padding "10px"}])]
        [:a.upload-a-photo {:height          header-height
                            :background      (str ladders-orange " url(" (img-url :exclamation) ") no-repeat 92% 50%")
                            :color           white
                            :text-decoration none
                            :line-height     header-height
                            :font-size       "16px"
                            :padding         "0 42px 0 10px"}]]
       [:.arrow-up-ra :.arrow-down-ra {:left "-10px"}]
       [:.arrow-up-ra {:top "17px"}]
       [:.arrow-down-ra {:top "24px"}]]
      [:.menu-divider {:width      "150px"
                       :background dark-grey}]
      [:.menu-stripe {:margin     "0 10px"
                      :width      "130px"
                      :height     "1px"
                      :background "#9a9a9a"}
       (small-screen {:width "180px"})]
      [:.header-menu {:color         "#9a9a9a"
                      :background    ladders-black
                      :border        none
                      :height        "50px"
                      :border-radius "0"
                      :text-align    "left"
                      :overflow      "visible"
                      :padding       "10px"
                      :font-size     "16px"}
       [:&:focus {:outline none}]
       (small-screen [:& {:width "100%"}])]
      [:.arrow-down-ra :.arrow-up-ra {:width        "0px"
                                      :height       "0px"
                                      :display      "inline-block"
                                      :position     "relative"
                                      :left         "68px"
                                      :float        "left"
                                      :border-left  "6px solid transparent"
                                      :border-right "6px solid transparent"}]
      [:.arrow-down-ra {:top           "-27px"
                        :border-top    "6px solid #9a9a9a"
                        :border-bottom "6px solid transparent"}]
      [:.arrow-up-ra {:top           "-33px"
                      :border-top    "6px solid transparent"
                      :border-bottom "6px solid white"}]
      [:.active {:color      white
                 :background dark-grey
                 :box-shadow none}]
      [:.dropdown {:background dark-grey
                   :width      "150px"
                   :margin-top "-13.5px"}
       [:a {:font-size       "16px"
            :padding         "10px"
            :color           "#9a9a9a"
            :text-decoration none}
        [:&:hover {:color ladders-teal}]]
       [:.btn {:background dark-grey
               :width      "150px"}
        [:&:hover {:color ladders-teal}]]]
      [:&.active {:cursor "pointer"}]]
     [:.menu-holder.active {:background dark-grey}
      (small-screen [:& {:height "auto"}])
      [:.header-menu {:background dark-grey
                      :color      white}]]
     [:.jobs-menu.active :.search-menu.active :.superuser.active
      (small-screen [:& {:height "135px"}])]]
    [:.nav-tabs {:border      "rgb(247, 245, 240)"
                 :font-family ladders-font-bold}
     [:li.active [:a :a:hover {:background ladders-bg-grey
                               :border     none
                               :color      "#343434"}]]
     [:li [:a {:background    dark-white
               :font-size     "16px"
               :color         "#aaa"
               :border-radius "0"
               :border        none
               :margin        "0 3px"
               :padding-left  "30px"
               :padding-right "30px"}]
      [:a:hover {:background ladders-bg-grey}]]]
    [:.level1 {:font-family ladders-font}]

    [:.job-menu {:z-index 10}]
    [:.rc-div-table {:font-size "14px"
                     :border    none}]
    [:.rc-div-table-header [:div {:background ladders-bg-grey
                                  :border     none}]]
    [:.rc-div-table-row {:font-size "14px"}
     [:.rc-box {:overflow "hidden"}]]
    [:.char-count {:width      "100%"
                   :text-align "right"
                   :font-size  "11px"
                   :color      "#959595"
                   :margin     "5px 0 0"}]

    [:.account-management
     [:.contact-info {:margin-top "30px"}
      [:a.contact {:padding-left "40px"}
       [:&.email-rep {:background      (str "transparent url(" (img-url :email-icon) ") 10px 2em no-repeat")
                      :background-size "20px 20px"}]
       [:&.call-rep {:background      (str "transparent url(" (img-url :phone-icon) ") 10px 2em no-repeat")
                     :background-size "20px 20px"}]]
      [:p {:font-size     "14px"
           :color         "#818181"
           :padding       "0 1em"
           :margin-bottom "0"}]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Footer
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.footer-container {:position "relative"
                         :margin-top "60px"}
     [:.footer {:width      "100%"
                :height     footer-height
                :background ladders-black
                :padding    "10px"
                :position   "relative"
                :z-index    10}
      [:.content {:width      "100%"
                  :max-width  "968px"
                  :margin     "0 auto"
                  :min-height "0px"
                  :height     "auto"}
       [:.logo {:text-align    "left"
                :width         "100%"
                :float         "left"
                :margin-bottom "15px"}
        [:a [:img {:width "162px"}]]]]
      [:.links {:width        "20%"
                :margin-right "auto"}]
      [:.sublinks {:text-align "left"
                   :padding    "2px 0"}
       [:a {:color     "#cdcccc"
            :font-size "14px"}]
       [:p {:margin "auto"}]]

      [:.sublinks-social {:padding-top "1.5em"
                          :float       "right"
                          :text-align  "right"}
       [:a {:padding "3px"}]
       [:.social {:width             "20px"
                  :height            "20px"
                  :display           "inline-block"
                  :background        (str "url(" (img-url :social-media-transparent) ")")
                  :background-repeat "no-repeat"
                  :background-size   "148px auto"}]
       [:.twitter {:background-position "-10px -6px"
                   }]
       [:.facebook {:background-position "-36px -6px"}]
       [:.instagram {:background-position "-61px -6px"}]
       [:.linkedin {:background-position "-88px -6px"}]]
      [:.copyright {:font-size "14px"
                    :color     "#cdcccc"}]
      [:#links [:.sublinks {:text-align  "left"
                            :line-height "2em"}]]]
     [:.contact-slide-up {:width              "201px"
                          :height             "102px"
                          :border-radius      "2px"
                          :background-color   "#fbbc48"
                          :position           "absolute"
                          :right              "20px"
                          :transform          "translateY(-47px)"
                          :font-family        ladders-font
                          :font-size          "12px"
                          :padding            "6px 10px"
                          :line-height        "15px"
                          :transition         ".4s"
                          :-webkit-transition ".4s"
                          :box-shadow         "0 3px 6px 0 #555"
                          :cursor             "pointer"}
      [:&.up {:transform "translateY(-100px)"}]
      [:a {:color  ladders-black
           :height "auto"}]
      [:.contact {:margin-left   "30px"
                  :margin-bottom "3px"
                  :line-height   "18px"}
       [:&:before {:background-image (str "url(" (img-url :talk-bubble) ")")
                   :position         "absolute"
                   :content          "''"
                   :height           "21px"
                   :width            "24px"
                   :background-size  "24px 21px"
                   :left             "10px"
                   :top              "8px"}]]
      [:.label {:font-size "12px"
                :padding   "0"}]
      [:.name {:font-family ladders-font-boldest}]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.modal-box {:border-radius "0"
                  :background    ladders-bg-grey
                  :padding       "24px"
                  :position      "relative"
                  :width         "600px"}
     [:.modal-title {:width       "100%"
                     :margin      "0 0 20px 0"
                     :font-size   "30px"
                     :font-family ladders-font-bold}]
     [:.modal-message {:padding     0
                       :font-size   "16px"
                       :font-family ladders-font
                       :color       error-red}]
     [:.modal-body {:width   "100%"
                    :padding "0"}]
     [:.modal-close {:position         "absolute"
                     :top              "31px"
                     :right            "31px"
                     :cursor           "pointer"
                     :width            "23.7px"
                     :height           "23.7px"
                     :background-image (str "url(" (img-url :modal-x-url) ")")}]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Promote Job Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.modal
     [:&.promote-job
      [:.modal-box {:border-radius "0"
                    :background    ladders-bg-grey
                    :padding       "50px"
                    :width         "660px"}
       [:.modal-title {:width          "100%"
                       :margin         "0 0 20px 0"
                       :font-size      "36px"
                       :letter-spacing "-.2px"
                       :font-weight    "100"
                       :font-family    ladders-font
                       :color          "#999999"}]
       [:.modal-body {:padding     "0"
                      :font-size   "16px"
                      :line-height "1.5"
                      :width       "100%"}]
       [:button {:background-color ladders-orange
                 :width            "100%"
                 :margin           "37px 0 0"
                 :font-size        "20px"
                 :font-weight      "300"
                 :font-family      ladders-font}
        [:&:hover :&:focus {:background-color ladders-orange}]]]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Purchase Promotion For Job Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.modal
     [:&.purchase-for-job
      [:.modal-box {:padding "50px 98.5px 41px 85px"
                    :width   "750px"}
       [:.modal-title {:width       "100%"
                       :font-size   "36px"
                       :font-weight "300"
                       :font-family ladders-font
                       :color       "#4a4a4a"
                       :line-height "1"}]
       [:.modal-body {:width "100%"}
        [:ul {:margin  "0"
              :padding "20px 0 0 34px"}
         [:li {:font-size "20px"}]]
        [:.modal-list-disclaimer {:font-size   "14px"
                                  :font-weight "300"
                                  :color       "#999999"
                                  :line-height "2.14"
                                  :height      "30px"
                                  :padding     "0 0 0 34px"
                                  :margin      "-5px 0 0"}]]
       [:.modal-disclaimer {:width       "534px"
                            :font-size   "14px"
                            :font-weight "300"
                            :color       "#999999"
                            :margin      "41px 0 0"}]
       [:button {:background-color ladders-orange
                 :width            "300px"
                 :margin           "37px 0 0"
                 :font-size        "20px"
                 :font-weight      "300"
                 :font-family      ladders-font}
        [:&:hover :&:focus {:background-color ladders-orange}]]]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Purchase Again Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.modal
     [:&.purchase-again
      [:.modal-box {:padding "52px 50px 53px"
                    :width   "773px"}
       [:.modal-title {:width          "100%"
                       :font-size      "36px"
                       :letter-spacing "-.2px"
                       :font-weight    "100"
                       :font-family    ladders-font
                       :color          "#999999"}]
       [:.modal-body {:width     "100%"
                      :font-size "16px"}]
       [:button {:background-color ladders-orange
                 :width            "100%"
                 :margin           "37px 0 0"
                 :font-size        "20px"
                 :font-weight      "300"
                 :font-family      ladders-font}
        [:&:hover :&:focus {:background-color ladders-orange}]]]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  All Pages
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.main {:background ladders-bg-grey
             :min-height (str "calc(100% - " header-height " - " footer-height ")")}]
    [:.content-holder {:width          "100%"
                       :background     ladders-bg-grey
                       :padding-bottom "10px"
                       :min-height     "100%"}]
    [:.content {:margin "0 auto"
                :height "100%"
                :width  content-width}]
    (small-screen [:.content {:width "345px"}])
    [:.title-box {:margin "48px auto 0"
                  :width  content-width}
     [:.subtitle {:font-size "14px"}]]

    [:.form-control {:border-radius "0"
                     :box-shadow    none}]
    [:.rc-div-table-header {:font-family ladders-font-boldest}]
    [:.title-container {:width      "100%"
                        :position   "relative"
                        :background white}
     [:.title {:font-size    "36px"
               :font-family  ladders-font
               :line-spacing "-.2px"}]
     [:.subtitle {:margin-bottom "24px"
                  :font-size     "14px"
                  :font-family   ladders-font-bold}]]
    [:.circle-img {:width         "48px"
                   :height        "48px"
                   :border-radius "50%"
                   :margin-left   "15px"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Login Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.login {:margin "100px 0 50px"}
     [:.form-col {:width "50%"}
      [:.login-disclaimers {:padding "0 50px"}]
      [:.login-form {:padding "20px 50px"}
       [:&.change-password {:padding "20px 0"}
        [:.btn :.holder :.info {:width "86%"}]]]
      [:.title {:font-size     "52px"
                :margin-bottom "30px"
                :line-height   "1.2em"}]
      [:.error-msg {:color       error-red
                    :font-size   "16px"
                    :font-family ladders-font-bold
                    :margin      "0 0 20px"}]
      [:.login.btn {:margin      "30px 0 20px"
                    :width       "100%"
                    :font-size   "16px"
                    :height      "50px"
                    :font-family ladders-font-bold}]
      [:.form-control {:height    "48px"
                       :margin    "0 0 10px"
                       :border    none
                       :font-size "21px"}]]
     [:.privacy-policy-agreement {:color       grey
                                  :font-size   "16px"
                                  :margin      "0 0 20px"
                                  :font-family ladders-font-bold}]
     [:img.secure-site-img {:width  "150px"
                            :height "65px"}]
     [:.forgot-password-link {:font-size   "16px"
                              :font-family ladders-font-bold}]]

    [:.info {:font-size     "16px"
             :font-family   ladders-font-bold
             :color         ladders-text-dark
             :margin-bottom "24px"}]
    [:.login-info {:width   "50%"
                   :padding "103px 20px 20px 60px"}
     [:.title {:font-family ladders-font-bold
               :font-size   "24px"}]
     [:.password-info
      [:.title {:margin "0 0 20px"}]
      [:.info {:margin "0 0 10px"}]
      [:.protect-your-privacy {:margin "0 0 30px"}]
      [:.password-info-list-item {:margin "0 0 10px"}
       [:.list-item :.list-item-example {:padding "0 0 0 16px"}]
       [:.list-item {:position    "relative"
                     :font-family ladders-font-boldest
                     :font-size   "16px"
                     :width       "100%"}
        [:&:before {:position "absolute"
                    :left     0
                    :content  "\"-\""
                    :padding  "0"}]]]]
     [:.list-item-example {:font-family ladders-font-bold
                           :font-size   "16px"}]]
    [:.info-group {:margin "0 0 40px"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Account Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.account
     [:.row {:margin "0 0 0 3px"
             :width  "100%"}]
     [:.divider {:width         "100%"
                 :height        "1px"
                 :background    white
                 :margin-bottom "0"}]
     [:.title-container {:margin     "0 auto"
                         :min-height "170px"}
      [:.title-box {:width    content-width
                    :margin   "0 auto"
                    :position "relative"}
       [:.level1 {:font-size      "36px"
                  :letter-spacing "-.2px"
                  :margin-top     "40px"}]
       [:.account-info {:margin-bottom "30px"}]
       [:.button-group {:position "absolute"
                        :top      "100px"
                        :right    "0px"
                        :padding  "0"}]]]
     [:input {:padding ".3em .8em"}]
     [:.info-box {:margin "40px 0"}
      [:.holder {:width "45%"}]
      [:.site-holder {:width "100%"}]
      [:.email-holder {:width "630px"}]
      [:.phone-holder {:width "300px"}]
      [:.ext-holder {:width "150px"}]
      [:.street-holder :.city-holder :.state-dd-holder :.zip-holder :.country-dd-holder {:width "32%"}]
      [:.rc-label {:margin    "5px 5px 5px 0"
                   :font-size "16px"}]
      [:.title {:margin      "0"
                :font-family ladders-font-bold
                :font-size   "24px"
                :color       charcoal-light}]
      [:.subtitle {:font-size "16px"
                   :margin    "10px 0"}]]

     [:.form-control {:border    "none"
                      :height    "40px"
                      :font-size "16px"}
      [:&:disabled
       {:background (str "#FFFFFF url(" (img-url :lock) ") 97% 50% no-repeat")}]
      [:&.error {:border "1px solid #ed4e3d"}]]
     [:.password-col {:width "416px"}
      [:.holder {:width "100%"}]]
     [:.ats-col
      [:.holder {:width "100%"}]
      [:.api-key-holder :.ats-dropdown-holder :.job-board-token-holder {:max-width "416px"}
       [:.info {:font-size "13px"
                :margin    "0"}]]
      [:.lever-desc {:font-size "16px"
                     :margin    "10px 0"}
       [:span {:font-family ladders-font-boldest}]]]
     [:.ats-info
      [:p {:font-size     "16px"
           :margin-top    "15px"
           :margin-bottom "25px"}]]
     [:.social.info-box {:position "relative"
                         :width    "1000px"}
      [:.holder {:width "490px"}]
      [:.rc-input-text {:width "100%"}]]
     [:.personal.info-box {:position "relative"
                           :width    "70%"}
      [:.rc-input-text {:width "100%"}
       [:.company-site {:width "650px"}]]]
     [:.dropdown {:width "100%"}]
     [:.holder
      [:&.country-holder :&.state-holder {:width  "100%"
                                          :height "40px"}]]
     [:.chosen-single {:height "40px"}]
     [:.nav-tabs {:position "relative"
                  :bottom   "0"}]
     [:.biography {:width "1000px"}
      [:.sample-bio {:font-size     "16px"
                     :background    ladders-dark-tan
                     :padding       "20px"
                     :margin-bottom "10px"}
       [:h1 {:font-size "20px"}]]
      [:a {:cursor "pointer"}]
      [:.bio {:height "200px"}]]
     [:.hidden-info {:background ladders-dark-tan
                     :padding    "20px"
                     :width      "100%"
                     :margin     "0"}
      [:.head-text {:font-size   "36px"
                    :color       charcoal-light
                    :font-family ladders-font
                    :margin-top  "0"}]
      [:.hidden-info-desc {:background   (str "transparent url(" (img-url :eye-crossout) ") 0 8px no-repeat")
                           :color        charcoal
                           :font-size    "14px"
                           :line-height  "36px"
                           :padding-left "40px"}]]
     [:.photo {:width "25%"}
      [:.photo-header {:font-size   "14px"
                       :font-family ladders-font-bold}]]
     [:.sub-info {:top   "auto"
                  :right "auto"}]
     [:.button-group {:padding "50px 0"}
      [:.btn {:width            "107px"
              :padding          "0"
              :font-size        "16px"
              :height           "50px"
              :background-color ladders-teal}]
      [:.btn-border {:background "none"
                     :box-sizing "border-box"
                     :margin     "0"}]]
     [:.password-save-btn :.ats-save-btn {:padding-top "30px"}]
     [:.subscriptions-info
      [:.cell1 {:width "450px"}]
      [:.status {:width       "100px"
                 :color       "#aaa"
                 :font-family ladders-font-bold
                 :font-size   "16px"
                 :line-height "34px"
                 :padding     "0 0 0 20px"}]
      [:.subscriptions-row {:font-size "14px"}
       [:a {:margin-top "5px"
            :cursor     "pointer"}]
       [:.sub-icon {:width           "31px"
                    :height          "26px"
                    :margin          "2px 45px 0 25px"
                    :background-size "31px 26px"}]
       [:.check {:background-image (str "url(" (img-url :check-url) ")")}]
       [:.nein {:background-image (str "url(" (img-url :nein-url) ")")}]
       [:.title {:color       ladders-black
                 :font-size   "16px"
                 :font-family ladders-font-boldest}]]]]
    [:.profile-image {:position "relative"
                      :width    "250px"
                      :height   "250px"
                      :margin   "5px 0 0"
                      :cursor   "pointer"}
     [:.loading-icon-holder {:position absolute
                             :top      "40%"
                             :left     "45%"}]
     [:img {:width  "100%"
            :height "100%"}]
     [:button {:position  "absolute"
               :left      "5%"
               :bottom    "5%"
               :width     "90%"
               :font-size "16px"
               :height    "50px"}
      [:&:hover {:background-color ladders-teal}]]]
    [:.photo-disclaimer {:font-size "11px"}]
    [:.account-panel-alert-bar {:position "fixed"
                                :z-index  "100"
                                :top      "0"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Search Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.search {:background-color white}
     [:.whoops-page {:text-align  "center"
                     :padding-top "70px"}
      [:h1 {:font-family ladders-font-bold}]
      [:p {:font-size "16px"}]]
     [:.content {:min-height "auto"}]
     [:.content-holder {:position       "relative"
                        :padding-bottom "0"
                        :min-height     "auto"}]
     [:.arrow-down-ra :.arrow-up-ra {:width        "0px"
                                     :height       "0px"
                                     :border-left  "6px solid transparent"
                                     :border-right "6px solid transparent"
                                     :margin       "0 0 0 5px"}
      [:&.disabled {:border-top    "6px solid transparent"
                    :border-bottom "6px solid transparent"}]]
     [:.arrow-down-ra {:border-top    (str "8px solid " ladders-greeny-blue)
                       :border-bottom "6px solid transparent"
                       :position      "relative"
                       :top           "8px"}]
     [:.arrow-up-ra {:border-top    "6px solid transparent"
                     :border-bottom (str "8px solid " ladders-greeny-blue)
                     :padding       "2px 0 0"}]


     [:.input-label {:font-size "14px"}]
     [:.chosen-container-active.chosen-container-active
      [:.chosen-single {:box-shadow    none
                        :border        none
                        :border-bottom white}]
      [:.chosen-drop {:box-shadow    none
                      :border        (str "1px solid " lighter-grey)
                      :border-top    none
                      :border-radius 0}]]
     [:.chosen-container [:.chosen-results [:li.highlighted {:background "none"
                                                             :color      "#333333"}]]]
     [:.rc-typeahead-suggestions-container {:width  "100%"
                                            :border (str "1px solid " lighter-grey)}]
     [:.holder
      [:.labels {:margin-bottom "5px"}]]
     [:.rc-dropdown
      [:.chosen-single {:height "28px"}
       [:span {:font-size   "13px"
               :height      "28px"
               :line-height "23px"}]]]
     [:.search-tooltip {:max-width "200px"
                        :font-size "13px"
                        :padding   "7px 10px"
                        :margin    "0"}
      [:.bold {:font-family ladders-font-boldest}]]
     [:.fields-holder
      [:.fields {:width         "100%"
                 :margin-bottom "20px"}
       [:.holder {:width      "30%"
                  :max-height "300px"
                  :position   "relative"}]]]
     [:.title-box {:background    white
                   :margin-top    "10px"
                   :margin-bottom "20px"
                   :width         "100%"}
      [:.divider {:background ladders-bg-grey
                  :height     "2px"
                  :width      "100%"
                  :margin     "0 0 20px"}]
      [:.search-title-container {:width "30%"}
       [:.holder {:width "100%"}]
       [:.check-holder
        [:.info {:font-size "13px"
                 :color     "#333"}]]]]
     [:.left-menu {:font-size "36px"}]
     [:.result-header {:font-family ladders-font
                       :font-weight "300"
                       :font-size   "14px"
                       :margin-top  "10px"}
      [:.filter
       [:a {:padding "0 5px"
            :cursor  "pointer"}]
       [:.active {:color       ladders-black
                  :font-family ladders-font-bold
                  :cursor      "auto"}]]]

     [:.results-wrapper {:table-layout "fixed"
                         :width        "1000px"
                         :margin-top   "31px"
                         :box-sizing   "border-box"}
      [:.white-bottom-border-2 {:border-bottom "2px solid #fff"}]
      [:.ellipsis {:text-overflow "ellipsis"
                   :overflow      "hidden"
                   :white-space   "nowrap"}]
      [:td {:position       "relative"
            :padding        "6px 20px 20px 0"
            :box-sizing     "border-box"
            :vertical-align "top"}
       [:p {:min-height "18px"}]]
      [:tr {:position "relative"}]
      [:thead {:table-layout   "fixed"
               :padding-bottom "8px"
               :font-family    ladders-font-boldest
               :font-size      "13px"}
       [:th {:table-layout  "fixed"
             :padding-right "20px"
             :box-sizing    "border-box"
             :font-weight   "400"}]
       [:tr {:height "24px"}]]
      [:.candidate-name {:width     "200px"
                         :max-width "227px"}
       [:a {:font-size "16px"}]]
      [:.location {:max-width   "115px"
                   :width       "115px"
                   :font-size   "13px"
                   :line-height "1.85em"
                   :font-weight "300"}]
      [:.education {:width "80px"}]
      [:.desired {:width "60px"}]
      [:.job-title {:width "197px"}]
      [:.company-name {:width "170px"}]
      [:.job-length {:width         "83px"
                     :padding-right "0"}]]
     [:.candidate {:font-family   ladders-font-bold
                   :font-weight   "300"
                   :font-size     "13px"
                   :min-height    "24px"
                   :border-bottom "2px solid #fff"
                   :padding       "1rem 0"
                   :cursor        "pointer"}
      [:&:hover {:background-color "#fff"}]]
     [:.blurred {:cursor "auto"}
      [:&:hover {:background-color ladders-bg-grey}]]
     [:.candidate-viewed {:opacity "0.5"}]
     [:.slider-label {:color ladders-greeny-blue}]
     [:.advanced-search-btn {:font-size   "14px"
                             :line-height "30px"
                             :cursor      "pointer"}
      [:span.arrow-down-ra {:top      "12px"
                            :position "relative"}]
      [:span.arrow-up-ra {:top      "-11px"
                          :position "relative"}]]
     [:.new-search-btn {:font-size   "14px"
                        :line-height "30px"
                        :cursor      "pointer"}]
     [:.edit {:width      "12px"
              :height     "22px"
              :background (str "transparent url(" (img-url :icon-pencil-url) ") no-repeat 0 0")
              :padding    "0px 10px"
              :position   "relative"
              :top        "2px"}]
     [:.remove {:color       ladders-teal
                :font-weight "600"
                :line-height "1"}]

     [:.title-container {:width          "100%"
                         :padding-bottom "20px"
                         :min-height     "190px"}
      [:.title-box {:width  content-width
                    :margin "0 auto"}]
      [:.title {:margin-top  "0"
                :font-size   "14px"
                :font-family "inherit"}]
      [:.advanced-fields {:width         content-width
                          :margin-bottom "20px"}
       [:.search-company-container {:width "25%"}
        [:.holder {:width "100%"}]
        [:.check-holder
         [:.info {:font-size "13px"
                  :color     "#333"}]]]
       [:.holder {:width "25%"}]
       [:.rc-label {:font-size "14px"}]
       [:input.form-control {:height "28px"}]
       [:.name-holder {:width "235px"}]]
      [:.btn {:background    ladders-teal
              :color         white
              :border-radius "0"
              :border        none
              :padding       "15px 40px"
              :font-size     "16px"}]
      [:.search-btn-container {:width       "38%"
                               :padding-top "40px"}]
      [:.search-btn {:background ladders-teal}
       [:&:hover {:background ladders-teal-dark}]
       [:&.disabled {:background "#ccc"
                     :cursor     "auto"
                     :opacity    "1"}]]
      [:.search-btn-icon {:height        "44px"
                          :background    ladders-teal
                          :color         white
                          :padding       "5px"
                          :border-radius "0"
                          :font-size     "34px"
                          :line-height   "34px"
                          :position      "relative"}
       [:&:hover {:background ladders-teal-dark}]
       [:&.disabled {:background "#ccc"
                     :cursor     "not-allowed"}]]

      [:.fields
       [:.rc-popover-anchor-wrapper {:position "relative"
                                     :height   "44px"
                                     :top      "26px"}]]
      [:.form-control {:background ladders-bg-grey
                       :border     "none"
                       :height     "44px"}]
      [:.chosen-single {:background ladders-bg-grey
                        :color      ladders-black
                        :border     "none"}]
      [:.email-alert-label {:font-size    "13px"
                            :margin-right "10px"
                            :line-height  "30px"}]]
     [:.fields-holder {:width    content-width
                       :position "relative"}
      [:.fields {:margin "10px 0"}
       [:.holder {:flex "auto"}]]]
     [:.blur-row {:max-width "1000px"}]
     [:.js-modal {:background    white
                  :border-radius 0}]
     [:.no-result {:font-size     "13px"
                   :margin-top    "50px"
                   :margin-bottom "30px"}
      [:.bold {:font-family ladders-font-boldest}]]
     [:.rc-slider {:width "300px"}]
     [:.slider-input-container
      [:.rc-box {:position "relative"}]]
     [:.salary-input
      [:.rc-box {:position "relative"}]]
     [:.rc-popover-anchor-wrapper {:z-index "10"}]
     [:&.pre-approved
      [:.results-section {:background-color white}]
      [:.pre-approve-result-container {:text-align          "center"
                                       :background-color    white
                                       :min-height          "550px"
                                       :font-size           "30px"
                                       :background-image    (str "url(" (img-url :pre-approved-search-bg) ")")
                                       :background-position "-120px 0"}
       [:h1 {:padding     "0"
             :margin-top  "210px"
             :font-family ladders-font-boldest}]
       [:p {:padding "0"
            :margin  "0"}]
       [:span {:font-family ladders-font-boldest}]]
      [:.alert-bar {:height           "72px"
                    :background-color "#ef8f1c"}
       [:.alert-message {:width        "1000px"
                         :color        white
                         :margin       "0 auto"
                         :padding-left "50px"}
        [:span {:font-family ladders-font-boldest}]]]
      [:&.first-time
       [:.alert-bar {:height           "110px"
                     :background-color "#ef8f1c"}
        [:.alert-message {:width        "1000px"
                          :color        white
                          :margin       "0 auto"
                          :padding-left "0"
                          :font-size    "28px"}
         [:span {:font-family ladders-font-boldest}]
         [:p {:margin    "0"
              :padding   "0"
              :font-size "20px"}]
         [:.em {:font-size  "13px"
                :margin     "0"
                :padding    "0"
                :font-style "italic"}]]]
       [:.pre-approve-search-header {:width    "1000px"
                                     :margin   "20px auto 50px"
                                     :position "relative"}
        [:.pre-approve-search-fields {:width "60%"}
         [:.search-tooltip {:max-width "250px"
                            :font-size "13px"
                            :padding   "20px"
                            :margin    "0"}]
         [:p {:font-size "18px"}]
         [:.form-control {:background ladders-bg-grey
                          :border     "none"
                          :height     "44px"}]
         [:.rc-label {:font-family    ladders-font-boldest
                      :text-transform "uppercase"
                      :font-size      "14px"}]
         [:.salary-input {:margin-bottom "20px"
                          :margin-right  "0"}]
         [:.rc-popover-anchor-wrapper {:z-index "auto"}]
         [:.rc-slider {:width "100%"}]
         [:.chosen-container {:width  "100px"
                              :height "44px"}
          [:.chosen-single {:height           "44px"
                            :background-color ladders-bg-grey}
           [:span {:line-height "38px"}]]]
         [:.search-btn {:width     "600px"
                        :padding   "15px 40px"
                        :font-size "16px"
                        :outline   "none"}
          [:&.disabled {:background "#ccc"
                        :cursor     "auto"
                        :opacity    "1"}]]]
        [:.pre-approved-info {:width      "30%"
                              :margin-top "50px"}
         [:.title {:font-size "22px"}]
         [:.divider {:background ladders-black}]
         [:.info-icon {:width             "100%"
                       :height            "54px"
                       :padding-left      "70px"
                       :margin            "10px 0"
                       :color             "#959595"
                       :font-size         "16px"
                       :line-height       "50px"
                       :background-repeat "no-repeat"}
          [:&.download_resume {:background-image (str "url(" (img-url :download_resume) ")")}]
          [:&.contactinfo {:background-image (str "url(" (img-url :contactinfo) ")")}]
          [:&.location {:background-image (str "url(" (img-url :location) ")")}]
          [:&.salary_data {:background-image (str "url(" (img-url :salary_data) ")")}]
          [:&.yoe {:background-image (str "url(" (img-url :yoe) ")")}]]]]]]
     [:.skills-location {:padding "5px 12px"}
      [:.box-header {:display "none"}]]

     [:.checkbox-container
      [:label {:font-size "12px"
               :color light-grey}]]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Candidate Profile
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.resume-holder {:background ladders-bg-grey
                      :padding    "0 20px"}]
    [:.no-resume {:text-align  "center"
                  :font-size   "24px"
                  :padding     "93px 0 40px"
                  :color       grey
                  :font-family ladders-font-bold}]
    [:.resume-iframe {:border none}]
    [:.js-nav-bar {:padding "10px"}
     [:a {:padding "10px"
          :cursor  "pointer"}]
     [:&:hover {:text-decoration none
                :color           ladders-teal-dark}]]
    [:.js-modal {:position "relative"}
     [:.btn-holder {:position "absolute"
                    :right    "100px"
                    :top      "50px"}
      [:.btn {:margin  "2px"
              :width   "100px"
              :padding "15px"}]
      [:.more-holder {:background   white
                      :border       "solid 1px"
                      :border-color ladders-teal
                      :margin       "2px"}
       [:&.active {:padding "10px"}]
       [:.btn {:margin      "auto"
               :padding     "5px 0"
               :background  white
               :color       ladders-teal
               :font-family ladders-font-bold}]
       [:a {:font-size "14px"
            :padding   "3px"
            :margin    "0 20px"}]]]]
    [:.js-info {:padding "10px 40px"
                :height  "185px"}
     [:.js-name {:font-size "36px"}]]
    [:.application-bar {:width            "100%"
                        :height           "50px"
                        :color            white
                        :background-color charcoal-light
                        :font-size        "13px"
                        :font-family      ladders-font-bold}
     [:.application-info {:width   content-width
                          :height  "100%"
                          :margin  "0 auto"
                          :padding "0 10px"}]
     [:.dismiss-candidate {:height    "26px"
                           :width     "145px"
                           :padding   "2px 20px 3px"
                           :font-size "13px"
                           :margin    "12px 0"}
      [:.loader {:margin-top    "3px!important"
                 :margin-bottom "3px!important"
                 :height        "14px"
                 :width         "14px"}]]]
    [:.candidate-profile
     [:.fa-overlay {:left       "50%"
                    :top        "0"
                    :margin     "175px 0 0 -330px"
                    :box-shadow "0 3px 6px 0 #555"
                    :border-top (str "1px solid " grey)}]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Email Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.email-modal
     [:.form-control {:border "none"}]
     [:.modal-box {:padding 0
                   :width   "800px"}]
     [:.email-form {:background ladders-bg-grey
                    :width      "500px"
                    :padding    "36px 50px 45px"}]
     [:.rc-label {:font-size "16px"
                  :margin    "10px 0"}]
     [:textarea
      [:&:focus {:border none}]]
     [:.title {:font-size   "36px"
               :color       charcoal-light
               :font-family ladders-font
               :margin      "0 0 6px"}]
     [:.email-input {:width "100%"}
      [:.greeting-dropdown {:width      "100%"
                            :max-height "270px"}]]
     [:.email-link {:font-size "14px"
                    :margin    "5px 0 0"}]
     [:button {:width            "100%"
               :margin           "24px 0 0"
               :font-size        "20px"
               :font-family      ladders-font-bold
               :background-color ladders-teal}
      [:&:hover {:background-color ladders-teal-dark}]]
     [:.templates {:width      "300px"
                   :background ladders-dark-tan
                   :padding    "117px 30px 0 35px"}
      [:.rc-label {:height        "31px"
                   :width         "100%"
                   :border-bottom "2px solid #ffffff"}]
      [:.template-list
       [:div [:div {:position "relative"}]]
       [:.template-list-item-holder
        [:&:hover {:background-color white
                   :opacity          "0.6"}]
        [:.rc-box {:width "100%"}
         [:.rc-box {:position "relative"}]]
        [:.template {:font-size "14px"
                     :margin    "5px 0"
                     :color     light-grey
                     :cursor    "pointer"
                     :width     "100%"}
         [:&.active
          [:&:before {:background-image (str "url(" (img-url :arrow-line-url) ")")
                      :position         "absolute"
                      :content          "''"
                      :height           "11px"
                      :width            "9px"
                      :left             "-15px"
                      :top              "9px"}]]]
        [:.delete-template {:width    "17px"
                            :height   "17px"
                            :margin   "6.5px 5px 0 0"
                            :position "absolute"
                            :right    "30px"
                            :cursor   "pointer"}]]]
      [:.no-templates {:font-size "16px"}]]]

    [:.action-text-input-holder {:width    "100%"
                                 :position "relative"}
     [:.action-text-input {:width "100%"}]
     [:.action-input-link {:position  "absolute"
                           :right     "0"
                           :font-size "16px"
                           :margin    "5px 10px 0 0"}]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Marketing Home Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+


    [:.marketing-home {:min-width        "100%"
                       :background-color "#f5f5f5"}
     [:.form-control:focus {:box-shadow         "none"
                            :border-color       "inherit"
                            :-webkit-box-shadow "none"}]
     [:div {:text-align "center"}]
     [:.container {:max-width "1000px"
                   :style     {:align-self "center"}}]
     [:.content {:width      "100%"
                 :min-height "0px"
                 :height     "auto"
                 :background ladders-black}]
     [:.divider {:background white
                 :width      "90%"
                 :max-width  "1000px"
                 :clear      "both"
                 :height     "2px"
                 :border     "solid 1px #cccccc"
                 :margin     "0 auto"
                 :opacity    "0.6"}]
     [:h2 {:font-size     "40px"
           :font-family   ladders-font-bold
           :display       "block"
           :margin-bottom "30px"
           :margin-top    "30px"}
      [:.break {:display "inline-block"}]]
     [:h1 {:font-family ladders-font
           :font-size   "64px"
           :text-align  "center"
           :color       "#ffffff"
           :font-weight "100"
           :margin-top  "140px"}]
     (st/at-media {:max-width "610px"}
                  [:h1 {:font-size "45px"}])
     [:h3 {:font-family ladders-font
           :font-size   "40px"
           :color       "#333333"
           :font-weight "normal"
           :margin      "57px 0 45px"}]
     [:.row {:margin-left  "auto"
             :margin-right "auto"
             :box-sizing   "border-box"}]
     [:.info-container {:padding-left  "75px"
                        :padding-right "75px"}]
     [:.info-item {:clear      "left"
                   :float      "inherit"
                   :text-align "inherit"
                   :margin     "0"}
      [:.title {:font-family ladders-font-boldest
                :font-size   "48px"
                :color       ladders-teal}]
      [:.img {:background-color    "transparent"
              :background-size     "126px"
              :background-position "bottom center"
              :width               "215px"
              :height              "126px"
              :background-repeat   "no-repeat"}
       [:&.mh-demographic-image {:background-image (str "url(" (img-url :demographic) ")")}]
       [:&.mh-engagement-image {:background-image (str "url(" (img-url :engagement) ")")}]
       [:&.mh-fastest-growing-image {:background-image (str "url(" (img-url :fastest-growing) ")")}]]
      [:.caption {:color       "#333333"
                  :font-size   "16px"
                  :font-family ladders-font-bold
                  :text-align  "center"}]
      [:.img-caption {:color       "#333333"
                      :font-size   "16px"
                      :font-family ladders-font-bold
                      :text-align  "center"
                      :width       "215px"}]
      [:.mh-demographic-image-img {}]]
     [:.mh-header-img {:background      (str "url(" (img-url :hire-faster) ") no-repeat ")
                       :background-size "cover"
                       :height          "512px"}]
     [:.ladders-clients {:margin "0 auto 65px"}
      [:img {:width "100%" :max-width "1000px"}]]
     [:.login-section {:background-color "#231f20"
                       :width            "100%"
                       :max-width        "1000px"
                       :margin           "0 auto"}
      [:.logo-holder {:margin-left "15px"}]

      [:.logo [:a [:img {:width "162px"}]]
       {:float         "left"
        :margin-bottom "15px"}]]
     [:.login-button {:margin "15px 19px 0 auto"}]
     [:.login-button [:a {:font-size      "16px"
                          :letter-spacing "1px"
                          :padding-right  "4px"
                          :color          "#aaaaaa"}]]
     [:.feature-row-1 {:margin-bottom  "20px"
                       :margin-top     "22px"
                       :padding-bottom "10px"
                       :padding-left   "5%"
                       :padding-right  "5%"}]
     [:.feature-row {:max-width "1000px"
                     :margin    "10px auto"}
      (st/at-media {:max-width "1000px"}
                   [:.feature-row {:width "100%"}])
      [:h4 {:font-size   "30px"
            :font-family ladders-font-bold}]
      [:.feature-info {:width      "100%"
                       :max-width  "450px"
                       :margin     "30px auto"
                       :padding    "0 20px"
                       :align-self "center"}]]
     [:.featured-row
      [:.full-title {:font-size   "40px"
                     :font-family ladders-font-bold
                     :margin      "30px auto 30px"
                     :max-width   "1000px"}]
      (st/at-media {:max-width "625px"}
                   [:.full-title {:font-size     "40px"
                                  :line-height   "48px"
                                  :margin-bottom "10px"}])
      [:div.full-subtitle {:font-size   "18px"
                           :font-family ladders-font-bold
                           :color       "#333333"
                           :margin      "0 30px 40px"}]]
     [:.infographic-row {:background-color white
                         :padding-bottom   "30px"
                         :margin-top       "0"
                         :margin-bottom    "0"}]
     [:.email-wrapper {:background     "#000"
                       :position       "fixed"
                       :padding-bottom "15px"
                       :width          "100%"}]
     [:.bottom-form {:padding-bottom "80px"}
      [:.email-form {:margin-top "19px"
                     :position   "relative"}]
      [:.email-hint-container {:max-width "1000px"
                               :width     "100%"
                               :margin    "0 auto"
                               :position  "absolute"
                               :top       "50px"}]
      [:.icons {:height "0px"}]]

     [:.email-form {:background   "#ffffff"
                    :width        "95%"
                    :max-width    "620px"
                    :height       "70px"
                    :margin       "auto"
                    :padding      "12px"
                    :padding-left "0px"
                    :margin-top   "25px"}
      [:.btn {:font-size      "16px"
              :padding        "15px"
              :padding-bottom "11px"
              :padding-top    "11px"}]
      [:.get-started {:height "44px"}
       [:.loader {:margin-top    "0!important"
                  :margin-bottom "0!important"}]]
      [:input {:margin-top "6px"}]
      [:.form-control {:font-size   "20px"
                       :margin-left "20px"
                       :border      none}]
      [:.email-holder
       [:.rc-input-text {:width "100%"}]
       (st/at-media {:max-width "500px"} [:input {:margin-left "3px"
                                                  :font-size   "16px"}])]]
     [:.feature-info
      [:p {:text-align "left"
           :font-size  "18px"}]
      [:h4 {:text-align  "left"
            :color       "#333333"
            :font-weight "bold"}]]
     [:.email-hint-container {:margin-top "20px"
                              :margin     "20px auto 0"
                              :width      "100%"
                              :max-width  "616px"}
      [:div {:background-color "#52cf5a"
             :width            "602px"
             :height           "42px"
             :color            "#fff"
             :padding          "10px"
             :padding-top      "2px"
             :text-align       "left"}]
      [:.icons {:width        "42px"
                :height       "42px"
                :float        "left"
                :position     "absolute"
                :top          "0px"
                :padding-top  "12px"
                :left         "0px"
                :padding-left "18px"}]
      [:.valid-msg [:.icons {:background   "rgba(82, 206, 90, 1)"
                             :padding-left "12px"}]]
      [:.error-msg [:.icons {:background "rgba(255, 50, 38, 1)"}]]
      [:.valid-msg-corp [:.icons {:background   "rgba(68, 68, 68, .5)"
                                  :padding-left "17px"}]]
      [:.message {:font-size    "14px"
                  :width        "100%"
                  :max-width    "620px"
                  :margin-top   "15px"
                  :position     "relative"
                  :padding-left "70px"
                  :padding-top  "10px"}]
      [:.error-msg {:background "rgba(255, 50, 38, .25)"}]
      [:.valid-msg {:background "rgba(82, 206, 90, .25)"}]
      [:.valid-msg-corp {:background "rgba(68, 68, 68, .5)"}
       [:img {:margin-right "0px"}]]]
     [:.half {:align-self "center"
              :margin     "30px auto"}]
     [:.featured-row
      [:.premium {:position        relative
                  :margin          "0 auto"
                  :height          "30px"
                  :width           "100px"
                  :top             "-15px"
                  :background      (str "url(" (img-url :premium) ") no-repeat")
                  :background-size "100px 30px"}]
      [:.divider {:position        "relative"
                  :overflow        "visible"
                  :height          "1px"
                  :border-collapse "collapse"
                  :opacity         "0.9"}]]
     [:ul.get-started-list {:text-align "left"
                            :font-size  "16px"}]
     [:.client-list {:background-color white}]
     [:.routing {:width          "320px"
                 :font-size      "20px"
                 :line-height    "20px"
                 :padding-top    "20px"
                 :padding-bottom "20px"
                 :margin-bottom  "42px"
                 :align-self     "center"}]]




    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Post Job Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.post-job {:color          charcoal-light
                 :letter-spacing "0.4px"}
     [:.no-comm {:margin-top  "20px"
                 :font-size   "14px"
                 :font-family ladders-font-bold}]
     [:.info-tool-tip {:margin "17px 10px 0"
                       :height "22px"}]
     [:.roles-tooltip {:width     "280px"
                       :margin    "25px 40px"
                       :font-size "14px"}
      [:.title {:margin-bottom "20px"}]]
     [:.info {:color ladders-text-dark}]
     [:.quill-holder {:margin-top "20px"}]
     [:.holder {:margin-top "20px"}]
     [:.progress-status {:background        white
                         :-webkit-animation "slideInFromLeftFirst 1s ease-out 0s 1"
                         :animation         "slideInFromLeftFirst 1s ease-out 0s 1"}
      [:.bar {:width      "30%"
              :height     "8px"
              :background ladders-teal}]
      [:.arrow {:width         "0"
                :height        "0"
                :border-top    "4px solid transparent"
                :border-bottom "4px solid transparent"
                :border-left   (str "4px solid " ladders-teal)}]]
     [:.content-holder {:justify-content "flex-start"
                        :padding-bottom  "0"}
      [:.content {:justify-content "flex-start"
                  :height          "initial"}]]
     [:.form-control {:font-size "16px"
                      :height    "48px"}]
     [:.divider {:margin     "20px 0 0 "
                 :height     "2px"
                 :background lighter-grey}]
     [:.title-wrapper {:width      "100%"
                       :background white
                       :display    "flex"}
      [:.title-content {:width     "100%"
                        :max-width "1000px"
                        :margin    "0 auto"
                        :position  "relative"}]
      [:.title {:flex           "0 0 auto"
                :max-width      "1000px"
                :width          "100%"
                :margin         "32px auto"
                :font-size      "36px"
                :font-family    ladders-font
                :color          charcoal
                :letter-spacing "-.2px"}]]
     [:.job-desc {:height    "240px"
                  :font-size "20px"}
      [:ul {:list-style "disc"
            :font-size  "20px"}]]
     [:#quill-wrapper-job-desc {:margin-bottom "5px"}]
     [:.exp-picker {:margin-top   "5px"
                    :margin-right "-30px"}
      [:.rc-tabs {:padding "0 10px"}
       [:.btn
        [:&:after {:width    "20px"
                   :height   "20px"
                   :content  (str "url(" (img-url :oval) ")")
                   :position "absolute"
                   :top      "-8px"
                   :right    "-8px"}]]
       [:button.btn.active
        [:&:after {
                   :width    "20px"
                   :height   "20px"
                   :content  (str "url(" (img-url :selected-icon) ")")
                   :position "absolute"
                   :top      "-8px"
                   :right    "-8px"
                   }]]]
      [:.input-label {:margin-bottom "5px"
                      :font-size     "10px"
                      :font-family   ladders-font-bold}]
      [:.btn {:width        "150px"
              :margin-right "16px"
              :padding      "15px 0"
              :background   white
              :color        ladders-greeny-blue
              :box-shadow   none
              :font-size    "16px"}
       [:&:focus {:outline none}]]
      [:.active {:background ladders-teal
                 :color      white}]]
     [:.denomination-tabs {:margin-top "20px"}
      [:.input-label {:margin-bottom "0px"
                      :font-size     "10px"
                      :font-family   ladders-font-bold}
       [:&.exterior {:margin-bottom "5px"}]]
      [:.holder {:margin-top "0"}]
      [:.btn {:width        "80px"
              :margin-right "2px"
              :background   white
              :height       "40px"
              :color        ladders-greeny-blue
              :box-shadow   none
              :font-size    "16px"}
       [:&:focus {:outline none}]]
      [:.active {:background ladders-teal
                 :color      white}]]
     [:.min-max-slider {:width       "510px"
                        :margin      "20px 0 60px 40px"
                        :font-size   "16px"
                        :font-family ladders-font-bold}
      [:.slider-label {:margin-bottom "10px"}]]
     [:.role-dropdown {:width  "380px"
                       :height "40px"}]
     [:.holder
      [:.labels {:margin-bottom "0"}]]
     [:.input-label {:font-size      "10px"
                     :font-family    ladders-font-bold
                     :text-transform "uppercase"
                     :margin-bottom  "5px"}]
     [:.name {:margin-bottom  "5px"
              :text-transform "uppercase"
              :font-size      "10px"
              :font-family    ladders-font-bold}]
     [:.bonus-holder {:width "100%"}
      [:.labels {:display "none"}]]
     [:.comp-holder
      [:.entry-holder {:margin-top    "40px"
                       :padding-right "40px"
                       :width         "42%"}
       [:.info.rc-label {:position    "relative"
                         :top         "-8px"
                         :line-height "10px"}]]
      [:.title {:margin         "20px 0 0"
                :font-size      "36px"
                :letter-spacing "-.2px"
                :font-family    ladders-font
                :color          charcoal-light}]
      [:.holder
       [:.form-control {:width  "100%"
                        :height "40px"}]]
      [:.salary-labels {:width "490px"}]
      [:.comp-label {:width       "180px"
                     :text-align  "left"
                     :color       ladders-text-dark
                     :font-size   "16px"
                     :line-height "27px"}]
      [:.comp-info {:width       "200px"
                    :text-align  "left"
                    :color       ladders-text-dark
                    :font-size   "16px"
                    :font-family ladders-font-bold}]
      [:.comp-range
       [:.rc-label {:margin         "12px 5px"
                    :color          charcoal-light
                    :font-size      "10px"
                    :text-transform "uppercase"}]
       [:.holder {:margin-top "0px"}]]
      [:.total-comp {:padding       "50px 0 50px 111px"
                     :width         "550px"
                     :border-radius "12px"
                     :align-self    "flex-start"
                     :background    (str "url(" (img-url :line) ") transparent no-repeat 0 40px")}
       [:.error-msg {:margin-bottom "19px"}]
       [:.amount {:width       "100%"
                  :font-size   "36px"
                  :font-family ladders-font-bold
                  :color       ladders-text-dark}]
       [:.amount-sub {:width      "100%"
                      :font-size  "18px"
                      :margin-top "30px"
                      :color      "#4a4a4a"}]]]
     [:.error-msg {:margin-top "5px"}]
     [:.company-holder
      [:.rc-dropdown
       [:&.error {:border "1px solid #ed4e3d"}]]
      [:.holder {:margin-top "10px"}]
      [:.title {:margin         "20px 0 10px"
                :font-size      "36px"
                :letter-spacing "-.2px"
                :font-family    ladders-font
                :color          charcoal-light}]
      [:.form-control {:margin-right "20px"
                       :width        "490px"
                       :height       "48px"
                       :font-size    "16px"}]
      [:.dropdown {:margin-right "20px"
                   :width        "490px"
                   :border       "solid 1px #979797"}]
      [:.company-name-holder {:margin-top "10px"}
       [:.form-control
        [:&:disabled
         {:background-color "#fff"}]]]
      [:.location-holder {:margin-top "20px"}
       [:.selected-locations {:width "490px"}
        [:.location-tags {:flex-wrap "wrap"}]]
       [:.location-tag {:float         "left"
                        :color         ladders-teal
                        :font-family   ladders-font-bold
                        :font-size     "14px"
                        :border        (str "1px solid " ladders-teal)
                        :border-radius "5px"
                        :margin        "0 10px 10px 0"}
        [:.loc {:padding "4px 17px 4px 13px"}]
        [:.btn {:padding     "4px 10px 0 0"
                :background  "transparent"
                :color       ladders-teal
                :font-size   "14px"
                :outline     "none"
                :font-family ladders-font-boldest}
         [:&:active {:box-shadow "none"}]]]]
      [:.rec-holder {:margin-top "0"
                     :padding    "7px"
                     :background lighter-grey}
       [:img {:height        "34px"
              :width         "34px"
              :border-radius "20px"}]
       [:.name {:padding     "5px 10px 0"
                :margin      "0"
                :font-size   "18px"
                :font-family ladders-font-bold}]]]
     [:.rec-profile {:width "490px"}]
     [:.preview-holder {:margin-top "60px"
                        :height     "120px"
                        :background white}]
     [:.preview-btn {:margin           "27px auto 5px"
                     :width            "300px"
                     :padding          "16px 0"
                     :font-size        "20px"
                     :letter-spacing   "0.4px"
                     :background-color ladders-orange}
      [:&:hover {:background-color ladders-orange}]
      [:&:active {:background-color ladders-orange}]
      [:&:focus {:background-color ladders-orange}]]]

    [:.check-holder
     [:.info {:color ladders-text-dark}]
     [:span {:top "2px"}]]
    [:.sub-info {:height    "17px"
                 :font-size "14px"
                 :color     "#999999"
                 :position  "relative"
                 :top       "-3px"
                 :right     "21px"}]
    [:.ql-toolbar.ql-snow {:border "1px solid #979797"}]
    [:.ql-container.ql-snow {:border "1px solid #979797"}]
    [:.ql-container {:height "initial"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Post Preview
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.post-job-preview
     [:.job-desc {:font-size "20px"}]
     [:.bar {:width      "60%"
             :height     "8px"
             :background ladders-teal}]
     [:.arrow {:width         "0"
               :height        "0"
               :border-top    "4px solid transparent"
               :border-bottom "4px solid transparent"
               :border-left   (str "4px solid " ladders-teal)}]
     [:.progress-status {:background        white
                         :-webkit-animation "slideInFromLeftSecond 1s ease-out 0s 1"
                         :animation         "slideInFromLeftSecond 1s ease-out 0s 1"}
      [:.bar {:background ladders-teal}]
      [:.arrow {:width         "0"
                :height        "0"
                :border-top    "4px solid transparent"
                :border-bottom "4px solid transparent"
                :border-left   (str "4px solid " ladders-teal)}]]
     [:.dark {:position absolute
              :width    "100%"}
      [:.bar {:width      "30%"
              :background ladders-teal-dark
              :z-index    "10"}]
      [:.arrow {:border-left (str "4px solid " ladders-teal-dark)}]]
     [:.banner-container {:margin     "0 auto"
                          :height     "100px"
                          :width      "100%"
                          :background white}
      [:&.bottom
       [:.button-group {:width  content-width
                        :margin "0 auto"}]]
      [:.banner-title {:font-size      "36px"
                       :letter-spacing "-.2px"
                       :font-family    ladders-font-bold}]
      [:.button-group {:margin-left "auto"}
       [:.btn {:width     "300px"
               :font-size "20px"
               :margin    "0 5px"
               :padding   "16px 0"
               :border    (str "1px solid " ladders-orange)}]
       [:.btn-border {:width      "150px"
                      :background white
                      :padding    "16px 0"
                      :color      ladders-orange}]]
      [:.title-box {:margin-top 0}]]
     [:.total-salary {:font-family ladders-font-bold}]
     [:.salary-details {:font-size "16px"}]
     [:.publish-btn {:margin           "27px auto 5px"
                     :width            "300px"
                     :padding          "16px 0"
                     :font-size        "20px"
                     :letter-spacing   "0.4px"
                     :background-color ladders-orange}
      [:&:hover {:background-color ladders-orange}]
      [:&:active {:background-color ladders-orange}]
      [:&:focus {:background-color ladders-orange}]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Publish Job
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.post-job-publish
     [:.progress-status {:background        white
                         :-webkit-animation "slideInFromLeftThird 1s ease-out 0s 1"
                         :animation         "slideInFromLeftThird 1s ease-out 0s 1"}
      [:.bar {:width      "100%"
              :height     "8px"
              :background ladders-teal}]
      [:.arrow {:width         "0"
                :height        "0"
                :border-top    "4px solid transparent"
                :border-bottom "4px solid transparent"
                :border-left   (str "4px solid " ladders-teal)}]]
     [:.dark {:position absolute
              :width    "70%"}
      [:.bar {:width      "100%"
              :height     "8px"
              :background ladders-teal-dark
              :z-index    "10"}]
      [:.arrow {:width         "0"
                :height        "0"
                :border-top    "4px solid transparent"
                :border-bottom "4px solid transparent"
                :border-left   (str "4px solid " ladders-teal-dark)}]]
     [:.banner-container {:margin     "0 auto"
                          :width      "100%"
                          :background white}
      [:.title-box {:flex       "1 0 auto"
                    :margin-top 0}]
      [:.banner-title {:margin         "24px 0"
                       :font-size      "36px"
                       :letter-spacing "-.2px"
                       :font-family    ladders-font-bold}
       [:&.promote {:margin         "35px 0 0 "
                    :font-size      "36px"
                    :letter-spacing "-.2px"
                    :font-family    ladders-font}]]
      [:banner-subtitle {:font-size   "20px"
                         :font-family ladders-font-bold}]]
     [:.title {:margin         "42px 42px 28px"
               :font-size      "36px"
               :letter-spacing "-.2px"
               :font-family    ladders-font-bold}]
     [:.bullet {:margin      "0 42px"
                :font-size   "20px"
                :line-height "30px"}]
     [:.blt {:margin-right "15px"}]
     [:.details {:margin    "0 68px"
                 :font-size "14px"
                 :color     "#999"}]
     [:.detail {:font-size "14px"
                :color     "#999"}]
     [:span.pjl-count {:color       ladders-orange
                       :font-size   "40px"
                       :font-family ladders-font-boldest}]

     [:.promote-view {:margin     "40px 20px 0 0"
                      :width      "618px"
                      :height     "420px"
                      :background white}
      [:.publish-btn {:margin     "37px auto"
                      :padding    "16px 0"
                      :width      "303px"
                      :background ladders-orange
                      :font-size  "20px"}]]
     [:.publish-view {:margin-top "40px"
                      :width      "380px"
                      :height     "420px"
                      :background white}
      [:.publish-btn {:margin      "86px auto 0"
                      :padding     "16px 16px"
                      :background  white
                      :border      (str "1px solid " ladders-orange)
                      :color       ladders-orange
                      :font-size   "20px"
                      :font-family ladders-font-bold}]]
     [:.promote-btn {:margin      "52px 0 49px"
                     :padding     "16px 51px"
                     :background  ladders-orange
                     :font-family ladders-font-bold
                     :font-size   "20px"}]
     [:.promote-holder {:margin-bottom "14px"}]
     [:.promote-pub {:width "745px"}
      [:.title {:margin         "46px 0 20px"
                :font-family    ladders-font
                :font-size      "36px"
                :letter-spacing "-.2px"
                :line-height    "1.12"}]
      [:.sub-title {:margin-top  "19px"
                    :font-size   "28px"
                    :font-family ladders-font}]
      [:.bullet {:margin "0 5px"}]
      [:.details {:margin "0 27px"}]
      [:.promote-btn {:margin  "35px 0 0"
                      :padding "16px 76px"}]
      [:.publish-btn {:margin-bottom "29px"
                      :padding       "10px 16px"
                      :background    "none"
                      :color         ladders-orange
                      :font-size     "20px"
                      :font-family   ladders-font}
       [:&.underline {:margin-top      "19px"
                      :text-decoration "underline"
                      :padding         "0"}]]
      [:.qual {:margin "0"
               :width  "690px"}]]
     [:.promote-banner {:margin-top "55px"
                        :width      "255px"
                        :height     "449px"}]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Pagination
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.pagination
     [:.left-pagination :.right-pagination {:width "25%"}]
     [:.pages {:width "50%"}]
     [:.link {:font-size  "13px"
              :padding    "6.5px 8.45px"
              :border     (str "solid 1px " ladders-teal)
              :text-align "center"
              :cursor     "pointer"
              :margin     "0 .15em"}
      [:&.selected :&:hover {:background-color ladders-teal
                             :color            "#ffffff"}]
      [:&.next :&.prev {:padding-top ".3em"}]
      [:&.next {:margin-left ".5em"}]
      [:&.prev {:margin-right ".5em"}]
      [:.arrow {:font-size "1.25em"}]]
     [:.ellipsis {:padding "0 .25em"
                  :color   "#959595"}]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Preview Job Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.preview.job
     [:.content-holder
      [:.left-col {:width "70%"}]
      [:.right-col {:width "30%"}
       [:.apply-btn {:margin-right "0"
                     :margin-left  "0"}]]]]



    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Read More Module
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.read-more-expand {:background-color white
                         :padding          "10px"
                         :text-align       "center"
                         :margin-top       "50px"
                         :margin-bottom    "10px"
                         :align-items      "center"
                         :justify-content  "center"}
     [:.read-more-label {:font-size "18px"
                         :margin    "0"
                         :color     charcoal}]]
    [:.job-desc.read-more {:height   "360px"
                           :overflow "hidden"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Job Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.job
     [:.title-container {:min-height "0"}
      [:.title {:margin "10px 0 0"}]
      [:.title-box {:justify-content "space-between"
                    :margin-top      "0"}
       [:.job-title {:font-size   "24px"
                     :font-family ladders-font-bold
                     :font-weight "300"
                     :line-height "1.33"}]
       [:.title-in {:line-height "1.33"
                    :font-size   "24px"
                    :font-family ladders-font-bold
                    :padding     "0 5px"}]
       [:.back-to-jobs {:margin-top  "16px"
                        :font-family ladders-font-bold
                        :font-size   "14px"
                        :color       ladders-greeny-blue}]
       [:.rc-hyperlink-href {:margin-top  "40px"
                             :font-family ladders-font-bold
                             :font-size   "14px"
                             :color       ladders-greeny-blue}]
       [:.chosen-container-single
        [:.chosen-single {:font-family ladders-font
                          :background  white
                          :padding     "0"
                          :height      "auto"}
         [:span {:font-size       "22px"
                 :line-height     "30px"
                 :color           ladders-teal
                 :text-decoration "underline"
                 :padding         "0 4px"}]]]
       [:.label {:color       grey
                 :font-family ladders-font-bold
                 :font-size   "14px"}]]]
     [:.nav-tabs {:margin "25px 0 0"}]
     [:.content-holder
      [:.cols {:align-content "space-between"}]
      [:.job-title {:font-size      "36px"
                    :letter-spacing "-.2px"
                    :font-family    ladders-font
                    :margin         "40px 0 20px"
                    :max-width      "760px"}]
      [:.save-job {:margin      "7px 20px 0 10px "
                   :text-align  "center"
                   :font-size   "20px"
                   :font-family ladders-font-boldest
                   :color       ladders-teal
                   :opacity     "0.5"}]
      [:.apply-btn {:margin      "40px 5px"
                    :padding     "16px 55px"
                    :background  ladders-teal
                    :color       white
                    :font-size   "20px"
                    :font-family ladders-font-bold
                    :opacity     "0.5"}]
      [:.sal-rec {:margin      "30px 0"
                  :font-size   "20px"
                  :font-family ladders-font-bold}]]
     [:.divider {:width         "100%"
                 :height        "2px"
                 :background    white
                 :margin-bottom "30px"}]
     [:.logo {:margin "3px 10px 0 0"
              :height "40px"}]
     [:.job-details {:font-size   "18px"
                     :font-family ladders-font-bold}]
     [:.job-desc {:font-family ladders-font-bold}]
     [:.promote-job-btn {:margin-top  "15px"
                         :font-size   "20px"
                         :font-family ladders-font-bold}]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Post Thank You
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.post.thank-you
     [:.table [:tbody [:th {:border "none"}]]]
     [:.table {:margin-top  "40px"
               :font-size   "13px"
               :font-family ladders-font-bold}
      [:td {:border-top "1px solid #fff"}
       [:td {:border "none"}]]
      [:td.title {:width "170px"}
       [:td.company {:width "170px"}]]
      [:th {:font-size   "13px"
            :font-family ladders-font-boldest
            :font-weight "400"
            :line-height "24px"
            :border-top  "none"}]
      [:.location {:min-width "150px"}]]
     [:.candidate-title {:font-size  "24px"
                         :margin-top "60px"}
      [:span {:font-family ladders-font-bold}]]
     [:.banner-wrapper {:background-color "#e7f0f0" :width "100%" :padding-top "14px" :padding-bottom "14px"}
      [:.banner-content {:width  "1000px"
                         :margin "0 auto"}]]
     [:.banner-wrapper-bottom {:background-color "#e7f0f0" :width "100%" :padding-top "18px" :padding-bottom "18px"}
      [:.banner-content {:width  "1000px"
                         :margin "0 auto"}]]
     [:.banner-title {:font-size   "36px"
                      :margin      "0 0 20px 0"
                      :padding-top "32px"}]
     [:.banner-container {:position   "relative"
                          :background white
                          :width      "100%"
                          :margin     "0 auto"
                          :padding    "30px 0"}
      [:.banner-container {:width content-width}]
      [:.banner-title {:font-size   "36px"
                       :margin      "0 0 20px 0"
                       :padding-top "32px"}]
      [:.banner-text {:font-size "16px" :padding-bottom "28px"}]
      [:.title-box {:width      "100%"
                    :margin-top 0}]]
     [:.progress-bar {:height           "10px"
                      :background-color "#00828b"
                      :border           "solid 1px #00828b"
                      :width            "100%"}]
     [:.promo-box {:width "50%"}]
     [:.you-have-job-prom {:font-size   "24px"
                           :font-weight "500"
                           :color       "#ef8f1c"
                           :font-family ladders-font-boldest}]
     [:.job-prom-container {:padding-bottom "28px"}
      [:.you-have-job-prom {:font-size   "24px"
                            :font-weight "500"
                            :color       "#ef8f1c"
                            :font-family ladders-font-boldest}]
      ]
     [:button.orange {:color            "white"
                      :background-color "#ef8f1c"
                      :width            "300px"
                      :padding          "10px"
                      :border           "none"
                      :border-radius    "0"
                      :margin-top       "10px"
                      :font-size        "20px"}
      [:label {:font-size   "20px"
               :font-family ladders-font
               :font-weight "200"}]]
     [:.btn-border {:width     "300px"
                    :font-size "18px"}]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+ Checkout Styles
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.iframe-container {}]
    [:.modal-window {:position "relative"
                     :width    "928px"
                     :height   "637px"
                     :display  "block"
                     :padding  "0"}]
    [:.iframe-window {:width   "100%"
                      :height  "100%"
                      :border  "none"
                      :padding "0"}]
    [:.close-btn {:position "absolute"
                  :top      "0"
                  :right    "0"}]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+ Candidate Profile
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.candidate-profile
     [:.get-full-access-bar {:font-size        "16px"
                             :font-family      ladders-font-bold
                             :background-color even-lighter-grey
                             :padding          "10px 0 5px"}
      [:.lock {:width  "10px"
               :height "13px"
               :margin "4px 5px 0 0"}]
      [:a {:margin "0 0 0 3px"}]]
     [:.candidate-header-holder {:background white}]
     [:.candidate-scroller {:width         content-width
                            :margin        "0 auto"
                            :padding       "16px 0"
                            :font-size     "13px"
                            :font-family   ladders-font-bold
                            :border-bottom (str "solid 2px " ladders-bg-grey)}
      [:.previous-candidate :.next-candidate {:color grey}]
      [:a.previous-candidate
       [:&:hover {:color ladders-teal}
        [:&:before {:background      (str "transparent url(" (img-url :caret-teal) ") 0 0 no-repeat")
                    :background-size "12px 9px"}]]]
      [:.previous-candidate {:padding "0 0 0 18px"}
       [:&:before {:content  "''"
                   :position "absolute"
                   :width    "16px"
                   :height   "16px"}]
       [:&:before {:background        (str "transparent url(" (img-url :caret) ") 0 0 no-repeat")
                   :margin-left       "-18px"
                   :margin-top        "-1px"
                   :-webkit-transform "rotate(270deg)"
                   :-moz-transform    "rotate(270deg)"
                   :-ms-transform     "rotate(270deg)"
                   :-o-transform      "rotate(270deg)"
                   :transform         "rotate(270deg)"}]]
      [:a.next-candidate
       [:&:hover {:color ladders-teal}
        [:&:after {:background      (str "transparent url(" (img-url :caret-teal) ") 0 0 no-repeat")
                   :background-size "12px 9px"}]]]
      [:.next-candidate {:padding "0 19px 0 0"}
       [:&:after {:content  "''"
                  :position "absolute"
                  :width    "16px"
                  :height   "16px"}]
       [:&:after {:background        (str "transparent url(" (img-url :caret) ") 0 0 no-repeat")
                  :margin            "3px 0 0 3px"
                  :-webkit-transform "rotate(90deg)"
                  :-moz-transform    "rotate(90deg)"
                  :-ms-transform     "rotate(90deg)"
                  :-o-transform      "rotate(90deg)"
                  :transform         "rotate(90deg)"}]]]
     [:.candidate-info-holder {:width      content-width
                               :margin     "0 auto"
                               :padding    "0 10px 20px 20px"
                               :min-height "220px"}]
     [:.candidate-header {:margin "20px 0 0"}
      [:.candidate-info {:width       "75%"
                         :font-size   "16px"
                         :font-family ladders-font-bold}
       [:.candidate-info-divider {:margin "0 5px"}]
       [:.candidate-name {:font-size   "36px"
                          :font-family ladders-font
                          :line-height "56px"}]
       [:.candidate-history {:margin      "10px 0 0"
                             :line-height "24px"}
        [:.time-in-role {:color grey}]]]
      [:.candidate-actions {:width "25%"}
       [:.action-btn-holder {:margin "10px 0 0"}
        [:.action-btn {:width       "105px"
                       :height      "50px"
                       :font-size   "16px"
                       :font-family ladders-font-bold}]
        [:div:first-child
         [:.action-btn {:margin "0 3px 0 0"}]]
        [:div
         [:&:only-child
          [:.action-btn {:width  "213px"
                         :margin 0}]]]]
       [:.more-toggle {:margin "10px 0 0"}
        [:.more-actions :.more-button {:width       "213px"
                                       :margin      0
                                       :font-size   "13px"
                                       :font-family ladders-font-boldest
                                       :line-height "24px"}]
        [:.more-button {:height  "24px"
                        :padding 0}]
        [:.more-actions {:padding     "10px"
                         :font-family ladders-font-bold}
         [:.more-actions-link {:padding "0 10px 5px"}]]]]]
     [:.candidate-info-divider {:color  grey
                                :margin "0 10px"}]
     [:.resume-actions
      [:.candidate-info-divider {:font-size "16px"
                                 :margin    "0 5px"}]
      [:.resume-action {:font-size   "16px"
                        :font-family ladders-font-bold}]]
     [:.resume-holder {:background ladders-bg-grey}]
     [:.resume-iframe {:border none}]
     [:.resume-upload-date-tag {:position      "absolute"
                                :top           "72px"
                                :right         "52px"
                                :background    "#d6d6d6"
                                :padding       "5px 20px"
                                :border-radius "5px"
                                :font-size     "14px"
                                :font-family   ladders-font-bold}]
     [:.candidate-last-active {:color         orange
                               :font-family   ladders-font-boldest
                               :font-size     "14px"
                               :margin-bottom "8px"
                               :display       "flex"
                               :align-items   "center"}
      [:&:before {:background-image (str "url(" (img-url :clock) ")")
                  :background-size  "24px 24px"
                  :display          "inline-block"
                  :width            "24px"
                  :height           "24px"
                  :margin-right     "8px"
                  :content          "''"}]]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+ Cropper
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.cropper-holder
     [:.img-holder {:height "480px"
                    :width  "540px"}
      [:img {:max-width  "100%"
             :max-height "100%"}]]
     [:button {:height "50px"}]
     [:.zoom-holder
      [:div:first-child {:margin "0 20px 0 0"}]
      [:.zoom {:padding "0 30px"}]]
     [:.crop-btn {:font-size "16px"
                  :padding   "0 80px"}]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+ Profile Image Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.modal
     [:&.profile-image-modal
      [:.modal-body {:width "auto"}]]]]

   [:#app

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Superuser
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.superuser
     [:.nav-tabs {:margin "19px 0 0"}]
     [:.tab-body {:padding "10px 0"}]
     [:.superuser-form {:padding "15px"
                        :width   "50%"}
      [:.btn {:margin    "10px 0 0"
              :font-size "16px"}]]
     [:.inline-form
      [:.holder {:width "80%"}]
      [:.btn {:height  "35px"
              :padding "6px 10px"}]]
     [:.edit-permissions-form {:margin "15px 0 0"}]
     [:.unlimited-disclaimer {:font-size "13px"
                              :margin    "0 0 10px"}]
     [:.sales-inventory-form {:width "100%"}]
     [:.inventory-dropdown-holder {:margin "0 0 10px"}
      [:.inventory-dropdown {:width  "100%"
                             :margin "5px 0 0"}]]
     [:.load-inventory-form {:width "75%"}
      [:.inline-form
       [:.holder {:width "50%"}]]
      [:.btn {:margin-left "10px"}]]
     [:.inventory-action-form {:width "25%"}]
     [:.inventory-table {:margin "10px 0 0"}
      [:th :td
       [:&.action {:width "118px"}]
       [:&.type {:width "165px"}]
       [:&.qty {:width "70px"}]
       [:&.insert-date {:width "118px"}]]]
     [:.feature-job-form {:margin "10px 0 0"}
      [:.promotion-form {:margin     "10px 0 0"
                         :padding    "10px 0 0"
                         :border-top "1px dotted black"}
       [:.feature-job-dates {:margin "10px 0 0"}]]]]


    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Onboarding Page
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.dropdown {:border "none"}]

    [:.signup-form-holder {:width       "620px"
                           :margin      "100px auto"
                           :font-family ladders-font
                           :font-weight "100"}
     [:.title {:width          "100%"
               :font-size      "36px"
               :letter-spacing "-.2px"
               :font-weight    "100"
               :line-height    "1.12"
               :text-align     "center"
               :color          "#333333"
               :height         "62px"
               :margin         "0px"}]
     [:>p {:color       "#666666"
           :line-height "1.33"
           :font-size   "24px"
           :height      "32.7px"}]
     [:.continue {:width            "100%"
                  :height           "56px"
                  :background-color "#3fb2bb"
                  :font-size        "20px"
                  :margin           "25px 5px"}]]
    [:.signup-form {:font-family   ladders-font
                    :font-weight   "100"
                    :margin-bottom "25px"}
     [:* {:font-family (str ladders-font "!important")}]
     [:.error-border {:border "1px solid #ff3333 !important"}]



     [:.extra-input-msg {:font-size      "14px"
                         :font-weight    "300"
                         :line-height    "14px"
                         :letter-spacing "0.5px"
                         :color          "#666666"
                         :margin         "5px 0 0 5px"}]

     [:input {:border      "solid 0.5px #979797"
              :height      "40px"
              :line-height "40px"
              :font-size   "18px"
              :color       "#444"}]

     [:.holder {:padding-right "5px"
                :width         "100%"}
      [:.labels {:margin 0}]]

     [:.rc-label {:height         "10px"
                  :font-size      "10px"
                  :color          "#666666"
                  :letter-spacing "0.5px"
                  :line-height    "1"}]

     [:.name-inputs :.phone-inputs :.company-inputs
      [:.holder {:width "50%"}]]

     [:.input-label {:margin "15px 0 5px"}]

     [:.rc-v-box
      [:.rc-label {:margin         "15px 0 5px"
                   :text-transform "uppercase"}]]

     [:.input-box {:margin-bottom "10px"}]

     [:.form-control
      [:&:focus {:border     "solid 0.5px #3fb2bb"
                 :box-shadow "none"}]]

     [:.company-type {:width  "100%"
                      :height "40px"
                      :flex   "0 0 auto !important"}
      [:a {:height     "40px"
           :border     "solid 0.5px #979797"
           :box-shadow "none !important"}]]

     [:.error-border-sdd
      [:a {:border "solid 1px #ff3333 !important"}]]

     [:.chosen-single {:padding "0 0 0 11px"}
      [:span {:font-size   "18px"
              :line-height "38px"
              :color       "#444"}]]

     [:.error-msg {:color          "#ff3333"
                   :font-size      "12px"
                   :line-height    "25px"
                   :letter-spacing "0.2px"}]
     [:.password-lable {:color         "#848383"
                        :margin-top    "5px"
                        :margin-bottom "2px"
                        :font-size     "14px"
                        :line-height   "18px"
                        }]
     [:.password-hint-container
      [:.error-msg {:font-size   "14px"
                    :line-height "16px"
                    :position    "relative"
                    }]
      [:.valid-msg {:font-size      "14px"
                    :color          "#666666"
                    :line-height    "16px"
                    :letter-spacing "0.2px"
                    :position       "relative"}
       [:.check-ok {:width        "5px"
                    :height       "9px"
                    :border       "solid #06c20f"
                    :border-width "0 2px 2px 0"
                    :position     "absolute"
                    :left         "134px"
                    :transform    "rotate(40deg)"
                    :top          "4px"}]]]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Get-Full-Access
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   [:#app
    [:.get-full-access {:background        (str "url(" (img-url :background-img-src) ")")
                        :background-size   "cover"
                        :background-repeat "no-repeat"
                        :background-color  ladders-black}
     [:&.main {:min-height "100%"}]
     [:.header {:font-family ladders-font-bold
                :background  ladders-black}
      [:.logo {:height "30px"
               :margin "10px 0"}]
      [:.header-content {:background ladders-black
                         :color      white
                         :margin     "0 auto"}
       [:.home-icon {:margin "0 auto"}]]]
     [:.body
      [:.headline-holder {:width      "100%"
                          :height     "56px"
                          :margin-top "61px"}
       [:.headline {:width       "830px"
                    :font-family ladders-font
                    :font-size   "80px"
                    :font-weight "100"
                    :line-height "0.7"
                    :text-align  "center"
                    :color       "#ffffff"
                    :margin      "0 auto"}]]
      [:.sub-headline-holder {:width      "100%"
                              :margin-top "26px"}
       [:.sub-headline {:width       "830px"
                        :height      "32px"
                        :font-family ladders-font
                        :font-size   "24px"
                        :font-weight "300"
                        :line-height "1.33"
                        :text-align  "center"
                        :color       "#ffffff"
                        :margin      "0 auto"}]]
      [:.form-box {:width            "830px"
                   :background-color "#f5f1ec"
                   :margin           "23px auto 0"}
       [:.contact-sales-form {:width   "475px"
                              :padding "10px 50px 0 30px"}
        [:.form-row {:width  "395px"
                     :margin "21px 0 0"}
         [:.form-label {:font-size   "14px"
                        :font-family ladders-font
                        :font-weight "300"
                        :line-height "0.71"
                        :text-align  "left"
                        :color       "#666666"}]
         [:.form-element {:width "50%"}
          [:&.full-width {:width "100%"}]
          [:input :textarea {:border        "none"
                             :border-radius "0"}]
          [:input {:width  "185px"
                   :margin "9px 0 0"}]
          [:textarea {:margin "9px 0 0"}]
          [:+ [:.form-element {:margin-left "14px"}]]]
         [:.error-msg {:font-size "12px"
                       :color     "red"}]]
        [:.submit-btn {:margin           "16px 0 38px"
                       :width            "100%"
                       :height           "56px"
                       :background-color ladders-orange
                       :font-size        "20px"
                       :font-family      ladders-font
                       :font-weight      "300"
                       :text-align       "center"
                       :color            "#ffffff"
                       :border-radius    "0"}]]
       [:.full-access-info {:width            "355px"
                            :background-color "#ece6df"}
        [:.info-title {:width       "221px"
                       :height      "32px"
                       :font-size   "20px"
                       :font-family ladders-font-boldest
                       :margin      "67px 59px 0 75px"}]
        [:.info-divider {:width   "236px"
                         :height  "2px"
                         :opacity "0.15"
                         :border  "solid 1px #000000"
                         :margin  "2px auto 11px"}]
        [:ul {:margin     "0 0 0 78px"
              :list-style "disc"
              :padding    "0"}
         [:li {:font-size   "16px"
               :font-family "HelveticaNeue-Light"
               :font-weight "300"
               :line-height "2.5"
               :color       "#aaaaaa"}
          [:span {:color "#333333"}]]]]]
      [:.legal {:width  "830px"
                :height "17px"
                :margin "15px auto 0"}
       [:.legal-holder {:margin "0 auto"}
        [:.legal-text {:padding     "0 17px"
                       :color       lighter-grey
                       :font-family ladders-font
                       :font-size   "14px"
                       :font-weight "300"}
         :&a {:text-decoration "none"
              :color           lighter-grey}]
        [:.legal-divider {:color lighter-grey}]]]]
     [:.alert-bar {:background-color alert-success
                   :margin-bottom    "10px"
                   :width            "100%"
                   :position         "relative"}
      [:.alert-message {:padding    "10px 0"
                        :text-align "center"}]
      [:.close-alert-bar {:position    absolute
                          :right       "20px"
                          :top         "0"
                          :line-height "50px"
                          :font-family ladders-font-boldest
                          :cursor      "pointer"}]]]

    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
    ;_+
    ;_+  Create Account Modal
    ;_+
    ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    [:.create-account-modal {:overflow-y "scroll"}
     [:.modal-box {:width "690px"}
      [:.modal-body {:width "100%"}
       [:.btn {:width            "100%"
               :background-color ladders-teal
               :margin           "10px 0 0"}
        [:&:hover {:background-color ladders-teal-dark}]]]
      [:.modal-title {:font-family    ladders-font
                      :color          light-grey
                      :font-size      "36px"
                      :letter-spacing "-.2px"
                      :margin-bottom  "0"}]
      [:.modal-sub-header {:font-size "16px"
                           :margin    "5px 0 15px"}]
      [:.disclaimer {:color       grey
                     :font-size   "16px"
                     :font-family ladders-font-bold
                     :margin      "10px 0"}]
      [:.already-a-member {:font-size "16px"
                           :color     grey
                           :margin    "15px 0 0"}
       [:.login-link {:margin "0 0 0 5px"}]]]]]



   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  FAQs
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:.faqs
    [:.content-holder {:margin-top "108px"}]
    [:.title {:font-size     "52px"
              :margin-bottom "40px"}]
    [:.sublinks {:margin-bottom "10px"}]
    [:.answers [:.text {:font-size   "20px"
                        :width       "800px"
                        :font-family ladders-font-bold}]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Terms and conditions
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   [:.terms-and-conditions
    [:.content-holder {:margin-top "108px"}]
    [:.title {:font-size     "52px"
              :margin-bottom "40px"}]
    [:.terms {:font-size   "13px"
              :font-family ladders-font-bold}]]


   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Referral Hiring
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app [:.referral-hiring {:padding-bottom "40px"}
           [:.title {:font-size "52px" :margin-top "40px"}]
           [:.deck {:text-align "center"}
            [:.learn-more {:cursor              "pointer"
                           :padding-right       "16px"
                           :background-repeat   "no-repeat"
                           :background-position "center right"}
             [:&.carat-down {:background-image (str "url(" (img-url :down-carat) ")")}]
             [:&.carat-up {:background-image (str "url(" (img-url :up-carat) ")")}]]]
           [:.more-text {:margin-top "30px"}
            [:p {:margin-bottom "20px"}]]
           [:.infographic {:margin-top "50px"}
            [:.item {:width               "13%"
                     :background-size     "95px"
                     :background-color    "transparent"
                     :background-repeat   "no-repeat"
                     :position            "relative"
                     :padding-top         "100px"
                     :text-align          "center"
                     :background-position "top center"}
             [:&.enroll {:background-position "top left" :background-image (str "url(" (img-url :signup) ")")}]
             [:&.tell {:background-image (str "url(" (img-url :wordout) ")")}]
             [:&.hires {:background-image (str "url(" (img-url :free_referrals) ")")}]
             [:.text {:font-size "24px"}]
             [:.arrow {:background-image    (str "url(" (img-url :arrow) ")")
                       :background-color    "transparent"
                       :background-repeat   "no-repeat"
                       :background-position "top left"
                       :width               "237px"
                       :height              "14px"
                       :position            "absolute"
                       :top                 "40px"
                       :left                "145px"}]]]
           [:.form-row {:margin-top "50px"}
            [:.referral-form {:width "50%"}
             [:.form-header {:font-size "24px"}]
             [:.input-label {:color "#999999" :font-size "16px"}]
             [:.form-control {:font-size "21px" :height "48px" :margin-bottom "10px"}]
             [:.referral-btn {:width "100%" :font-size "16px"}]]
            [:.image-wrapper {:align-self "center"}]
            [:.referral-signup-image {:background-image    (str "url(" (img-url :referral-signup) ")")
                                      :background-color    "transparent"
                                      :background-size     "320px"
                                      :background-position "bottom left"
                                      :width               "320px"
                                      :height              "320px"}]]
           [:.roles-tooltip {:width     "280px"
                             :margin    "25px 40px"
                             :font-size "14px"}]
           [:.info-tool-tip {:height "22px"}]
           [:.tooltip-wrapper {:position "relative"}]
           ]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Email Verification Page
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app
    [:.email-verification
     [:.content {:max-width     "660px"
                 :padding-top   "100px"
                 :margin-bottom "100px"}
      [:.teal {:color ladders-teal}]
      [:h1 {:font-size      "36px"
            :letter-spacing "-.2px"}]
      [:.desc {:font-size "24px"}]
      [:.code-input-view {:margin "30px 0 0"}
       [:.labels {:margin-bottom "5px"}]
       [:.input-label {:font-size "10px"}]
       [:input {:height    "48px"
                :font-size "21px"
                :padding   "0.3em 0.8em"
                :width     "500px"}]
       [:.btn {:height    "48px"
               :width     "150px"
               :font-size "16px"}]]
      [:.error {:font-size     "16px"
                :color         "#ff3226"
                :margin-bottom "0"}]
      [:.re-send {:font-size  "16px"
                  :margin-top "20px"}
       [:.link {:cursor "pointer"}]]]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Pricing Page
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app
    [:.pricing {:width       "100%"
                :font-family ladders-font}
     [:.content-holder {:max-width "1000px"
                        :margin    "0 auto"}]
     [:h1 {:font-size   "36px"
           :font-family ladders-font
           :margin-top  "77px"}]
     [:h2 {:font-family ladders-font-boldest}]
     [:.pricing-table {:background-color white
                       :border-radius    "7px"
                       :box-shadow       "0 2px 4px 0 #d6d6d6"
                       :margin           "30px 0 50px 0"}
      [:table {:width "100%"}]
      [:tr {:border-bottom "1px solid #d6d6d6"}]
      [:th {:font-family ladders-font
            :padding     "10px 30px"
            :width       "310px"
            :font-size   "16px"
            :color       "#333"}]
      [:td {:padding     "10px 20px"
            :width       "230px"
            :text-align  "center"
            :border-left "1px solid #d6d6d6"}]
      [:.cat-title {:font-size   "36px"
                    :line-height "40px"}]]
     [:.title-cell {:vertical-align "top"
                    :font-size      "24px"
                    :color          white}
      [:p {:font-size   "16px"
           :padding     "10px 0"
           :line-height "1.25"}]
      [:&.basic {:background "#b0bd3c"}]
      [:&.individual {:background "#73486f"}]
      [:&.enterprise {:background    "#f87b31"
                      :border-radius "0 7px 0 0"}]]
     [:.btn-container {:border-bottom "none"}
      [:.btn {:font-size     "20px"
              :width         "180px"
              :margin-top    "30px"
              :margin-bottom "56px"}]
      [:.current-plan {:background-color "#ccc"}]]
     [:.mobile-view {:display "none"}]

     (st/at-media {:max-width "768px"}
                  [:.content-holder {:text-align "center"
                                     :width      "100%"}
                   [:.mobile-view {:display "block"}]
                   [:h1 {:margin "30px 0"}]
                   [:h2 {:margin-top "30px"}]
                   [:.car-title {:background-color white
                                 :padding          "20px 0"
                                 :margin           "0"
                                 :border-top       "1px solid #d6d6d6"}]
                   [:.title-cell
                    [:p {:padding "10px 20px"}]
                    [:&.enterprise {:border-radius "0"}]]
                   [:ul {:padding          "20px 0 0 30px"
                         :margin           "0"
                         :background-color white}]
                   [:li {:list-style "none"
                         :font-size  "16px"
                         :text-align "left"
                         :margin     "15px 0"}]
                   [:img {:margin-right "18px"}]]
                  [:.btn-container {:background-color white}]
                  [:.pricing-table {:display "none"}])]]


   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  404 page
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app
    [:.page-404
     [:.info {:max-width "366px"
              :color     "#b19673"
              :margin    "200px 50px 0 545px"}
      [:.link {:color  "#5397e3"
               :cursor "pointer"}]]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  toggle switch
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app
    [:.switch {:position "relative"
               :display  "inline-block"
               :width    "50px"
               :height   "24px"
               :top      "2px"}
     [:input {:display "none"}]]
    [:.switch-label {:position    "absolute"
                     :cursor      "pointer"
                     :padding     "0 0 0 26px"
                     :font-size   "13px"
                     :line-height "24px"
                     :color       white}
     [:&.on {:padding          "0 26px 0 10px"
             :background-color "transparent"}]]
    [:.slider {:position           "absolute"
               :cursor             "pointer"
               :top                "0"
               :left               "0"
               :right              "0"
               :bottom             "0"
               :background-color   "#ccc"
               :transition         ".4s"
               :-webkit-transition ".4s"}
     [:&:before {:position           "absolute"
                 :content            "''"
                 :height             "16px"
                 :width              "16px"
                 :left               "4px"
                 :bottom             "4px"
                 :background-color   white
                 :-webkit-transition ".4s"
                 :transition         ".4s"
                 :border-radius      "50%"}]
     [:&.round {:border-radius "24px"}]]
    [:.on {:background-color ladders-teal}
     [:&:before {:transform         "translateX(26px)"
                 :-webkit-transform "translateX(26px)"
                 :-ms-transform     "translateX(26px)"}]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Recruiter Admin
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [:#app
    [:.recruiter-admin
     [:.content {:padding       0
                 :margin-bottom "60px"}]
     [:.title-container {:min-height "0"}
      [:.title-box {:margin "30px auto"}]]]]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Dashboard
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   [:#app
    [:.dashboard {:font-family      ladders-font-bold
                  :background-color "#f5f5f5"
                  :padding          "30px 0 60px"
                  :font-size        "12px"
                  :color            "#3a3a3a"}
     [:.position-left {:width     "100%"
                       :max-width "170px"}]
     [:.position-middle {:width     "100%"
                         :max-width "510px"}]
     [:.position-right {:width     "100%"
                        :max-width "338px"}]

     [:.top-right {:line-height "28px"}]
     [:.dashboard-list {:margin-bottom "10px"
                        :max-width     "290px"}]
     [:.dashboard-action-btn {:margin     "0 auto"
                              :align-self "center"
                              :background "#00C0D1"}]
     [:.no-data-message {:text-align  "center"
                         :width       "330px"
                         :font-size   "16px"
                         :margin      "20px 0 20px 62px"
                         :font-family ladders-font-bold}]
     [:.no-applicants {:width           "471.84px"
                       :height          "420px"
                       :position        "relative"
                       :top             "-7px"
                       :left            "-12px"
                       :text-align      "center"
                       :background      (str "transparent url(" (img-url :app-demo-placeholder) ") no-repeat 0 15px")
                       :background-size "471.84px"}]
     [:.no-applicants-cover {:width            "100%"
                             :height           "100%"
                             :position         "absolute"
                             :top              "0"
                             :left             "0"
                             :background-color "rgba(187, 187, 187, 0.8)"}]
     [:.no-applicants-message {:background-color white
                               :position         "relative"
                               :padding          "5px 25px"
                               :top              "45%"}]
     [:.no-applicants-promote {:width       "369px"
                               :margin      "0 auto"
                               :top         "10%"
                               :font-size   "14px"
                               :font-family ladders-font-boldest
                               :padding     "10px 25px 30px"}
      [:.promote-btn {:align-self "center"
                      :margin-top "20px"}]]
     [:.app-demo-placeholder {:opacity    "0.6"
                              :margin-top "20px"}]
     [:.zero-state {:font-size "14px"}]
     [:.no-saved-searches-message {:margin "10px 0"}]
     [:.no-jobs-view {:margin "10px 0"}]
     [:.ad-citihire {:font-size "14px"}
      [:.ad-text-citihire {:width "60%"}]
      [:.ad-image-citihire {:width      "96px"
                            :height     "150px"
                            :margin-top "5px"}]]
     [:.btn {:width "165px"}]
     [:.top-stat {:text-align "center"
                  :width      "30%"}
      [:.row1 {:font-size "14px"
               :width     "83%"
               :margin    "0 auto"}]
      [:.row2 {:font-family ladders-font-boldest
               :font-size   "30px"}]
      [:.row3 {:font-size "12px"}]]
     (small-screen [:.top-stat {:width "100%"}])
     [:#barchart-education {:height "100px"}]
     [:.barchart-education-svg {:width "330px"}
      [:rect {:fill "#fbbc48"}]
      [:text {:fill        "#000000"
              :font-family ladders-font
              :font-size   "12px"}]]
     [:.experience-chart-container {:width  "330px"
                                    :height "100px"}]
     [:.barchart-experience
      [:.experience-range
       [:text {:fill        "#000000"
               :font-family ladders-font
               :font-size   "12px"}]]
      [:.applicants {:fill "#F97C0D"}]
      [:.site-average {:fill "#d6d6d6"}]]
     [:.rec {:width     "40%"
             :height    "20px"
             :font-size "12px"
             :color     "#3a3a3a"}
      [:&:before {:position         "relative"
                  :top              "2px"
                  :display          "inline-block"
                  :content          "\"\""
                  :background-color "#F97C0D"
                  :width            "12px"
                  :height           "12px"
                  :margin-right     "5px"}]
      [:&.average-rec
       [:&:before {:background-color "#d6d6d6"}]]]
     [:.salary-chart-container {:width  "330px"
                                :height "24px"}]
     [:.salary-range-1 {:fill "#005221"}]
     [:.salary-range-2 {:fill "#168716"}]
     [:.salary-range-3 {:fill "#76be4e"}]
     [:.salary-range-4 {:fill "#9ee973"}]
     [:.salary-range-5 {:fill "#cdeeac"}]
     [:.salary-chart-legend {:max-width "320px"}
      [:.rec {:width "33%"}
       [:&.salary-range-rec-1 {:width "32%"}
        [:&:before {:background-color "#005221"}]]
       [:&.salary-range-rec-2
        [:&:before {:background-color "#168716"}]]
       [:&.salary-range-rec-3
        [:&:before {:background-color "#76be4e"}]]
       [:&.salary-range-rec-4 {:width "34%"}
        [:&:before {:background-color "#9ee973"}]]
       [:&.salary-range-rec-5
        [:&:before {:background-color "#cdeeac"}]]
       ]]
     [:.loading-circle {:margin-top "100px"}]]
    [:.team-members-summary {:background "#f3ffff"}
     [:.sub-title {:margin "8px 0 0 10px"}]
     [:table {:background "transparent"
              :min-width  "97%"
              :width      "97%"
              :margin     "0 auto"}]
     [:tr {:border           "none"
           :background-color "transparent"}
      [:th {:border-bottom "1px solid #cccccc"
            :text-align    "center"
            :padding       "4px 12px !important"}
       [:&:first-child {:text-align   "left"
                        :padding-left "0 !important"}]
       [:&:last-child {:padding-right "0 !important"
                       :text-align    "right"}]]
      [:td {:text-align "center"
            :padding    "12px 12px 0"}
       [:&:first-child {:text-align   "left"
                        :padding-left "0 !important"}]
       [:&:last-child {:padding-right "0px !important"}]]]]]
   [:#app
    [:.teams-modal
     [:.modal-box {:width "600px"}]
     [:.modal-body :.modal-title {:width "100%"}]]
    [:.confirm-modal
     [:.modal-body :.modal-title {:width "600px"}]]]

   [:#app
    [:.create-project-input {:width "713px"}]
    [:.modal
     [:&.save-candidate
      [:.modal-box {:width      "800px"
                    :max-height "550px"
                    :overflow-y "auto"}]
      [:.create-project-input {:width "380px"}]]]]

   ;; Dropdown

   [:#app
    [:.rc-point-wrapper
     [:.input-group {:width "100%"}
      [:.rc-h-box {:width "100%"}]
      [:label
       [:&.dropdown-button {:width         "100%"
                            :font-size     "16px"
                            :margin        "0 0 12px"
                            :border        "1px solid #cccccc"
                            :border-radius "2px"}]]]]
    [:span.dropdown-button.activator.input-group-addon {:display "none"}]
    [:.chosen-container-active.chosen-container-active
     [:.chosen-single {:box-shadow    none
                       :border        (str "1px solid " bright-teal)
                       :border-bottom white}]
     [:.chosen-drop {:box-shadow    none
                     :border        (str "1px solid " bright-teal)
                     :border-top    none
                     :border-radius 0}
      [:a {:padding 0
           :margin  0}]]]
    [:.component-dropdown
     [:.rc-point-wrapper
      [:.input-group {:width "100%"}
       [:.rc-h-box {:width "100%"}]
       [:label
        [:&.dropdown-button {:width         "100%"
                             :font-size     "14px"
                             :margin        "0 0 12px"
                             :border        "1px solid #cccccc"
                             :border-radius "2px"}]]]]
     [:span.dropdown-button.activator.input-group-addon {:display "none"}]
     [:.rc-dropdown
      [:.chosen-single {:height  "30px"
                        :padding 0}
       [:span {:padding     "7px 4px 6px 12px"
               :height      "30px"
               :font-size   "14px"
               :line-height "14px"
               :color       "#000000"}]]]
     [:.chosen-container-active.chosen-container-active
      [:.chosen-single {:box-shadow    none
                        :border        (str "1px solid " bright-teal)
                        :border-bottom white}]
      [:.chosen-drop {:box-shadow    none
                      :border        (str "1px solid " bright-teal)
                      :border-top    none
                      :border-radius 0}
       [:a {:padding 0
            :margin  0}]]]
     [:.chosen-container
      [:.chosen-results
       [:li {:font-family ladders-font
             :font-size   "14px"}
        [:&:hover {:background "#cccccc"
                   :color      white}]
        [:&.highlighted {:background bright-teal
                         :color      white}]]]]
     [:&.saved-searches {:width "235px"}
      [:.chosen-container {:width "100%"}]
      [:.saved-search-loading-ani
       [:img {:position  "relative"
              :top       "50%"
              :transform "translateY(-50%)"}]]]
     [:&.search-sort-by {:width "127px"}]
     [:&.search-view {:width "125px"}]
     [:&.distance-dropdown {:width    "95px"
                            :position "absolute"
                            :right    "1px"
                            :top      "19px"}
      [:.chosen-container-single {:border "none !important"}
       [:.chosen-single {:height           "28px"
                         :background-color "#f5f5f5"}]]]]
    [:.location {:height "30px"
                 :border "1px solid #cccccc"}]
    [:.range-dropdown
     (rc-dropdown "16px" nil "italic")
     (dropdown-container-results "16px")]
    [:.standard-dropdown
     (rc-dropdown "16px")
     (dropdown-container-results "16px")]
    [:.secondary-dropdown
     (rc-dropdown "16px" "#d6d6d6")
     (dropdown-container-results "16px")]
    [:.tertiary-dropdown
     (rc-dropdown "14px")
     (dropdown-container-results "14px")]

    ;; Popover
    [:.rc-popover-anchor-wrapper {:z-index 11}]

    (small-screen [:.no-padding-mobile {:padding "0!important"}])]

   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
   ;_+
   ;_+  Min Max Slider
   ;_+
   ;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

   [(s/input (s/attr= :type :range)) {:-webkit-appearance none
                                      :height             "1px"
                                      :width              "100%"
                                      :background         "transparent"}]

   [(s/input (s/attr= :type :range)) [:&:focus {:outline none}]]

   [(s/input (s/attr= :type :range) :.multirange) {:padding        0
                                                   :margin-top     "20px"
                                                   :display        'inline-block
                                                   :vertical-align 'top}]

   [(s/input (s/attr= :type :range) :.multirange -webkit-slider-thumb) {:-webkit-appearance none
                                                                        :height             "13px"
                                                                        :width              "13px"
                                                                        :background         ladders-turquoise-blue
                                                                        :border             "1px solid white"
                                                                        :border-radius      "30px"}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange.original) {:position   'absolute
                                                            :background ladders-teal}
    [:&:hover {:background ladders-teal-dark}]]
   [(s/input (s/attr= :type :range) :.multirange.original -webkit-slider-thumb) {:position "relative"
                                                                                 :z-index  2}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange.original -moz-range-thumb) {:height     "13px"
                                                                             :width      "13px"
                                                                             :z-index    1
                                                                             :background ladders-turquoise-blue
                                                                             :border     none}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange -moz-range-track) {:border-color "transparent"
                                                                    :height       "1px"
                                                                    :background   ladders-turquoise-blue}]
   [(s/input (s/attr= :type :range) :.multirange.ghost -moz-range-thumb) {:height     "13px"
                                                                          :width      "13px"
                                                                          :z-index    1
                                                                          :background ladders-turquoise-blue
                                                                          :border     none}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange.original -ms-range-thumb) {:height     "13px"
                                                                            :width      "13px"
                                                                            :z-index    1
                                                                            :background ladders-turquoise-blue
                                                                            :border     none}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange -ms-range-track) {:border-color "transparent"
                                                                   :background   ladders-turquoise-blue}]
   [(s/input (s/attr= :type :range) :.multirange.ghost -ms-range-thumb) {:height     "13px"
                                                                         :width      "13px"
                                                                         :z-index    1
                                                                         :background ladders-turquoise-blue
                                                                         :border     none}
    [:&:hover {:background ladders-teal-dark}]]

   [(s/input (s/attr= :type :range) :.multirange.ghost) {:position   "relative"
                                                         :background ladders-turquoise-blue}]])
