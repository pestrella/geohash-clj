;; Authors: Paolo Estrella
(ns geohash-clj.core-test
  (:require [geohash-clj.core :refer :all]
            [clojure.test :refer :all]))

(deftest test-decode
  (is (= (decode "dqcm0jxssg7f")
         {:lon [-76.98167894035578 -76.98167860507965]
          :lat [38.87865889817476 38.878659065812826]})))

(deftest test-encode
  (is (= (encode 38.878 -76.981)
         "dqcm0m80u8qp")))

(deftest test-decode-encode
  (let [geohash "dqcm0jxssg7f"
        loc (decode geohash)
        lat (/ (apply + (:lat loc)) 2)
        lon (/ (apply + (:lon loc)) 2)]
    (is (= (encode lat lon)
           geohash))))

(deftest test-neighbours
  (is (= (neighbours "dqcjqc")
         ["dqcjqf" "dqcjqb" "dqcjr1" "dqcjq9" "dqcjqd" "dqcjr4" "dqcjr0" "dqcjq8"])))
