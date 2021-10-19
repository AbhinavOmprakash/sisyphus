(ns sisyphus.core
  (:require [sisyphus.utils :as utils]))


(defn- due? [task]
  true)

(def ^:private tasks (atom []))

(defn add-task
  [name task schedule]
  (let [[interval start-time] (utils/schedule-parser schedule)
        new-task {:name                name
                  :task                task
                  :interval-in-seconds interval
                  :due-at              start-time}]
    ; should I throw an exception if the task already exists?
    (swap! tasks conj new-task)))

(defn- update-due-at [task]
  task)

(defn- handle-tasks [task]
  (if (due? task)
    (do (future (task))
        (update-due-at task))
    task))


(defn run-tasks!
  []
  ()
  (while true
    (let [updated-tasks (map handle-tasks @tasks)]
      (swap! tasks (fn [_] updated-tasks)))
    (Thread/sleep 1000)))