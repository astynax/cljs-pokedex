(ns me.astynax.cljs-pokedex.api.spec.common
  (:require [clojure.spec.alpha :as s]))

(s/def ::id number?)
(s/def ::name (s/and string?
                     #(pos? (count %))))
(s/def ::names (s/and #(= 1 (count %))
                      (s/? (s/keys :req-un [::name]))))
