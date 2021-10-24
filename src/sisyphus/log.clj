(ns sisyphus.log
  (:require [java-time :as jtime]))

(def ^:private log (atom (list)))

(def ^:private lines-written-to-file (atom 0))

(defn- get-lines-for-file []
  (let [log-lines (count @log)
        no-of-new-lines (- log-lines @lines-written-to-file)
        new-lines (take no-of-new-lines @log)]
    new-lines))

(defn- update-lines-written-to-file! [lines]
  (swap! lines-written-to-file + (count lines))
  lines)

(defn- truncate [s n]
  (subs s 0 n))

(defmulti prettify-log
  "local-date-time becomes str. 
  Stack trace is printed in case of failure, 
  sorted by time at which the task was run."
  identity)


(defmethod prettify-log :file
  ([_]
   (->> (get-lines-for-file)
        update-lines-written-to-file!
        (sort (fn [a b]
                (jtime/before? (:start-time a) (:start-time b))))
        (map (fn [x]
               (-> x
                   (update :start-time #(str "#inst " "\"" % "\"")) ; #inst "2021-10-23T16:51:04.134884800"
                   (update :end-time #(str "#inst " "\"" % "\""))
                   (update :outcome #(if (= % true) "SUCCESS" "!ERROR!")))))
        (map (fn [x]
               [(:outcome x) (:name x) (:start-time x) (:end-time x) (:result x) "\n"]))
        (map #(interpose " " %))
        (map (partial apply str)))))

(defmethod prettify-log :console
  ([_]
   (->> @log
        (sort (fn [a b]
                (jtime/before? (:start-time a) (:start-time b))))
        (map (fn [x]
               (-> x
                   (update :start-time #(truncate (str %) 19)) ; 2021-10-23T16:51:04.134884800 -> 2021-10-23T16:51:04
                   (update :end-time #(truncate (str %) 19))
                   (update :outcome #(if (= % true) "SUCCESS" "!ERROR!")))))
        (map (fn [x]
               [(:outcome x) (:name x) (:start-time x) (:end-time x) (:result x) "\n"]))
        (map #(interpose " " %))
        (map (partial apply str)))))

(defn update-log! [start end name outcome result]
  (swap! log #(cons {:outcome outcome :name name
                     :start-time start :end-time end
                     :result result}
                   %)))

