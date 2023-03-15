(ns me.astynax.cljs-pokedex.api.spec.pokemon
  (:require [clojure.spec.alpha :as s])
  (:import me.astynax.cljs-pokedex.api.spec.common))

(s/def ::type (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.common/id]))
(s/def ::types (s/+ (s/keys :req-un [::type])))

(s/def ::color (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.common/id]))
(s/def ::is_legendary boolean?)
(s/def ::is_mythical boolean?)

(s/def ::specy (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.common/names
                                ::color
                                ::is_legendary
                                ::is_mythical]))

(s/def ::pokemon
  (s/* (s/keys :req-un [:me.astynax.cljs-pokedex.api.spec.common/id
                        ::types
                        ::specy])))
