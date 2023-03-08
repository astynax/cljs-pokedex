(ns me.astynax.cljs-pokedex.api
  (:require [ajax.core :refer [POST]]))

(defn fetch [handler]
  (POST
      "https://beta.pokeapi.co/graphql/v1beta"
      :handler #(handler (:data %))
      :format :json
      :response-format :json
      :keywords? true
      :headers
      {"X-Method-Used" "graphiql"}
      :params
      {:query "
query samplePokeAPIquery {
  pokemon: pokemon_v2_pokemon {
    types: pokemon_v2_pokemontypes {
      type: pokemon_v2_type {
        id
      }
    }
    specy: pokemon_v2_pokemonspecy {
      names: pokemon_v2_pokemonspeciesnames(where: {language_id: {_eq: 9}}) {
        name
      }
      color: pokemon_v2_pokemoncolor {
        id
      }
      is_mythical
      is_legendary
    }
    id
    sprites: pokemon_v2_pokemonsprites {
      sprites
    }
  }
  types: pokemon_v2_type {
    id
    names: pokemon_v2_typenames(where: {language_id: {_eq: 9}}) {
      name
    }
  }
  colors: pokemon_v2_pokemoncolor {
    id
    names: pokemon_v2_pokemoncolornames(where: {language_id: {_eq: 9}}) {
      name
    }
  }
}"
       :variables nil
       :operationName "samplePokeAPIquery"
       }))

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
