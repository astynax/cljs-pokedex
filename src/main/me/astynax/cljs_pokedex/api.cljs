(ns me.astynax.cljs-pokedex.api
  (:require [shadow.resource :as rc]
            [ajax.core :refer [GET POST]]))

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
