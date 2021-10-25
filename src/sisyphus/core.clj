(ns sisyphus.core
  (:require [sisyphus.utils :as utils]
            [java-time :as jtime]
            [sisyphus.log :as sisy-log]))


(def ^:private tasks (atom (list)))

(def ^:private keep-running
  "flag to start or stop sisyphus."
  (atom false))

(def ^:private log-file-path "sisyphus-log.edn")

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

(defn- start-task! [task]
  (let [task-name (:name task)
        task-fn (:task task)
        start-time   (jtime/local-date-time)
        task-result  (try (task-fn) (catch Exception e e))
        task-outcome (not (instance? Exception task-result)) ; exception -> fail 
        end-time     (jtime/local-date-time)]
    (sisy-log/update-log! start-time end-time task-name task-outcome task-result)))

(defn- handle-tasks [task]
  (let [task-due (:due-at task)]
    (if (utils/due? task-due)
      (do (future (start-task! task))
          (update-due-at! task))
      task)))


(defn- initial-setup! [tasks]
  (swap! keep-running (fn [_] true))
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


(defn write-log! []
  (spit log-file-path
        (apply str (sisy-log/prettify-log :file))
        :append true))

(defn print-log! []
  (println (sisy-log/prettify-log :console)))

(defn running?
  "Will return true if sisyphus is running, false if not."
  [] @keep-running)

(defonce ^:private console-message 
  (apply str (interpose "\n" ["Here are somethings sisyphus can do."
              "Call (stop-tasks!) to stop tasks."
              "Call (print-log!) to print logs in the repl."
              "Call (write-log!) to write logs to sisyphus-log.edn."
              "Call (add-task!) to add a task."
              "Call (remove-task!) to remove a task."
              "Call (running?) to check if sisyphus is running."])))

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
  (println "sisyphus is doing your tasks.\n")
  (println console-message))

(defn stop-tasks! []
  (swap! keep-running (fn [_] false))
  (println "sisyphus has stopped running"))

(defn add-task!
  "Add a task to the list of tasks.
  `name` is a string with the name of the task, this can be used to delete the task, if needed.
   if `name` is not specified, the fully-qualified name of the function will be used. 
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
  ([task schedule]
   (add-task! (utils/get-fn-name task) task schedule))
  ([name task schedule]
   (let [[interval start-time] (utils/schedule-parser schedule)
         new-task              {:name                name
                                :task                task
                                :interval-in-seconds interval
                                :due-at              start-time}]
     ; should I throw an exception if the task already exists?
     (swap! tasks conj new-task)
     (println "Task" name "added successfully")
     (println console-message))))


(defn remove-task!
  "Removes a task from the task list given the name of the task."
  [name]
  (swap! tasks #(filter (fn [task]
                          (if (= name (:name task))
                            (do (println "successfully deleted task" name)
                                false)
                            true))
                        %))
  (println console-message))