(ns coffee-bob.rdf
  (:import [crg.turtle.parser Literal])
  (:require [datascript.core :as d]
            [clojure.string :as s]
            [clojure.java.io :refer [input-stream]]
            [crg.turtle.parser :as ttl]))

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
   '[:find ?c-uri ?a ?attr
     :where
     [?e :rdf/type :skos/ConceptScheme]
     [?e :uri ?cs-uri]
     [?c-id :skos/topConceptOf ?cs-uri]
     [?c-id :uri ?c-uri]
     [?c-id ?a]
     [?c-id ?a ?attr]]
   @conn))

(defn mapen-datoms [x]
  (->> x
       (group-by first)
       (mapcat (fn [[id vals]] [id (apply hash-map (mapcat rest vals))]))
       (apply hash-map)))

(defn map-map [f o]
  (->> o (mapcat f) (apply hash-map)))

(defn map-vals [f o]
  (map-map (fn [[k v]] [k (f v)]) o))
(defn map-keys [f o]
  (map-map (fn [[k v]] [(f k) v]) o))
(defn de-namespace [o]
  (map-keys #(-> % str (s/replace #":\w+/" "") (s/replace #"^:" "") keyword) o))

(def top-features (->> top-concepts mapen-datoms (map-vals de-namespace)))
