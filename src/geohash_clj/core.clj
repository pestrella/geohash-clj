;; Ported from David Troy's geohash library for Javascript
;; Authors: Paolo Estrella
(ns geohash-clj.core
  (:require [clojure.string :refer [lower-case]]))

(def bits [16, 8, 4, 2, 1])
(def base32 "0123456789bcdefghjkmnpqrstuvwxyz")

(def neighbours (let [neighbours {:right {:even "bc01fg45238967deuvhjyznpkmstqrwx"}
                                  :left {:even "238967debc01fg45kmstqrwxuvhjyznp"}
                                  :top {:even "p0r21436x8zb9dcf5h7kjnmqesgutwvy"}
                                  :bottom {:even "14365h7k9dcfesgujnmqp0r2twvyx8zb"}}]
                  (-> (assoc-in neighbours [:bottom :odd] (-> neighbours :left :even))
                      (assoc-in [:top :odd] (-> neighbours :right :even))
                      (assoc-in [:left :odd] (-> neighbours :bottom :even))
                      (assoc-in [:right :odd] (-> neighbours :top :even)))))

(def borders (let [borders {:right {:even "bcfguvyz"}
                            :left {:even "0145hjnp"}
                            :top {:even "prxz"}
                            :bottom {:even "028b"}}]
               (-> (assoc-in borders [:bottom :odd] (-> borders :left :even))
                   (assoc-in [:top :odd] (-> borders :right :even))
                   (assoc-in [:left :odd] (-> borders :bottom :even))
                   (assoc-in [:right :odd] (-> borders :top :even)))))

(defn- refine-interval [interval c bit-mask]
  (assoc interval
    (if (= 0 (bit-and c bit-mask)) 1 0)
    (double (/ (+ (interval 0) (interval 1)) 2))))

(defn- refine-intervals [geo c even?]
  (loop [bits bits
         even? even?
         geo geo]
    (if (seq bits)
      (let [interval (refine-interval (if even? (:lon geo) (:lat geo)) c (first bits))]
        (recur (rest bits)
               (not even?)
               (assoc geo (if even? :lon :lat) interval)))
      [geo even?])))

(defn decode [geohash]
  (let [geo {:lat [-90 90] :lon [-180 180]}]
    (loop [geohash geohash
           even? true
           geo geo]
      (if (seq geohash)
        (let [c (.indexOf base32 (str (first geohash)))
              [geo even?] (refine-intervals geo c even?)]
          (recur (rest geohash) even? geo))
        geo))))

(defn- locate [[lo hi] coord c bit]
  (let [mid (/ (+ lo hi) 2)]
    (if (> coord mid)
      [[mid hi] (bit-or c (bits bit))]
      [[lo mid] c])))

(defn encode [latitude longitude]
  (let [precision 12]
    (loop [geohash ""
           geo {:lat [-90 90] :lon [-180 180]}
           bit 0
           c 0
           even? true]
      (if (< (count geohash) precision)
        (let [loc (if even?
                    (locate (:lon geo) longitude c bit)
                    (locate (:lat geo) latitude c bit))
              geo (assoc geo (if even? :lon :lat) (loc 0))
              c (loc 1)]
          (recur (if (< bit 4) geohash (str geohash (nth base32 c)))
                 geo
                 (if (< bit 4) (inc bit) 0)
                 (if (< bit 4) c 0)
                 (not even?)))
        geohash))))

(defn- calc-adjacent [geohash direction]
  (let [type (if (even? (count geohash)) :even :odd)
        last-chr (str (nth geohash (- (count geohash) 1)))
        base (apply str (take (- (count geohash) 1) geohash))]
    (if (not= -1 (.indexOf (-> borders direction type) last-chr))
      (str (loop [geohash base]
             (let [type (if (even? (count geohash)) :even :odd)
                   last-chr (str (nth geohash (- (count geohash) 1)))
                   base (apply str (take (- (count geohash) 1) geohash))]
               (if (not= -1 (.indexOf (-> borders direction type) last-chr))
                 (recur base)
                 (str base (nth base32 (.indexOf (-> neighbours direction type) last-chr))))))
           (nth base32 (.indexOf (-> neighbours direction type) last-chr)))
      (str base (nth base32 (.indexOf (-> neighbours direction type) last-chr))))))

(defn get-neighbours [geohash]
  (let [n (calc-adjacent geohash :top)
        s (calc-adjacent geohash :bottom)
        e (calc-adjacent geohash :right)
        w (calc-adjacent geohash :left)
        ne (calc-adjacent e :top)
        se (calc-adjacent e :bottom)
        sw (calc-adjacent w :bottom)
        nw (calc-adjacent w :top)]
    [n s e w nw ne se sw]))
