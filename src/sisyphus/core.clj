(ns sisyphus.core
  (:require [sisyphus.utils :as utils]
            [java-time :as jtime]))

(def ^:private tasks (atom []))


(defn- nil-due-at->due-at
  "If the task has a nil due-at then this sets it to current local-date-time."
  [task]
  (update task :due-at (fn [due-at]
                         (if (nil? due-at)
                           (jtime/local-date-time)
                           due-at))))

(defn- local-time->local-date-time
  "If a task has a due-at, its in the local-time format,
  and must be converted to local-date-time.
  The date will the set to the date the scheduler is run."
  [task]
  (update task :due-at (fn [due-at]
                         (if-not (nil? due-at)
                           (jtime/adjust (jtime/local-date-time) due-at)
                           due-at))))


(defn- update-due-at [task]
  (let [interval (:interval-in-seconds task)]
    (update task :due-at (fn [old-due-at]
                           (jtime/plus old-due-at (jtime/seconds interval))))))


(defn- handle-tasks [task]
  (if (utils/due? task)
    ;; consider using claypoole to limit the number of spawned threads?
    (do (future (task))
        (update-due-at task))
    task))


(defn run-tasks!
  []
  (swap! tasks (fn [tasks]
                 (map nil-due-at->due-at tasks)))
  (while true
    (swap! tasks (fn [tasks-value]
                   (map handle-tasks tasks-value)))
    (Thread/sleep 1000)))

(defn add-task
  [name task schedule]
  (let [[interval start-time] (utils/schedule-parser schedule)
        new-task {:name                name
                  :task                task
                  :interval-in-seconds interval
                  :due-at              start-time}]
    ; should I throw an exception if the task already exists?
    (swap! tasks conj new-task)))
