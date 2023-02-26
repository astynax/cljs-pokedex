(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]
            [ajax.core :refer [GET POST]]))

(def url "https://pokeapi.co/api/v2/pokemon/")

(defonce cache (atom {}))

(defonce state (atom [:list url]))

(comment
  (POST "https://beta.pokeapi.co/graphql/v1beta"
      :format :json
      :params
      {:query "query samplePokeAPIquery {
  pokemon: pokemon_v2_pokemon {
    types: pokemon_v2_pokemontypes {
      type: pokemon_v2_type {
        id
        type_name: pokemon_v2_typenames(where: {language_id: {_eq: 9}}) {
          name
        }
      }
    }
    specy: pokemon_v2_pokemonspecy {
      specy_name: pokemon_v2_pokemonspeciesnames(where: {language_id: {_eq: 9}}) {
        name
      }
      color: pokemon_v2_pokemoncolor {
        id
        color_name: pokemon_v2_pokemoncolornames(where: {language_id: {_eq: 9}}) {
          name
        }
      }
      is_mythical
      is_legendary
    }
    sprites: pokemon_v2_pokemonsprites {
      sprites
    }
    id
  }
}"
       :variables nil
       :operationName "samplePokeAPIquery"
       }))

(defn fetch
  ([addr] (fetch addr ""))
  ([addr query]
   (or (get @cache addr)
       (and (GET (str addr query)
                :response-format :json
                :handler (fn [resp]
                           (swap! cache assoc addr resp))
                :keywords? true)
            {}))))

(rum/defc view < rum/reactive []
  (let [_ (rum/react cache)
        [page-type addr] (rum/react state)]
    (case page-type
      :list
      [:ul
       (for [row (take 20 (get (fetch addr) :results []))]
         [:li
          [:a {:href "#"
               :on-click (fn []
                           (reset! state [:pokemon (:url row)]))}
           (:name row)]])]

      :pokemon
      [:div
       [:a {:href "#" :on-click (fn [] (reset! state [:list url]))}
        "<< to list"]
       (let [data (fetch addr)]
         [:img.block.fixed {:src (get-in data [:sprites :front_default] "")}])]

      :else "oops!")))

(defn init []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn ^:dev/after-load after-reload []
  (init))
