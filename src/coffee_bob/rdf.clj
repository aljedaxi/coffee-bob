(ns coffee-bob.rdf
  (:import [crg.turtle.parser Literal])
  (:require [datascript.core :as d]
            [clojure.java.io :refer [input-stream]]
            [crg.turtle.parser :as ttl]
            [datascript.db :as db]))

(def schema {:uri {:db/index true}
             :aka {:db/cardinality :db.cardinality/many}})
(def conn (d/create-conn schema))

(with-open [f (input-stream "resources/specs.ttl")]
  (let [parser (ttl/create-parser f)]
    (doseq [[subj pred o] (ttl/get-triples parser)]
      (let [obj (if (instance? Literal o) (:lex o) o)
            ; https://www.learndatalogtoday.org/chapter/3
            ; https://github.com/tonsky/datascript/blob/fa222f7b1b05d4382414022ede011c88f3bad462/src/datascript/core.cljc#L283
            [found] (d/datoms @conn :avet :uri subj)
            [id] (if found found
                 (->> [[:db/add -1 :uri subj]]
                      (d/transact! conn)
                      :tx-data
                      first))]
        (d/transact! conn [[:db/add id pred obj]])))))

(def top-concepts
  (d/q
   '[:find ?c-uri
     :where
     [?e :rdf/type :skos/ConceptScheme]
     [?e :uri ?cs-uri]
     [?c-id :skos/topConceptOf ?cs-uri]
     [?c-id :uri ?c-uri]]
   @conn))
