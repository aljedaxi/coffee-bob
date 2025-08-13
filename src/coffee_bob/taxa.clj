(ns coffee-bob.taxa
  (:require [coffee-bob.cafes :refer [layout cafes bobbery class-link cafe silly-details]]
            [markdown-to-hiccup.core :as m]
            [coffee-bob.html-utils :as h]
            [coffee-bob.rdf :refer [top-features]]))

(defn md [s]
  (->> s (format "./resources/static/taxonomy/%s.md") m/file->hiccup list))

(defn skos [property & children]
  [:p {:property (format "skos:%s" property)} children])

(defn narrower [& children]
  [:p.children "children:"
   (map (fn [%] [:a {:rel "skos:narrower" :href (bobbery %)} %])
        children)])

(defn eg [& children]
  [:p "eg. " [:span {:property "skos:example"} children]])

(defn nar [& children]
  (print children)
  (cond
    (nil? children) nil
    (keyword? children) (apply narrower [children])
    (sequential? children) (apply narrower children)
    :else nil))

(defn top-feature
  [{:keys
    [prefLabel definition editorialNote scopeNote example id ]}
   & children]
  (print children)
  [:section {:id id :about (format "[bob:%s]" id) :typeof "[schema:reviewAspect]"}
   [:div
    [:h2 {:property "skos:prefLabel" :style "display: inline"} (or prefLabel id)]]
   (skos "definition" definition)
   (if editorialNote (skos "editorialNote" editorialNote) nil)
   (if example (eg example) nil)
   (if scopeNote (skos "scopeNote" scopeNote) nil)
   children])

(def taxonomy
  (layout
   {:headstuff (list
                [:style ".children {display: flex; gap: 4px;}"]
                [:style "h2 {margin: 0} section {padding-block: 2rem !important}"]
                [:style "section div {display: grid; grid-template-columns: 1fr 0.5fr; align-items: baseline}"])
    :prefix "bob: http://localhost:3000/taxonomy/# schema: https://schema.org/"}
   [:main
    [:hgroup [:h1 "the calgary coffee vocabulary"]
     [:p "ways of talking about coffee"]]
    (top-feature
     {:id "Coffee" :editorialNote (md "coffee/editorialNote")}
     (skos "scopeNote" (md "coffee/scopeNote"))
     (narrower "Turkish" "Espresso" "ShortDrinks" "LongBlacks" "Flights" "PourOvers"))
    (top-feature
     {:id "Turkish"
      :definition "alternatively known as arabic coffee. spread throughout the middle east by the ottoman empire, it's served with the grounds in the cup, like cupping coffee. for historical reasons, these days, it tends to be served with spices."})
    (top-feature
     {:id "Espresso"
      :definition "the ontic ground of all short drinks, long blacks &c."})
    (top-feature
     {:id "ShortDrinks"
      :prefLabel "short drinks"
      :example "your cortados, macchiatos, cappuccinos, &c."
      :editorialNote "with the exception of milk texture, i tend to think of these as all essentially the same, and choose whatever works best with the espresso."
      :definition "a double of espresso with less milk than a latte"})
    (top-feature
     {:id "LongBlacks"
      :prefLabel "long blacks"
      :example "americanos, long blacks, short blacks"
      :definition "hot watered down espresso"})
    (top-feature
     {:id "Flights" :definition "sometimes you want to try a lot of things...."})
    (top-feature
     {:id "PourOvers" :prefLabel "pour overs &c." :definition "if you could serve it at brewer's cup, it's in"})
    (top-feature
     {:id "Space" :definition "the parts of being that americans call 'common sense', 'objective'" }
     (narrower "Power" "Seating" "Architecture"))
    (top-feature
     {:id "Architecture"
      :example "the way the door arches, the way cafe integrates with the rest of the building"
      :definition "that which cannot be seen, but must speak through the seen"})
    (top-feature
     {:id "Seating" :definition "will you find a seat? when? will it be comfy?"})
    (top-feature
     {:id "Power"
      :definition "do they have a lot of outlets? may you use your laptop?"})
    (top-feature
     {:id "Price"
      :example "3 bucks for an americano is cheap."
      :definition "is this cheap? is this everyday bro? or is this a real excursion? a splurge? not normalized for quality; pure function of the market. please note that this is against other specialty shops, not tim hortons."})
    (top-feature
     {:id "Food" :definition "edible solids, and perhaps soups." }
     (narrower "BakedGoods"))
    (top-feature
     {:id "Vibes"
      :definition "branding; the feelings of the space, what the space engenders;"
      :scopeNote "this should be understood in its fullest, most diffuse sense. the vibes are the feel, the experience."}
     (eg "what you figure they're trying to accomplish")
     (eg "the way the lampshades make you feel")
     (narrower "View" "Appolonianism" "Comfy" "Cool"))
    (top-feature
     {:id "Cool"
      :definition "cool neither matters nor exists anymore. i'm just old and bitter"})

    (top-feature {:id "View" :definition "What's the view out the window?"})
    (top-feature
     {:id "Appolonianism" :definition "supreme focus on clarity of flavour on the nose and palate"})
    (top-feature
     {:id "Comfy" :definition "can you really sink in here? focus on soft, worn materials; warm colours; open spaces"})
    (top-feature
     {:id "OtherBevvies" :prefLabel "drinks" :definition "drinks that aren't coffee" }
     (narrower "HotChocolate" "MiscDrinks"))
    (top-feature
     {:id "Tea" :definition "a drink produced by immersion brewing the leaves of camellia sinensis, or whatever else you want these days."
      :scopeNote "because calgary isn't much of a tea city—i can name two decent tea shops off the top of my head—this includes matcha lattes, houjichas, &amp;c."})
    (top-feature
     {:id "HotChocolate" :prefLabel "hot chocolate" :definition "when the chocolate hot tho?"})
    (top-feature
     {:id "MiscDrinks" :prefLabel "misc drinks" :definition "usually stuff that's specific to this place"})
    ;; (map
    ;;  (fn [[k {:as all}]]
    ;;    (top-feature (merge all {:id k})))
    ;;  top-features)
    ]))
