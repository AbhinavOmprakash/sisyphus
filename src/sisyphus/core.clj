(ns sisyphus.core
  (:require [sisyphus.utils :as utils]))

(def ^:private tasks (atom {}))

(defn add-task
  [name task schedule]
  (let [[interval start-time] (utils/schedule-parser schedule)
        new-task {name {:task task
                        :interval-in-seconds interval
                        :due-at start-time}}]
    ; should I throw an exception if the task already exists?
    (swap! tasks conj new-task)))
