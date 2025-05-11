(ns coffee-bob.core
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [clojure.pprint :as p]
            [stasis.core :as stasis]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.export]
            [clojure.string :as s]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [hiccup.page :refer [html5]]
            [coffee-bob.cafes :refer [layout cafes bobbery cafe silly-details]]
            [coffee-bob.util :refer [depn]]
            [coffee-bob.rdf :refer [top-features]]
            [coffee-bob.html-utils :as h]))

(defn get-assets []
  (->> (assets/load-assets "public" [#"/.*\.(avif|ico|js|gif|css)"])
       (map #(assoc % :path (format "/public%s" (:path %))))))

(def meta first)
(depn cafe-id -> meta (get :id))
(depn cafe-url ->> (format "/coffee-house/%s/"))
(depn typeof some-> (nth 1) (get :typeof))
(depn property some-> (nth 1) (get :property))

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


(defn top-feature
  [{:keys [prefLabel definition editorialNote scopeNote example id topConceptOf narrower]}
   & children]
  [:section {:id id :about (format "[bob%s]" id) :typeof "[bob:Feature]"}
   [:div {:style "display: grid; grid-template-columns: 1fr 0.5fr; align-items: baseline"}
    [:h2 {:property "skos:prefLabel" :style "display: inline"} (or prefLabel id)]
    (if topConceptOf [:span {:property "skos:topConceptOf"} topConceptOf] nil)]
   (skos "definition" definition)
   (if editorialNote (skos "editorialNote" editorialNote) nil)
   (if example [:p "eg. " [:span {:property "skos:example"} example]] nil)
   (if scopeNote (skos "scopeNote" scopeNote) nil)
   (nar {} narrower)
   children])

(def taxonomy
  (layout
   {:prefix "bob: http://localhost:3000/taxonomy/#"} ;TODO
   [:main
    [:hgroup [:h1 "the calgary coffee vocabulary"]
     [:p "ways of talking about coffee"]]
    (map
     (fn [[k {:as all}]]
       (top-feature (merge all {:id k})))
     top-features)]))

(defn pages []
  (defn cafe-list-item [[props & rest :as cafe]]
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
                      (fn [[_ {:keys [id]} [_tagname _heading [_tag & rest]]]]
                        (let [rating-val (get-rating-val rest)]
                         [:li {:property "reviewRating" :typeof "Rating"}
                          [:a {:property "reviewAspect" :href (bobbery id)} id]
                          [:span {:property "ratingValue"} rating-val]]))
                      ratings)]
      [:details {:typeof "CriticReview"}
       [:summary
        [:h2 {:style (format "display: inline; color: %s" color)
              :property "itemReviewed" :typeof "CafeOrCoffeeShop"}
         [:a {:style "color: inherit" :href (cafe-url id)} name]]
        [:p {:property "abstract"} summary]]
       [:ul.ratings rating-lis]]))
  (let [cafe-map-list (mapcat
                       #(vector (-> % cafe-id cafe-url)
                                (->> % (apply cafe) html5))
                       cafes)
        cafe-map (apply hash-map cafe-map-list)
        head-group [:hgroup
                    [:h1 "the calgary " [:a {:href (bobbery "coffee")} "coffee bob"]]
                    [:p "a celebration of any aspect of anywhere that serves coffee"]]
        headstuff (list
                   silly-details
                   [:style ".ratings { & li {display: contents;} display: grid; grid-template-columns: repeat(5, 1fr 0.5fr); padding: 0; }"]
                   [:script {:type "module" :async true :src "/public/spider.js"}])
        index
        (layout
         {:headstuff headstuff}
         [:main head-group [:spider-graph {:aspects "rdfa"}]
          [:nav [:ul (map cafe-list-item cafes)]]])]
    (merge
     {"/" (html5 index)
      "/taxonomy/" (html5 taxonomy)}
     cafe-map)))

(def app (-> (stasis/serve-pages pages {:stasis/ignore-nil-pages? true})
             (optimus/wrap get-assets optimizations/none serve-live-assets)
             wrap-content-type))
