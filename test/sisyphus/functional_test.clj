(ns sisyphus.functional-test
  (:require [sisyphus.core :as sisy]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn cleanup-files []
  (let [files (seq (.list (io/file ".")))
        text-files (filter #(string/includes? % ".txt") files)]
    (doseq [file text-files] 
      (io/delete-file file))))

(def t1 ["writefile2seconds"
         (fn []
           (with-open [w (clojure.java.io/writer  (str (rand-int 100) ".txt")  :append true)]
             (.write w (str "hello" "world"))))
         [:every 5 :seconds]])

(def t2 ["writefileSleep"
         (fn []
           (with-open [w (clojure.java.io/writer  (str "sleep" (rand-int 500) ".txt")  :append true)]
             (.write w (str "hello" "world")))
           (Thread/sleep 10000))
         [:every 10 :seconds]])

(def t-exception ["exception"
                  (fn [] (throw RuntimeException))
                  [:every 7 :seconds]])

(apply sisy/add-task! t1)

(apply sisy/add-task! t2)
(apply sisy/add-task! t-exception)


(sisy/run-tasks!)
(Thread/sleep 10010)
;; (Thread/sleep 60000)
(sisy/stop-tasks!)
(sisy/print-log!)
(sisy/write-log!)
(cleanup-files)