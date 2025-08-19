(ns coffee-bob.export
  (:require
   [stasis.core :as stasis]
   [optimus.export]
   [optimus.assets :as assets]
   [optimus.optimizations :as optimizations]
   [coffee-bob.pages :as p]))

(def context
  (let [[_ ga version] (read-string (slurp "project.clj"))]
    {:version (format "v%s" version)}))

(defn get-assets []
  (->> (assets/load-assets "public" [#"/.*\.(avif|ico|js|gif|css|png|svg)"])
       (map #(assoc % :path (format "/public%s" (:path %))))))

(defn optimize [assets options]
  (-> assets
      (optimizations/add-cache-busted-expires-headers)
      (optimizations/add-last-modified-headers)))

(defn -main [target-dir]
  (let [assets (optimize (get-assets) {})
        pages (p/pages)]
    (stasis/empty-directory! target-dir)
    (optimus.export/save-assets assets target-dir)
    (stasis/export-pages pages target-dir context)))
