(ns clj-pokedex.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  [& args]
  (println "Starting on localhost:4000...")
  (-> #'app
      wrap-reload
      (run-jetty {:port 4000})))
