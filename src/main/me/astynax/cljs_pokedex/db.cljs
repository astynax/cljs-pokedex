(ns me.astynax.cljs-pokedex.db
  (:require [datascript.core :as d]))

(def ^:const schema
  {:pokemon/id {:db/unique :db.unique/identity}
   :pokemon/color {:db/index true
                   :db/type :db.type/ref}
   :pokemon/types {:db/index true
                   :db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/many}
   :pokemon/is-legendary {:db/index true}
   :pokemon/is-mythial {:db/index true}

   :color/id {:db/unique :db.unique/identity}

   :pokemon-type/id {:db/unique :db.unique/identity}})

(defn create []
  (d/create-conn schema))

(defn -to-type-entity [data]
  {:db/add -1
   :db/ident [:pokemon-type/id (:id data)]
   :pokemon-type/id (:id data)
   :pokemon-type/name (get-in data [:names 0 :name])})

(defn -to-color-entity [data]
  {:db/add -1
   :db/ident [:color/id (:id data)]
   :color/id (:id data)
   :color/name (get-in data [:names 0 :name])})

(defn -to-pokemon-entity [data]
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

(defn load [conn & {:keys [pokemon types colors]}]
  (d/transact! conn (map -to-type-entity types))
  (d/transact! conn (map -to-color-entity colors))
  (d/transact! conn (map -to-pokemon-entity pokemon)))

(defn -merge-rows [acc {:keys [eid type] :as row}]
  (update acc eid
          (fn [old] (or (and old (update old :types conj type))
                       (-> row
                           (dissoc :type)
                           (assoc :types [type]))))))

(defn list-pokemon [db & extra]
  (->>
   db
   (d/q (concat
         '[:find ?pid ?name ?color ?type
           :keys eid name color type
           :where
           [?eid :pokemon/id ?pid]
           [?eid :pokemon/name ?name]
           [?eid :pokemon/color ?cid]
           [?cid :color/name ?color]
           [?eid :pokemon/types ?tid]
           [?tid :pokemon-type/name ?type]]
         extra))
   (reduce -merge-rows {})
   vals
   (sort
    (fn [{i1 :eid n1 :name} {i2 :eid n2 :name}]
      (or (< n1 n2) (and (= n1 n2) (< i1 i2)))))))
