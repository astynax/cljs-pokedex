(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]
            [ajax.core :refer [GET POST]]
            [datascript.core :as d]))

(defonce cache (atom nil))
(defonce state (atom {:loaded false}))

(defonce schema {:pokemon/id {:db/unique :db.unique/identity}
                 :pokemon/color {:db/index true
                                 :db/type :db.type/ref}
                 :pokemon/types {:db/index true
                                 :db/valueType :db.type/ref
                                 :db/cardinality :db.cardinality/many}
                 :pokemon/is-legendary {:db/index true}
                 :pokemon/is-mythial {:db/index true}

                 :color/id {:db/unique :db.unique/identity}

                 :pokemon-type/id {:db/unique :db.unique/identity}})

(defonce conn (d/create-conn schema))

(defn to-type-entity [data]
  {:db/add -1
   :db/ident [:pokemon-type/id (:id data)]
   :pokemon-type/id (:id data)
   :pokemon-type/name (get-in data [:names 0 :name])})

(defn to-color-entity [data]
  {:db/add -1
   :db/ident [:color/id (:id data)]
   :color/id (:id data)
   :color/name (get-in data [:names 0 :name])})

(defn to-pokemon-entity [data]
  {:db/add -1
   :db/ident             [:pokemon/id (:id data)]
   :pokemon/id           (:id data)
   :pokemon/name         (get-in data [:specy :names 0 :name])
   :pokemon/color        [:color/id (get-in data [:specy :color :id])]
   :pokemon/types        (map (fn [t] [:pokemon-type/id
                                      (get-in t [:type :id])])
                              (:types data))
   :pokemon/is-legendary (get-in data [:specy :is_legendary])
   :pokemon/is-mythial   (get-in data [:specy :is_mythical])})

(defn load-cached-data [{:keys [pokemon types colors]}]
  (d/transact! conn (map to-type-entity types))
  (d/transact! conn (map to-color-entity colors))
  (d/transact! conn (map to-pokemon-entity pokemon))
  (swap! state assoc :loaded true))

(add-watch cache :from-cache (fn [_ _ _ data] (load-cached-data data)))

(defn fetch-and-cache []
  (POST
      "https://beta.pokeapi.co/graphql/v1beta"
      :handler #(reset! cache (:data %))
      :format :json
      :response-format :json
      :keywords? true
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

(rum/defc view < rum/reactive []
  (let [{:keys [loaded]} (rum/react state)]
    (if (not loaded)
      [:div "Loading..."]
      [:ul
       (for [[name color eid]
             (sort
              (d/q '[:find ?name ?color ?eid
                     :where
                     [?eid :pokemon/name ?name]
                     [?eid :pokemon/color ?cid]
                     [?cid :color/name ?color]]
                   @conn))]
         [:li {:key (str eid)}
          name
          " (" color ")"
          (for [t (sort (d/q '[:find ?type
                               :in $ ?pid
                               :where
                               [?pid :pokemon/types ?tid]
                               [?tid :pokemon-type/name ?type]]
                             @conn eid))]
            [:span " [" t "]"])
          ])]
      )))

(defn ^:dev/after-load mount []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn init []
  (js/setTimeout fetch-and-cache 0)
  (mount))
