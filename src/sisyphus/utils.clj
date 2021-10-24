(ns sisyphus.utils
  (:require [java-time :as jtime]
  [clojure.repl :as repl]          ))

(defn get-fn-name [f]
  (let [demunged (repl/demunge (str f))
        name (apply str (take-while (complement #{\@}) demunged))]
    name))


(defn due? [task-due-at]
  (let [now (jtime/local-date-time)]
    (or (jtime/before? task-due-at now)
        (= task-due-at now))))


(defn- get-before
  "Returns the element in coll before key-set.
   Returns 0 if key not in coll."
  [key-set coll]
  (when (some key-set coll)
    (reduce (fn [prev curr]
              (if (key-set curr)
                (reduced prev)
                curr))
            coll)))

(defn- time->seconds [days hours minutes seconds]
  (+
   (* days 24 60 60)
   (* hours 60 60)
   (* minutes 60)
   seconds))

(defn- get-start-time [schedule]
  (when (some #{:starting-at} schedule)
    (let [[_ & time] (drop-while (complement #{:starting-at}) schedule)]
      (apply jtime/local-time time))))

;replace with spec
(defn verify-schedule-syntax [schedule]
  (let [valid-keys #{:days :day
                     :hours :hour
                     :minutes :minute
                     :seconds :second}]
    (-> schedule
        (#(assert (= :every (first %)) "first element must be :every"))
        (#(assert (some valid-keys
                        %) "missing duration keyword"))
        (#(assert (not (some valid-keys (remove valid-keys %))) 
                  "use of a duration key twice")))))

(defn schedule-parser
  "Parses a vector to return a period in seconds and a start time.
  example `[:every 2 :days 5: hours 3 :minutes 2 :seconds :starting-at 5 30]`.
  If the start time is not provided the task will immediately be run when `sisyphus/run` is called."
  [schedule]
  (let [days       (or (get-before #{:days :day} schedule) 0)
        hours      (or (get-before #{:hours :hour} schedule) 0)
        minutes    (or (get-before #{:minutes :minute} schedule) 0)
        seconds    (or (get-before #{:seconds :second} schedule) 0)
        start-time (get-start-time schedule)]
    [(time->seconds days, hours, minutes, seconds), start-time]))

