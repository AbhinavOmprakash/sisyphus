(ns sisyphus.utils
  (:require [java-time :as jtime]))

(defn- get-before
  "Returns the element in coll before key-set.
   Returns 0 if key not in coll."
  [key-set coll]
  (if (some key-set coll)
    (reduce (fn [prev curr]
              (if (key-set curr)
                (reduced prev)
                curr))
            coll)
    nil))

(defn- time->seconds [days hours minutes seconds]
  (+
   (* days 24 60 60)
   (* hours 60 60)
   (* minutes 60)
   seconds))

(defn- schedule-parser
  "Parses a vector to return a period in seconds and a start time.
  example `[:every 2 :days 5: hours 3 :minutes 2 :seconds :starting-at 5 :pm]`.
  If the start time is not provided the task will immediately be run when `sisyphus/run` is called."
  [schedule]
  (let [days       (or (get-before #{:days :day} schedule) 0)
        hours      (or (get-before #{:hours :hour} schedule) 0)
        minutes    (or (get-before #{:minutes :minute} schedule) 0)
        seconds    (or (get-before #{:seconds :second} schedule) 0)
        start-time (get-start-time schedule)]
    [(time->seconds days, hours, minutes, seconds), start-time]))

