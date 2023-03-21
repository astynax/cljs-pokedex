(defproject clj-pokedex "0.1.0-SNAPSHOT"
  :description "A simple backend for the ClJS-Pokedex"
  :url "https://github.com/astynax/cljs-pokedex"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-devel "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [rum "0.12.10" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [compojure "1.7.0"]]
  :main ^:skip-aot clj-pokedex.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
