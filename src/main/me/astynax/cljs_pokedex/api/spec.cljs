(ns me.astynax.cljs-pokedex.api.spec
  (:require [clojure.spec.alpha :as s])
  (:import me.astynax.cljs-pokedex.api.spec.common
           me.astynax.cljs-pokedex.api.spec.pokemon))

(def ^:private named-things
  (s/every (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.common/id
                            :me.astynax.cljs-pokedex.api.spec.common/names])))
(s/def ::types named-things)
(s/def ::colors named-things)

(s/def ::spec
  (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.pokemon/pokemon
                   ::types
                   ::colors]))
