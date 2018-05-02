(ns recruit-app.util.events
  (:require [re-frame.core :as rf]
            [clojure.string :as string]))

(defn reg-events
  "Registers events for given namespace with optional nesting

  First argument is a string for the namespace to register
  Last argument must be a vector of strings to register change events for

  Optional middle arguments are strings to register nested events separated by -
  (i.e. (reg-events 'recruiter' 'editing-recruiter' ['firstname' 'lastname'])
  will register 2 events with names

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
    }
  "
  [ns & args]
  (let [events (last args)
        keypath (butlast args)]
    (doseq [type events]
      (let [nested-keypath (string/join "-" keypath)]
        (rf/reg-event-db
          (keyword ns (str (when (seq nested-keypath) (str nested-keypath "-")) type "-change"))
          (fn [db [_ val]]
            (assoc-in
              db
              (vec (concat [(keyword ns)] (map keyword keypath) [(keyword type)]))
              val)))))))

(defn reg-toggle-event
  [ns type]
  (rf/reg-event-db
    (keyword ns (str "toggle-" type))
    (fn [db _]
      (->> (get-in db [(keyword ns) (keyword type)])
           not
           (assoc-in db [(keyword ns) (keyword type)])))))
