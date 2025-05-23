(defproject coffee-bob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-ring "0.12.6"]]
  :ring {:handler coffee-bob.core/app}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [optimus "2023-02-08"]
                 [hiccup "2.0.0-RC1"]
                 [datascript "1.7.4"]
                 [org.clojars.quoll/turtle "0.2.2"]
                 [stasis "2023.11.21"]]
  :repl-options {:init-ns coffee-bob.core})
