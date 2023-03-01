(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]
            [ajax.core :refer [GET POST]]
            [datascript.core :as d]))

(defonce cache (atom nil))
(defonce state (atom {:loaded false}))

(def schema {:pokemon/id {:db/index true}
             :pokemon/color-id {:db/index true}
             :pokemon/is-legendary {:db/index true}
             :pokemon/is-mythial {:db/index true}})

(defonce conn (d/create-conn schema))

(defn to-entity [data]
  {:db/add -1
   :pokemon/id           (:id data)
   :pokemon/name         (get-in data [:specy :specy_name 0 :name])
   :pokemon/color-id     (get-in data [:specy :color :id])
   :pokemon/color        (get-in data [:specy :color :color_name 0 :name])
   :pokemon/is-legendary (get-in data [:specy :is_legendary])
   :pokemon/is-mythial   (get-in data [:specy :is_mythical])})

(defn load-cached-data [data]
  (d/transact! conn
               (->> data
                    :pokemon
                    (mapv to-entity)))
  (swap! state assoc :loaded true))

(add-watch cache :from-cache (fn [_ _ _ data] (load-cached-data data)))

(defn fetch-and-cache []
  (POST
      "https://beta.pokeapi.co/graphql/v1beta"
      :handler (fn [resp] (reset! cache (:data resp)))
      :format :json
      :response-format :json
      :keywords? true
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

(rum/defc view < rum/reactive []
  (let [{:keys [loaded]} (rum/react state)]
    (if (not loaded)
      [:div "Loading..."]
      [:ul
       (for [[name color]
             (sort
              (d/q '[:find ?name ?color
                     :where
                     [?eid :pokemon/name ?name]
                     [?eid :pokemon/color ?color]
                     ]
                   @conn))]
         [:li {:key name}
          name " (" color ")"])]
      )))

(defn ^:dev/after-load mount []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn init []
  (js/setTimeout fetch-and-cache 0)
  (mount))
