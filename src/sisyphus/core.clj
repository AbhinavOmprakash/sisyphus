(ns sisyphus.core
  (:require [sisyphus.utils :as utils]))

(def ^:private tasks (atom []))

(defn add-task
  [name task schedule]
  (let [[interval start-time] (utils/schedule-parser schedule)
        new-task              {:name                name
                               :task                task
                               :interval-in-seconds interval
                               :due-at              start-time}]
    ; should I throw an exception if the task already exists?
    (swap! tasks conj new-task)))

(defn- update-due-at [task]
  (let [interval (:interval-in-seconds task)]
    (update task :due-at (fn [old-due-at]
                           (jtime/plus old-due-at (seconds interval))))))


(defn- handle-tasks [task]
  (if (utils/due? task)
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