(ns recruit-app.search.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [clojure.string :as str]
            [reagent.core :as r]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.button :as btn]
            [recruit-app.components.form :as form]
            [recruit-app.components.box :as b]
            [recruit-app.components.hyperlink :as link]
            [recruit-app.util.data-table :as dt]
            [recruit-app.modals.saved-search.views :as edit]
            [recruit-app.util.typeahead :refer [typeahead]]
            [recruit-app.footer.views :as footer]
            [recruit-app.util.dropdowns :as dd]
            [recruit-app.util.input-view :as iv]
            [recruit-app.util.pagination :as pag]
            [recruit-app.util.search :as sr]
            [recruit-app.util.uri :as u]
            [recruit-app.util.img :as img]
            [recruit-app.components.loading :as lo]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.components.alert :as alert]))

(defn exp-label [exp]
  (case exp
    0 "< 5"
    1 "5"
    2 "8"
    3 "11"
    4 "15+"
    "< 5"))

(defn exp-label-2 [exp]
  (case exp
    0 "<5"
    1 "7"
    2 "10"
    3 "15"
    4 "15+"
    "< 5"))

(defn skills
  []
  [iv/actionable-on-enter-key
   [form/input-text
    :ns "search"
    :type "criteria-search-criteria-keyword"
    :label "Skills / Keywords"
    :placeholder "e.g. Salesforce and “Sales Operations Manager”"]
   #(rf/dispatch [:search/search])])

(defn distance
  []
  [form/single-dropdown
   :ns "search"
   :type "criteria-search-criteria-radius"
   :class "distance-dropdown"
   :choices (dd/search-distance)])

(defn location
  []
  [form/input-with-dropdown
   :input [form/ra-typeahead
           :ns "search"
           :type "criteria-search-criteria-location"
           :label "Location"
           :class "location"
           :placeholder "e.g. New York, NY"
           :data-source #(rf/dispatch [:search/loc-auto %1 %2])
           :on-change #(rf/dispatch [:search/add-loc %])
           :suggestion-to-string #(:name %)
           :render-suggestion #(:name %)]
   :dropdown [distance]])

(defn location-form
  []
  [layout/column
   :padding 0
   :width "100%"
   :children [[layout/row
               :padding 0
               :children [[location]]]
              [layout/row-bottom
               :padding 5
               :children [[form/checkbox
                           :label "Desired (include people willing to relocate)"
                           :ns "search"
                           :type "criteria-search-parameters-include-desired-location?"]]]]])

(defn skills-location
  []
  [b/box-2
   :class "skills-location"
   :body [layout/row
          :children [[layout/col-left
                      :class "col-xs-7"
                      :children [[skills]]]
                     [layout/col-right
                      :class "col-xs-5"
                      :children [[location-form]]]]]])

(defn title
  []
  [layout/column
   :padding 0
   :gap "5px"
   :children [[iv/actionable-on-enter-key
               [form/input-text
                :ns "search"
                :type "criteria-search-criteria-title"
                :label "Title"
                :placeholder "e.g. Sales Manager"]
               #(rf/dispatch [:search/search])]
              [form/checkbox
               :label "Only current title"
               :ns "search"
               :type "criteria-search-parameters-only-last-title?"]]])

(defn role
  []
  [form/single-dropdown
   :ns "search"
   :type "criteria-search-criteria-discipline-ids"
   :label "Role"
   :class "role-dropdown"
   :choices (dd/search-role)])

(defn name-field
  []
  [iv/actionable-on-enter-key
   [form/input-text
    :ns "search"
    :type "criteria-search-criteria-candidate-name"
    :label "Name"
    :placeholder "e.g. John Smith"]
   #(rf/dispatch [:search/search])])

(defn company
  []
  [iv/actionable-on-enter-key
   [layout/column
    :padding 0
    :gap "5px"
    :children [[form/input-text
                :ns "search"
                :type "criteria-search-criteria-company"
                :label "company"
                :placeholder "e.g. Microsoft"]
               [form/checkbox
                :label "Only current company"
                :ns "search"
                :type "criteria-search-parameters-only-last-company?"]]]
   #(rf/dispatch [:search/search])])

(defn education
  []
  [form/single-dropdown
   :ns "search"
   :type "criteria-search-criteria-min-degree-category-id"
   :label "Degree"
   :class "education-dropdown"
   :choices (dd/search-education)])

(defn school
  []
  [iv/actionable-on-enter-key
   [form/input-text
    :ns "search"
    :type "criteria-search-criteria-school"
    :label "School"
    :placeholder "e.g. Ohio State"]
   #(rf/dispatch [:search/search])])

(defn profile-form
  []
  [layout/column
   :gap "20px"
   :padding 0
   :width "100%"
   :children [[title]
              [role]
              [name-field]]])

(defn candidate-profile
  []
  [b/box-2
   :label "Candidate Profile"
   :body [profile-form]])

(defn education-form
  []
  [layout/column
   :gap "20px"
   :padding 0
   :width "100%"
   :children [[education]
              [school]]])

(defn education-inputs
  []
  [b/box-2
   :label "Education"
   :body [education-form]])

(defn salary-slider
  []
  [form/multirange-slider
   :label "Desired Salary"
   :ns "search"
   :min-type "criteria-search-criteria-salary-min"
   :max-type "criteria-search-criteria-salary-max"
   :class "salary-slider"
   :width "100%"
   :min 40000
   :max 500000
   :step 10000
   :min-format #(str "$" (/ % 1000) "k")
   :max-format #(str "$" (/ % 1000) "k")])

(defn experience-slider
  []
  [form/multirange-slider
   :label "Years of Experience"
   :ns "search"
   :min-type "min-experience"
   :max-type "max-experience"
   :class "experience-slider"
   :width "100%"
   :min 0
   :max 4
   :step 1
   :min-format #(exp-label %)
   :max-format #(exp-label-2 %)])

(defn experience-inputs
  []
  [layout/column
   :gap "25px"
   :padding 0
   :width "100%"
   :children [[experience-slider]
              [company]]])

(defn reset-or-search
  []
  (let [minimum-required-inputs? (rf/subscribe [:search/minimum-required-inputs?])]
    (fn []
      [layout/row
       :gap "20px"
       :padding 0
       :align :center
       :children [[link/hyperlink
                   :label "Reset All"
                   :on-click #(rf/dispatch [:search/reset-all])]
                  [btn/primary-button
                   :label "Search"
                   :disabled? (not @minimum-required-inputs?)
                   :on-click #(rf/dispatch [:search/search])]]])))

(defn no-results
  []
  [layout/row
   :padding-top 18
   :padding-bottom 0
   :children [[alert/error "Sorry, your search below did not return any results. We recommend you revise the search parameters and perform a new search"]]])

(defn skills-location-input-section
  []
  [layout/row
   :padding-top 20
   :padding-bottom 0
   :children [[skills-location]]])

(defn candidate-profile-input-section
  []
  [layout/col-left
   :class "col-xs-4"
   :children [[candidate-profile]]])

(defn salary-experience-input-section
  []
  [layout/column
   :class "col-xs-4"
   :children [[layout/column
               :padding 0
               :gap "20px"
               :children [[b/box-2
                           :label "Salary"
                           :body [salary-slider]]
                          [b/box-2
                           :label "Experience"
                           :body [experience-inputs]]]]]])

(defn education-input-section
  []
  [layout/col-right
   :class "col-xs-4"
   :children [[education-inputs]]])

(defn search-inputs
  []
  (let [show-no-result-error? (rf/subscribe [:search/show-no-result-error?])]
    [layout/column
     :padding 0
     :children [(when @show-no-result-error?
                  [no-results])
                [skills-location-input-section]
                [layout/row
                 :padding-top 20
                 :padding-bottom 0
                 :children [[candidate-profile-input-section]
                            [salary-experience-input-section]
                            [education-input-section]]]]]))

(defn index
  []
  (rf/dispatch [:search/load-view])
  (fn []
    [layout/column
     :padding 0
     :class "search main"
     :children [[header/page-header
                 :header-text "Search for Candidates"
                 :right-element [reset-or-search]]
                [layout/page-content [search-inputs]]
                [header/page-header
                 :right-element [reset-or-search]]]]))
