(ns me.astynax.cljs-pokedex.app
  (:require [rum.core :as rum]
            [ajax.core :refer [GET POST]]
            [datascript.core :as d]
            [me.astynax.cljs-pokedex.db :as db]
            [me.astynax.cljs-pokedex.api :as api]))

(defonce cache (atom nil))
(defonce state (atom {:loaded false}))
(defonce conn (d/create-conn db/schema))

(defn load-cached-data [{:keys [pokemon types colors]}]
  (db/load
   conn
   :types types
   :colors colors
   :pokemon pokemon)
  (swap! state assoc :loaded true))

(add-watch cache :load-cached-data (fn [_ _ _ data] (load-cached-data data)))

(defn make-filters [{:keys [color type]}]
  (filter
   identity
   [(when color
      [(list '= '?color color)])
    (when type
      [(list '= '?type type)])]))

(rum/defc button [text func & args]
  [:button.inline.block.round.accent
   {:on-click (fn [] (apply swap! state func args))}
   text])

(rum/defc tag [state key value]
  (if (get state key)
    [:span.inline.fixed.block.round value]
    (button value assoc key value)))

(rum/defc view < rum/reactive []
  (let [{:keys [loaded] :as s} (rum/react state)
        filters (make-filters s)]
    (if (not loaded)
      [:div "Loading..."]
      [:table
       [:thead
        [:tr
         [:td [:strong "Name"]]
         [:td [:strong "Color"]
          (when (:color s)
            (button "✕" dissoc :color))]
         [:td [:strong "Type"]
          (when (:type s)
            (button "✕" dissoc :type))]]]
       [:tbody
        (for [{:keys [eid name color types]}
              (apply db/list-pokemon @conn filters)]
          [:tr {:key (str eid)}
           [:td {:title (str "Id: " eid)} name]
           [:td (tag s :color color)]
           [:td (for [t (sort types)]
                  (rum/with-key
                    (tag s :type t)
                    (str eid "-" t)))]
           ])]]
      )))

(defn ^:dev/after-load mount []
  (rum/mount (view) (js/document.querySelector "#root")))

(defn init []
  ;; (api/fetch #(reset! cache %))
  (api/fetch-dump #(reset! cache %))
  ;; (reset! cache api/example)
  (mount))
