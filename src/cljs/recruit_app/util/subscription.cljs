(ns recruit-app.util.subscription
  (:require [re-frame.core :as rf]
            [clojure.string :as string]))

(defn- reg-sub
  "Registers sub for given ns and type with given default"
  [ns type default]
  (rf/reg-sub
    (keyword (str ns) (str type))
    (fn [db _]
      (get-in db [(keyword ns) (keyword (str type))] default))))

(defn reg-nested-sub
  "Registers sub for value at given key within top level sub"
  [ns top-level-sub key default-value]
  (rf/reg-sub
    (keyword ns (str top-level-sub "-" key))
    :<- [(keyword ns top-level-sub)]
    (fn [recruiter _]
      (get recruiter (keyword key) default-value))))

(defn reg-nested-subs
  "Registers subs for retrieving any keys from a nested map"
  [ns top-level-sub subs]
  (doseq [[key default-value] subs]
    (reg-nested-sub ns top-level-sub key default-value)))

(defn reg-subs
  "Registers subscriptions for namespace with optional nesting

  First argument must be the string of the namespace
  Last argument is a vector of tuples where the first is a string of the type
  and the second is the default value

  Optional middle arguments are strings for nested subs
  (i.e. (reg-subs 'recruiter' 'editing-recruiter' [['firstname' ''] ['lastname' '']])
  will register 2 subs with names

  :recruiter/editing-recruiter-firstname
  :recruiter/editing-recruiter-lastname

  which will correspond to the db as:
  {
    :recruiter {
      :editing-recruiter {
        :firstname 'first'
        :lastname 'last'
      }
    }
  }"
  [ns & args]
  (let [subs (last args)
        keypath (butlast args)]
    (doseq [[sub default] subs]
      (let [nested-keypath (string/join "-" keypath)]
        (rf/reg-sub
          (keyword ns (str (when (seq nested-keypath) (str nested-keypath "-")) sub))
          (fn [db]
            (get-in
              db
              (vec (concat [(keyword ns)] (map keyword keypath) [(keyword sub)]))
              default)))))))
