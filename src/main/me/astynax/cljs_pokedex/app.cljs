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
   :pokemon/is-mythical  (get-in data [:specy :is_mythical])})

(defn load-cached-data [{:keys [pokemon types colors]}]
  (d/transact! conn (map to-type-entity types))
  (d/transact! conn (map to-color-entity colors))
  (d/transact! conn (map to-pokemon-entity pokemon))
  (swap! state assoc :loaded true))

(add-watch cache :from-cache (fn [_ _ _ data] (load-cached-data data)))

(def fake-data
  {:pokemon
   (mapv (fn [[i name color types leg myth]]
           {:id i
            :types (mapv (fn [t] {:type {:id t}})
                         types)
            :specy
            {:names [{:name name}]
             :color {:id color}
             :is_legendary leg
             :is_mythical myth}})
         [[1 "Bulbazaur" 3 [1 2] false false]
          [2 "Charmander" 1 [3] false false]
          [3 "Foo" 2 [2] true false]
          [4 "Bar" 1 [1 3] false true]
          [5 "???" 1 [3] true true]
          ])

   :types
   (mapv (fn [[i n]] {:id i :names [{:name n}]})
         [[1 "Grass"]
          [2 "Poison"]
          [3 "Fire"]])

   :colors
   (mapv (fn [[i n]] {:id i :names [{:name n}]})
         [[1 "Red"]
          [2 "Yellow"]
          [3 "Green"]])})

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

(defn list-pokemon [& extra]
  (sort
   (fn [i1 i2] (< (:name i1) (:name i2)))
   (vals
    (reduce
     (fn [acc [eid name color typ]]
       (update acc eid (fn [old] (or (and old (update old :types conj typ))
                                    {:eid eid
                                     :name name
                                     :color color
                                     :types [typ]}))))
     {}
     (d/q (concat
           '[:find ?eid ?name ?color ?type
             :where
             [?eid :pokemon/id ?pid]
             [?eid :pokemon/name ?name]
             [?eid :pokemon/color ?cid]
             [?cid :color/name ?color]
             [?eid :pokemon/types ?tid]
             [?tid :pokemon-type/name ?type]]
           extra)
          @conn)))))

(rum/defc view < rum/reactive []
  (let [{:keys [loaded extra]} (rum/react state)]
    (if (not loaded)
      [:div "Loading..."]
      [:div
       (when extra
         [:button {:on-click (fn [] (swap! state dissoc :extra))}
          "Reset filters"])
       [:ul
        (for [{:keys [eid name color types]} (list-pokemon extra)]
          [:li {:key (str eid)}
           name
           [:button {:on-click
                     (fn [] (swap! state assoc
                                  :extra
                                  [(list '= '?color color)]))}
            color]
           (for [t (sort types)]
             [:button {:on-click
                       (fn [] (swap! state assoc
                                    :extra
                                    [(list '= '?type t)]))}
              t])
           ])]]
      )))

(defn ^:dev/after-load mount []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn init []
  ;; (js/setTimeout fetch-and-cache 0)
  (reset! cache fake-data)
  (mount))
