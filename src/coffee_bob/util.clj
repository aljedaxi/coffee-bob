(ns coffee-bob.util
  (:require [clojure.string :as s]))


(defmacro depn [func-name threading-macro & args]
  `(defn ~func-name [arg#] (~threading-macro arg# ~@args)))


(depn feature-url ->> (format "criterion/%s/"))
(depn cafe-url ->> (format "coffeehouse/%s/"))
(depn first-val some-> first (get :content) first s/trim)


(defn parse-cafe [{{:keys [id]} :attrs content :content}]
  (defn fuck [{:keys [tag content attrs]}]
    (let [{sub-tags false maybe-summary true} (group-by string? content)
          {:keys [summary write-up]
           real-tags nil} (group-by #(-> % :tag #{:summary :write-up}) sub-tags)
          value (or (get attrs :summary) (some-> maybe-summary first s/trim) "0")
          sub-features (map fuck real-tags)]
      {:sub-features sub-features
       :value value
       :summary (first-val summary)
       :write-up (first-val write-up)
       :tag tag}))
  (let [mapped-tags (group-by :tag content)
        {:keys [name coords summary impression write-up color]} mapped-tags
        latest-impression (first impression)
        {:keys [content attrs]} latest-impression
        {:keys [timestamp]} attrs
        features (map fuck content)]
    {:name (first-val name)
     :id id
     :write-up (first-val write-up)
     :url (cafe-url id)
     :coords (if coords (s/split (first-val coords) #", ") coords)
     :summary (first-val summary)
     :color (first-val color)
     :features features}))


(defn parse-cafes [{:keys [content] :as root}]
  (let [{:keys [cafe]} (group-by :tag content)]
    (map parse-cafe cafe)))
