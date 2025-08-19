(ns coffee-bob.pages
  (:require
   [clojure.pprint :as p]
   [clojure.data.json :as json]
   [markdown-to-hiccup.core :as m]
   [hiccup2.core :refer [html]]
   [coffee-bob.cafes :refer [layout cafes bobbery class-link cafe]]
   [coffee-bob.util :refer [depn]]
   [coffee-bob.taxa :refer [taxonomy]]
   [coffee-bob.html-utils :as h]))

(depn cafe-id -> first (get :id))
(depn cafe-url ->> (format "/coffee-house/%s/"))
(depn typeof some-> (nth 1) (get :typeof))
(depn property some-> (nth 1) (get :property))

(defn html5 [input] (-> input html str))

(defn skos [property & children]
  [:p {:property (format "skos:%s" property)} children])

(defn nar [props & children]
  (let [f (fn [%]
            [:p "children: "
             [:a {:rel "skos:narrower" :href (bobbery (name %))}
              %]])]
    (cond
      (keyword? children) (f children)
      (sequential? children) (map f children)
      :else nil)))

(defn cafe-list-item [[props & rest]]
  (let [{:keys [id name summary color]} props
        ratings (filter #(and (= (first %) :section)
                              (= (typeof %) "Rating")) rest)
        get-rating-val (fn [children]
                         (some->> children
                                  (filter #(and (vector? %) (= (property %) "ratingValue")))
                                  first
                                  last
                                  first))
        rating-lis (map
                    (fn [[_ {:keys [id resource]} [_tagname _heading [_tag & rest]]]]
                      (let [rating-val (get-rating-val rest)]
                        [:li {:property "reviewRating" :typeof "Rating"}
                         [:a {:property "reviewAspect"
                              :href (format "/taxonomy%s" resource)} id]
                         [:span {:property "ratingValue"} rating-val]]))
                    ratings)]
    [:details {:typeof "CriticReview"}
     [:summary
      [:h2 {:style (format "display: inline; color: %s" color)
            :property "itemReviewed" :typeof "CafeOrCoffeeShop"}
       [:a {:style "color: inherit" :href (cafe-url id)} name]]
      [:p {:property "abstract"} summary]]
     [:ul.ratings rating-lis]]))

(defn pages []
  (let [cafe-map-list (mapcat
                       #(vector (-> % cafe-id cafe-url)
                                (->> % (apply cafe) html5))
                       cafes)
        cafe-map (apply hash-map cafe-map-list)
        head-group [:hgroup
                    [:h1 "the calgary " [:a {:href (class-link "Coffee")} "coffee bob"]]
                    [:p "a celebration of any aspect of anywhere that serves coffee"]]
        speculationRules {:prefetch [{:where {:href_matches "/*"}}]
                          :eagerness "moderate"}
        headstuff (list
                   (h/stylesheet "/public/index.css")
                   [:script {:type "speculationrules"}
                    (json/write-str speculationRules)]
                   [:script {:type "module" :async true :src "/public/spider.js"}])]
    (merge
     {"/" (fn [{:keys [version]}]
            (html5
             (layout
              {:headstuff headstuff
               :version version}
              [:main head-group [:spider-graph {:aspects "rdfa"}]
               [:nav [:ul (map cafe-list-item cafes)]]])))
      "/about/" (fn [{:keys [version]}]
                  (html5
                   (layout
                    {:version version}
                    [:main
                     (m/file->hiccup "./resources/static/about.md")])))
      "/about-me/" (html5
                    (layout
                     {}
                     [:main
                      (m/file->hiccup "./resources/static/about-me.md")]))
      "/taxonomy/" (html5 taxonomy)}
     cafe-map)))
