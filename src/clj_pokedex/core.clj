(ns clj-pokedex.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [redirect]]
            [rum.core :as rum])
  (:gen-class))

(defonce state (atom 0))

(defroutes app
  (GET "/" []
    (rum/render-html
     [:div
      [:h1 "Counter"]
      (str @state)
      [:a {:href "/inc" :style {:border "1px solid red;"}} "+"]
      [:a {:href "/dec" :style {:border "1px solid red;"}} "-"]]))

  (GET "/inc" []
    (swap! state inc)
    (redirect "/"))

  (GET "/dec" []
    (swap! state dec)
    (redirect "/"))

  (GET "/greet" []
    (rum/render-html
     [:form {:method :POST}
      [:label "Name"
       [:input {:name "name"}]]
      [:button {:type "submit"} "Greet!"]]))

  (POST "/greet" {{n :name} :params}
    (rum/render-html
     [:h1 "Hello, " n "!"]))

  (route/not-found "<h1>Page not found :(</h1>"))

(defn -main
  [& args]
  (println "Starting on localhost:4000...")
  (-> #'app
      wrap-reload
      (run-jetty {:port 4000})))

;; REPL-related stuff

(defonce server (atom nil))

(defn- start []
  (when-not @server
    (reset! server
            (-> #'app
                wrap-keyword-params
                wrap-params
                (run-jetty {:port 4000 :join? false})))))

(defn- stop []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn- restart [] (stop) (start))
