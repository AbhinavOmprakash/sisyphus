(ns sisyphus.utils-test
  (:require [clojure.test :refer :all]
            [sisyphus.utils :refer :all]))


(deftest -schedule-parser
  (testing "all time periods get converted to seconds"
    (is (= (schedule-parser [:every 6 :days]) 
           [(* 6 24 60 60) nil]))
    (is (= (schedule-parser [:every 6 :hours]) 
           [(* 6 60 60) nil]))
    (is (= (schedule-parser [:every 6 :minutes]) 
           [(* 6 60) nil]))
    (is (= (schedule-parser [:every 6 :seconds]) 
           [(* 6) nil])))
  
  (testing "schedule can handle mixed time periods"
    (is (= (schedule-parser [:every 6 :days 6 :hours]) 
           [(+ (* 6 24 60 60) (* 6 60 60)) nil]))
    (is (= (schedule-parser [:every 6 :days 6 :hours 6 :minutes]) 
           [(+
             (* 6 24 60 60)
             (* 6 60 60)
             (* 6 60))
            nil]))))



  


