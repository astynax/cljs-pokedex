(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]))

(defonce counter (atom 0))

(rum/defc view < rum/reactive []
  [:h3 (str (rum/react counter))
   [:button {:on-click #(swap! counter inc)} "+"]
   [:button {:on-click #(swap! counter dec)} "-"]])

(defn init []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn ^:dev/after-load after-reload []
  (init))
