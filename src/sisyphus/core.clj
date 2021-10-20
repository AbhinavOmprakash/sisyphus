(ns sisyphus.core
  (:require [sisyphus.utils :as utils]
            [java-time :as jtime]))



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

(defn- initial-setup! [tasks]
  (swap! tasks (fn [tasks]
                 (map (comp nil-due-at->due-at
                            local-time->local-date-time)
                      tasks))))


(def ^:private tasks (atom []))

(defn run-tasks!
  "This function will start the scheduler. Your tasks will be run when they are due.
  Note: if your `:starting-at` time was before the task runner is called then it will be immediately run.
  For e.g if a task has a starting-at at 10 (10 am) and you call this function at 11 am, the task will run immediately."
  []
  (initial-setup! tasks)
  (while true
    (swap! tasks (fn [tasks-value]
                   (map handle-tasks tasks-value)))
    (Thread/sleep 1000)))


(defn add-task!
  [name task schedule]
  (let [[interval start-time] (utils/schedule-parser schedule)
        new-task {:name                name
                  :task                task
                  :interval-in-seconds interval
                  :due-at              start-time}]
    ; should I throw an exception if the task already exists?
    (swap! tasks conj new-task)))


(defn remove-task!
  [name]
  (swap! tasks #(filter (fn [task]
                          (not (= name (:name task)))
                          %))))