# geohash-clj

A Simple geohash library for Clojure. This is a Clojure port of David Troy's
geohash library for Javascript.

## Usage

    (require '[geohash-clj.core :as geohash])

    (geohash/encode 38.878 -76.981)
    => "dqcm0m80u8qp"

    (geohash/decode "dqcm0jxssg7f")
    => {:lon [-76.98167894035578 -76.98167860507965], :lat [38.87865889817476 38.878659065812826]}

    (geohash/get-neighbours "dqcjqc")
    => ["dqcjqf" "dqcjqb" "dqcjr1" "dqcjq9" "dqcjqd" "dqcjr4" "dqcjr0" "dqcjq8"]

## License

Distributed under the Eclipse Public License either version 1.0.
