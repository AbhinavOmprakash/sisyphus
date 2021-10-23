(ns sisyphus.core
  (:require [sisyphus.utils :as utils]
            [java-time :as jtime]
            [sisyphus.log :as sisy-log]))


(def ^:private tasks (atom (list)))
(def ^:private keep-running
  "flag to start or stop sisyphus."
  (atom true))
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


(defn- update-due-at! [task]
  (let [interval (:interval-in-seconds task)]
    (update task :due-at (fn [old-due-at]
                           (jtime/plus old-due-at (jtime/seconds interval))))))


(defn- handle-tasks [task]
  (let [task-due (:due-at task)
        task-fn  (:task task)]
    (if (utils/due? task-due)
      ;; consider using claypoole to limit the number of spawned threads?
      (do (future (task-fn))
          (update-due-at task))
      task)))


(defn- initial-setup! [tasks]
  (swap! tasks (fn [tasks]
                 (map (comp nil-due-at->due-at
                            local-time->local-date-time)
                      tasks))))


(defn- smap
  "strict version of map."
  ([f coll]
   (smap f coll []))
  ([f [x & xs] res]
   (if (seq xs)
     (recur f xs (conj res (f x)))
     (conj res (f x)))))


(def ^:private tasks (atom []))


(defn run-tasks!
  "This function will start the scheduler. Your tasks will be run when they are due.
  Note: if your `:starting-at` time was before the task runner is called then it will be immediately run.
  For e.g if a task has a starting-at at 10 (10 am) and you call this function at 11 am, the task will run immediately."
  []
  (initial-setup! tasks)
  (future (while @keep-running
            (swap! tasks (fn [tasks-value]
                           (smap handle-tasks tasks-value)))
            (Thread/sleep 1000)))

(defn stop-tasks! []
  (swap! keep-running (fn [_] false)))

(defn add-task!
  "Add a task to the list of tasks.
  `name` is a string with the name of the task, this can be used to delete the task, if needed.
  `task` is a 0-arity function that will be called for its side-effects, its return value will be discarded.
  `schedule` is a vector that is used to describe the interval at which the task is run and optionally,
  a time to start the task.
  
  Usage:
  ```clojure
  (add-task! \"my-task\" 
              #(println \"task is running\")  
              [:every 1 :day 5 :hours :3 minutes 1 :second 
              :starting-at 10 30]) 
  ```

  You don't need to specify :days or :hours, even something like this is valid.
  `[:every 1 :second] ; :starting-at is optional and can be left out.` 
  The singular and plural of the time period is valid. like `1 :day` and `5 :days`."
  ([[name task schedule]]
   (add-task! name task schedule))
  ([name task schedule]
   (let [[interval start-time] (utils/schedule-parser schedule)
         new-task              {:name                name
                                :task                task
                                :interval-in-seconds interval
                                :due-at              start-time}]
     ; should I throw an exception if the task already exists?
     (swap! tasks conj new-task))))


(defn remove-task!
  "Removes a task from the task list given the name of the task."
  [name]
  (swap! tasks #(filter (fn [task]
                          (if (= name (:name task))
                            (do (println "successfully deleted task" name)
                                false)
                            true))
                        %)))