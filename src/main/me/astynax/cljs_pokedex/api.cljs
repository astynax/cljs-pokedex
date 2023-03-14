(ns me.astynax.cljs-pokedex.api
  (:require [shadow.resource :as rc]
            [clojure.spec.alpha :as s]
            [ajax.core :refer [GET POST]]))

(defn -keys [& ks] (s/keys :req-un (vec ks)))

(s/def ::id number?)
(s/def ::name (and string?
                   #(pos? (count %))))
(s/def ::names (and #(= 1 (count %))
                    (s/? (-keys ::name))))

(s/def ::type (-keys ::id))
(s/def ::types (s/& (-keys ::type)))
(s/def ::color (-keys ::id))
(s/def ::is_legendary boolean?)
(s/def ::is_mythical boolean?)

(s/def ::specy (-keys ::names
                      ::color
                      ::is_legendary
                      ::is_mythical))

(s/def ::pokemon
  (s/* (-keys ::id
              ::types
              ::specy)))

(def named-things (s/& (-keys ::id ::names)))
(s/def ::types named-things)
(s/def ::colors named-things)

(s/def ::spec
  (-keys ::pokemon
         ::types
         ::colors))

(def ^:private -query
  (rc/inline "main.gql"))

(defn fetch [handler]
  (POST
      "https://beta.pokeapi.co/graphql/v1beta"
      :handler #(handler (:data %))
      :format :json
      :response-format :json
      :keywords? true
      :headers {"X-Method-Used" "graphiql"}
      :params
      {:query -query
       :variables nil
       :operationName "samplePokeAPIquery"
       }))

(defn fetch-dump [handler]
  {:post [(s/valid? ::spec %)]}
  (GET "/dump.json"
      :handler #(handler (:data %))
      :response-format :json
      :keywords? true))

(def example
  {:pokemon
   (for [[i name color types leg myth]
         [[1 "Bulbazaur" 3 [1 2] false false]
          [2 "Charmander" 1 [3] false false]
          [3 "Foo" 2 [2] true false]
          [4 "Bar" 1 [1 3] false true]
          [5 "???" 1 [3] true true]]]
     {:id i
      :types (for [t types] {:type {:id t}})
      :specy
      {:names [{:name name}]
       :color {:id color}
       :is_legendary leg
       :is_mythical myth}})

   :types
   (for [[i n]
         [[1 "Grass"]
          [2 "Poison"]
          [3 "Fire"]]]
     {:id i
      :names [{:name n}]})

   :colors
   (for [[i n]
         [[1 "Red"]
          [2 "Yellow"]
          [3 "Green"]]]
     {:id i
      :names [{:name n}]})})
