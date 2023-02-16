(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]
            [ajax.core :refer [GET]]))

(def url "https://pokeapi.co/api/v2/pokemon/")

(defonce cache (atom {}))

(defonce state (atom [:list url]))

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
           (:name row)]
          ])
       ]

      :pokemon
      [:div
       [:a {:href "#" :on-click (fn [] (reset! state [:list url]))}
        "<< to list"]
       (let [data (fetch addr)]
         [:img.block.fixed {:src (get-in data [:sprites :front_default] "")}])]

      :else "oops!")
    ))

(defn init []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn ^:dev/after-load after-reload []
  (init))
