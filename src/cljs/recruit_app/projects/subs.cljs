(ns recruit-app.projects.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]
            [cljs-time.coerce :as c]
            [recruit-app.util.sort :as sort-util]
            [recruit-app.projects.db :as db]
            [cljs.spec.alpha :as s]))

(def reg-projects-subs (partial sub/reg-subs "projects"))

(reg-projects-subs [["project-title" ""] ["new-title" ""]
                    ["curr-project-id" nil] ["curr-project-title" ""]
                    ["projects-map" ""] ["candidates-map" ""]
                    ["show-create-project-input?" false]
                    ["active-candidate-id" nil] ["candidate-projects" {}]
                    ["sort-order" :desc] ["sort-col" :date_created]
                    ["page-loaded?" false] ["disabled-projects" #{}]
                    ["editing-project" nil] ["show-errors?" false]])
(sub/reg-subs
  "projects"
  "editing-project"
  [["projectId" nil] ["title" ""]])

(defn projects-for-active-candidate
  "Returns set of projects for active candidate"
  [[projects secure-id] _]
  (get projects secure-id #{}))

(defn projects-vector
  "Converts map of projects to vector"
  [projects-map _]
  (reduce #(conj %1 (second %2)) [] projects-map))

(defn- projects-sort-col
  "Returns function to derive sort-col from project"
  [sort-col]
  (condp = sort-col
    :date_created #(c/from-long (:date_created %))))

(defn- projects-comparator
  "Returns comparator based on sort-col and sort-order"
  [sort-col sort-order]
  (condp = sort-col
    :date_created (sort-util/time-comparator sort-order)))

(defn projects-data
  "Returns sorted projects vector"
  [[projects sort-col sort-order] _]
  (sort-by
    (projects-sort-col sort-col)
    (projects-comparator sort-col sort-order)
    projects))

(defn candidates-data
  "Returns map of candidates"
  [candidates-map _]
  candidates-map)

(defn curr-project
  "Retrieves current project from projects map"
  [[project-id projects] _]
  (get projects project-id {}))

(defn project-disabled?
  "Returns whether given project-id is in disabled-projects"
  [disabled-projects [_ project-id]]
  (contains? disabled-projects project-id))

(rf/reg-sub
  :projects/projects-for-active-candidate
  :<- [:projects/candidate-projects]
  :<- [:projects/active-candidate-id]
  projects-for-active-candidate)

(rf/reg-sub
  :projects/projects-vector
  :<- [:projects/projects-map]
  projects-vector)

(rf/reg-sub
  :projects/projects-data
  :<- [:projects/projects-vector]
  :<- [:projects/sort-col]
  :<- [:projects/sort-order]
  projects-data)

(rf/reg-sub
  :projects/candidates-data
  :<- [:projects/candidates-map]
  candidates-data)

(rf/reg-sub
  :projects/curr-project
  :<- [:projects/curr-project-id]
  :<- [:projects/projects-map]
  curr-project)

(rf/reg-sub
  :projects/project-disabled?
  :<- [:projects/disabled-projects]
  project-disabled?)

(rf/reg-sub
  :projects/create-project-form-valid?
  :<- [:projects/new-title]
  (partial s/valid? ::db/name))

(rf/reg-sub
  :projects/editing-form-valid?
  :<- [:projects/editing-project-title]
  (partial s/valid? ::db/name))
