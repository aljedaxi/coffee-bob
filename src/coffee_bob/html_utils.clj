(ns coffee-bob.html-utils)


; todo https://developer.mozilla.org/en-US/docs/Web/HTML/Element/hgroup
(defn header [h1 & summary] (list [:header [:h1 h1] summary]))


(defn stylesheet [href] [:link {:rel "stylesheet" :href href}])


; (defn without-div [hiccup]
;   (let [[tag opts & children] (m/component hiccup)]
;     children))

(defn a [text href] [:a {:href href} text])
