(ns sisyphus.core-test
  (:require [clojure.test :refer :all]
            [sisyphus.core :refer :all]))

(def tasks #'sisyphus.core/tasks)


(deftest -add-tasks
  (testing "that a task is properly added"
    (let [test-fn #(println "test")]
      (is (= (add-task! "print" test-fn [:every 1 :second])
             [{:name "print" :task test-fn :interval-in-seconds 1 :due-at nil}]
             @tasks)))))
