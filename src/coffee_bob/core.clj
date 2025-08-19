(ns coffee-bob.core
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [stasis.core :as stasis]
            [optimus.prime :as optimus]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [coffee-bob.export :refer [get-assets context]]
            [coffee-bob.pages :refer [pages]]))

(def app (-> pages
             (stasis/serve-pages (merge context {:stasis/ignore-nil-pages? true}))
             (optimus/wrap get-assets optimizations/none serve-live-assets)
             wrap-content-type))
