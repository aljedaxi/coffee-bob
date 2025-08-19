(ns coffee-bob.core
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [stasis.core :as stasis]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [coffee-bob.export :refer [get-assets]]
            [coffee-bob.pages :refer [pages]]))

(def app (-> pages
             (stasis/serve-pages {:stasis/ignore-nil-pages? true})
             (optimus/wrap get-assets optimizations/none serve-live-assets)
             wrap-content-type))

(defn export []
  (stasis/empty-directory! "./out")
  (stasis/export-pages pages "./out"))
