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
  (swap! lines-written-to-file + (count lines)))

(defn- truncate [s n]
  (subs s 0 n))

