(ns sisyphus.functional-test
  (:require [sisyphus.core :as sisy]))


(def t1 ["writefile2seconds"
         (fn []
           (with-open [w (clojure.java.io/writer  (str (rand-int 100) ".txt")  :append true)]
             (.write w (str "hello" "world"))))
         [:every 2 :seconds]])

(def t2 ["writefileSleep"
         (fn []
           (with-open [w (clojure.java.io/writer  (str "sleep" (rand-int 500) ".txt")  :append true)]
             (.write w (str "hello" "world")))
           (Thread/sleep 10000))
         [:every 2 :seconds]])

(sisy/add-task! t1)

(sisy/add-task! t2)

(sisy/run-tasks!)