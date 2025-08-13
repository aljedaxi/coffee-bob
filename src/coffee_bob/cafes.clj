(ns coffee-bob.cafes
  (:require [clojure.string :refer [upper-case]]
            [markdown-to-hiccup.core :as m]
            [coffee-bob.html-utils :as h]))

(defn capitalize [s] (str (upper-case (subs s 0 1)) (subs s 1)))

(def bob-prefix "/taxonomy#")
(defn bobbery [s] (format "%s%s" bob-prefix s))
(defn class-link [s] (format "%s%s" bob-prefix (capitalize s)))

(def bottom-links
  [[:a {:href "/about"} "about"]
   [:a {:href "/about-me"} "about me"]
   [:a {:href "/coffee-bob"} "home"]
   [:a {:href "https://wiki.p2pfoundation.net/Peer_Production_License"} "PPL"]
   [:button {:type "button" :style "margin: 0" :onclick "void dispenseMittens()"} "coffee"]
   [:span "v0.1.0"]])

(defn layout [{:keys [prefix headstuff id]} & children]
  [:html {:lang "en-CA" :id id :prefix prefix}
   [:head
    headstuff
    (h/stylesheet "/public/main.css")
    [:meta {:charset "utf-8"}]
    [:link {:rel "icon" :href "/public/favicon.ico" :sizes "any"}]
    [:script {:src "/public/mittens.js"}]]
   [:body {:vocab "https://schema.org/" :typeof "WebPage"}
    children
    [:footer (interpose " ❧ " bottom-links)]]])

(defn coord [x y] (format "https://www.openstreetmap.org/#map=20/%f/%f" x y))
(defn location [x y] [:a {:href (coord x y)} "location"])
(defn insta [s]
  [:a {:href (format "https://www.instagram.com/%s" s)} "insta"])
(defn cutout [& children]
  (list [:hr] [:div.cutout children] [:hr]))

(def silly-details
  [:style " details[open] > summary {list-style-type: \"❦ \";} details > summary {list-style-type: \"❧ \";}"])

(defn cafe [{:keys [id name summary color href]} & children]
  (layout
   {:id id
    :headstuff
    (list
     [:title name]
     [:style "svg > text {fill: var(--fg);}"]
     [:script {:type "module" :async true :src "/public/spider.js"}]
     [:style ".cutout {display: flex; justify-content: center; gap: 6px;}"]
     silly-details
     [:style "section {display: contents;}"]
     [:style ".spread {display: flex; justify-content: space-between; align-items: baseline; padding-block: 1rem; & h2, h3 {margin: 0;}}"]
     [:style ".dented {margin-inline-start: 20.75px; padding: 0} .golden-ratio + p {margin: 0}"])}
   [:main {:resource "" :property "review" :typeof "CriticReview"}
    [:hgroup
     [:h1.coffeehouse {:resource ""
                       :property "itemReviewed"
                       :style (format "color: %s" color)
                       :typeof "CafeOrCoffeeShop"} name]
     [:p {:property "abstract"} summary]]
    [:spider-graph {:aspects "rdfa" :rating "[typeof=\"Rating\"]:has(h2)"}]
    children]))

(defn rating [& children]
  [:span
   [:meta {:property "worstRating" :content "1"}]
   [:sup {:property "ratingValue"} children]
   "/"
   [:sub {:property "bestRating"} [:a {:href "about/methodology/#nps"} "3"]]])

(defn section-header [level nps id & children]
  [:div.spread
   [level [:a {:href (class-link id) :property "reviewAspect"} children]]
   (rating nps)])

(defn aspect-body [summary children]
  (when (not= 0 (count children))
    [:details [:summary {:property "ratingExplanation"} summary]
     [:div.dented children]]))

(defn reviewRating [level nps title id & children]
  [:section {:id title
             :resource (format "#%s" id)
             :property "reviewRating"
             :typeof "Rating"}
   (section-header level nps id title)
   children])

; TODO lookup prefLabel for title if exists
(defn sub-aspect [title nps sum & children]
  (reviewRating :h3 nps title title (aspect-body sum children)))

; TODO try running nix run github:aljedaxi/rdfa2ttl "http://localhost:3000/euro/" to test
; TODO everything is currently fucked. consult
; * https://schema.org/CriticReview
; * https://schema.org/Rating --- consider that only the first p of the body should be the ratingExplanation
; * https://www.w3.org/TR/rdfa-lite
; * https://www.w3.org/TR/rdfa-primer/
(defn aspect [title nps sum & children]
  (reviewRating :h2 nps title (capitalize title) (aspect-body sum children)))

(defn other-bevvies [nps sum & children]
  (reviewRating :h2 nps "drinks" "OtherBevvies" (aspect-body sum children)))
(defn pour-over [nps sum & children]
  (reviewRating :h3 nps "pour overs" "PourOver" (aspect-body sum children)))
(defn short-drink [nps sum & children]
  (reviewRating :h3 nps "short drinks" "ShortDrink" (aspect-body sum children)))

(def european-bakery
  [{:id "european-bakery"
    :href "https://eurobakerydeli.com/"
    :name "European Bakery"
    :color "#e83326"
    :summary "a bakery that serves surprisingly good turkish coffee"}
   [:p "the European bakery is, foremostly, a eastern European bakery. they advertise the Turkish coffee on a small sign above the baked goods in the corner of the cafe. it's absolutely one of the best in downtown, and only about 3 dollars."]
   [:p "all in all, this is a hidden gem, and the reason i made this website."]
   (cutout (location 51.03766612184806 -114.07219196898996))
   (aspect "coffee" 2 "they only serve turkish coffee. it's good."
           (sub-aspect "price" 3 nil)
           (sub-aspect "turkish" 2 nil)
           (sub-aspect "variety" 1 nil))
   (aspect "price" 3 "monumental value."
           (sub-aspect "baked-goods" 3 nil)
           (sub-aspect "variety" 3 nil))
   (aspect "food" 2 "all manner of (mostly eastern) european baked goods and imports"
           [:p "the baked goods run the gamut from flat breads, northern european loaves, baguettes, and pretzels, to savory stuffed goods like "
            [:a {:href "https://www.thespruceeats.com/traditional-yugoslavian-rolled-burek-borek-recipe-1805900"} "bureks"]
            ". on the right side of the store are imports from all around Europe. everything is excellent and very reasonably priced."])
   (aspect "vibes" 2 "it's a bakery with a little grocery store")])

(def velet
  [{:id "velet"
    :color "#ff005d"
    :name "Velet Bike-Ski Cafe"
    :summary "coffee, turkish baked goods, and bike/ski repairs"}
   [:p "velet is actually cool, in that effervescent, classical american/french sense. something about the japanese hiphop, turkish menu items, the snowboarding videos constantly playing on the tv, the exposed pillars and brick walls, and the used skis everywhere, that pulls together into something that feels real and raw. weeds comes close, but weeds feels more middle aged, comfortably not cool anymore, focused now on being cozy. any object in here could speak for lifetimes."]
   [:p "a while ago, i had the distinct pleasure of spending a few days in whistler. velet would fit perfectly into that scene; not just in decor or design philosophy, but the coffee is scary reminiscent of some of the best spros i had there."]
   [:p "finally, i'd like to note that this is also a bike/ski tune up shop. i don't have either of those so i can't speak to the quality, but i get the feeling the owner knows what he's doing."]
   (aspect
    "coffee" 2 "solid! fairly standard menu with little turkish additions"
    [:p "(please don't take turkish too seriously. the ottoman empire was very large and very influential.)"]
    (sub-aspect "price" 2 nil)
    (sub-aspect "turkish" 2 "one of the less spiced in the city. defined, subdued bitterness. lively, contained acidity. a bit roasty. generally, well balanced, easy drinking.")
    (sub-aspect "variety" 3 "salep? who has salep these days? baller shit."))
   (aspect "price" 2 "standard"
           (sub-aspect "bakedGoods" 2 nil)
           (sub-aspect "variety" 2 nil))
   (aspect "food" 2 "turkish stuff" [:p "i haven't had a lot of it, but everything i've had is good. i usually don't buy baked goods, but i feel good when buying this."])
   (aspect
    "vibes" 3 "immaculate"
    (sub-aspect "comfy" 3 "fireplace. most of the seats are cushy. most of the colours are warm; most of the materials are warm, worn, exposed, rough. the staff is friendly and inviting."))])

(def semantics
  [{:id "semantics"
    :name "Semantics Cafe"
    :color "#5ba8f7"
    :summary "too early to say, but i'm interested in what's to come"}
   [:p "semantics was dreaming;"]
   (aspect
    "coffee" 2 "solid! on par with the alfornos of the world"
    [:p "pleasant almond notes. mild roastiness. mild acidity. very par, very middle of the road."]
    (sub-aspect "espresso" 2 "nothing special")
    (sub-aspect "short-drinks" 2 "smooth, inoffensive."))
   (aspect
    "vibes" 3 "this " [:em "must"] " be understood in the fullest, most diffuse sense."
    (sub-aspect "power" 2 "outlets run along the walls. you'll want to sit on the edges if you need to plug in")
    (sub-aspect "seating" 3 "there's a lot. most of it is hard plastic, but there's a lovely little couch by the door")
    (sub-aspect "space" 2 "lovely. feels a bit sparse and empty right now, but the bones are solid."))])

(def aubade [:a {:href "https://www.vancouvercoffeesnob.com/chinatown/aubade-coffee-2/"} "aubade"])
(def glitch [:a {:href "https://tokyocoffee.org/2016/04/15/glitch-coffee-roasters/"} "glitch"])

(def monogram
  [{:id "monogram" :name "Monogram" :color "#997600" :summary "what was the crown jewel of the calgary scene" :recc "short-drinks"}
   [:p "i don't have—a lot of—insider knowledge. but i pay close attention to some things when i go to cafes, especially when i go to the same cafe almost every day. Monogram was that for me. even " aubade " ran Monogram as their espresso."]
   [:p "but things have been shifting. many things that i need to write a general \"scene think piece\" on that don't fit in this article, but also smaller things. the cortados went from being routinely excellent, to sometimes excellent, to always good. staff churn has been increasing. the seasonal drinks have all kinda sucked recently (including the hot chocolate fest hot chocolates)."]
   [:p "Monogram is still better than like, Deville or Analog or Phil and Sebastian, but the margin gets slimmer every year. It's all a bit depressing to think about."]
   (cutout (location 51.049096124603615, -114.06718542186842))
   (aspect "coffee" 2 "Monogram is a no-mans-land between your 2s and your 3s"
           [:p "you should think of Monogram as quitessential third wave, calgary's revolver."]
           (sub-aspect "short-drinks" 2 "very dependably good")
           (sub-aspect "pour-overs" 2 "chill. pretty standard.")
           (sub-aspect "variety" 3 "they run the standard gamut of coffee drinks, with little seasonal additions"))])

(def t2722
  [{:id "t2722" :name "T2722" :color "#1077f3"
    :summary "something that requires an entirely new language"
    :reqq "flight"}
   [:p "genuinely world class. consider yourself fortunate we have access to this"]
   [:p "we might think of T2722 as having three prongs: coffee, tea, and pastry. this is paralleled in the staff, 3 experts: Elle, Julian, and the mysterious french baker who i haven't spoken with. these three work 12 hour shifts, every day. the depths of their passion is a mystery even to me."]
   [:p "the core idea is pairing these prongs. for each baked good, there's a matching tea or coffee drink. from each pairing emerges—at times—a truly sublime experience. i mean this very specifically and technically. it's like walking through a hallway that opens onto a cliff face, stumbling and flailing as you try not to fall "
    [:a {:href "https://www.youtube.com/watch?v=Zya8jdPa-rU"} "into a distractingly breath-taking vista"]
    ". after that first time, every pairing placed before you is a mountain. the sublime seizes."]
   [:p "T2722 started as a pop-up for hot chocolate fest in the Weslian hotel. they were easily the best hot chocolate that year, and have continued to levitate above the competition thence. for 2 years they've been doing pop-ups for some of the worlds biggest brands, and hosting tastings for some of the city's richest people. all ingredients are of a highest quality you can find in this city. it's definitely the priciest cafe on this list, but you can only get comparable quality in Tokyo or Paris for twice the price."]
   (cutout (location 51.043029027997044 -114.03897643163158))
   (aspect "coffee" 3 "a principled, french approach to specialty"
           [:p "a facet is a window; the facet holds the gem up, and pours light into it, and sucks light out of it."]
           [:p "to mill or grind coffee is not to facet it. coffee as such is not coffee itself. coffee is this other thing. when i talk about coffee, there's the taste, the aroma, the sight, the feel of the heat in your hands. there's the atmosphere, the thought you spare to the people you bring with you."]
           [:p "there's the mouth feel, the heaviness, the comfort of coffee. as Paul Zits wrote, \"day break[s]//under the coffee pot\". T2722 captures this in a way i haven't felt since "
            aubade
            ". it may not—it certainly doesn't—do high clarity the way other top tier coffeehouses do, but they do something special; something at the top of that game."]
           [:p "please note that the variety is massive and constantly shifting, so i can't walk you through the experience of drinking what's currently on. just know that, with a bit of a conversation, you'll be able to find exactly what you want."]
           (sub-aspect "americano" 3 "proper, hearty, heartwarming")
           (sub-aspect "price" 2 "strongly depends on what you're getting"
                       [:p "on the other hand, i had something of a similar quality at"
                        glitch
                        " in Tokyo—admittedly in ochanomizu—for 4 times the price? so, again, reasonable."])
           (sub-aspect "short-drinks" 3 "")
           (sub-aspect "variety" 3 "genuinely absurd" [:p "the mind boggles. come in at a slow time and ask Julien for the list. then just ask him for a recommendation cuz it's all too much to take in."])
           (sub-aspect "pour-overs" 3 "actually french presses" [:p "the french press gives body in a way that pour overs don't."])
           (sub-aspect "price" 1 "but well worth it"))
   (aspect "food" 3
           [:span
            "finally, "
            [:a {:href "https://www.youtube.com/watch?v=yWKeZe6ggJI"}
             "some good fucking food"]]
           [:p "i've had a lot of very good coffee. when i had the coffee here, it didn't blow my mind[1]. the food was genuinely life changing. i'm not certain why i've focused exclusively on beverages; why i've never sought out solid food this good before. i now feel this folly."]
           [:p "i can't capture the actual experience, nor the techniques nor ingredients. i asked Julien how to categorize the dishes and he hasn't come up with the words yet, so i don't feel too bad."]
           [:p "tldr; get in before they raise the price."]
           [:p "[1] tbh, i'd never had a "
            [:em "really"]
            "good medium-dark roast, so it did open my mind in that regard."]
           (sub-aspect "baked-goods" 3 ""))
   (aspect "vibes" 3 "" (sub-aspect "apollonian-aestheticism" 3 ""))
   (other-bevvies 3 "While the emphasis is on coffee and tea, they have a few fascinating items at the back of the menu"
                  (sub-aspect "tea" 3 "")
                  (sub-aspect "hot-chocolate" 3 "")
                  (sub-aspect "misc" 3 "")
                  (sub-aspect "variety" 3 ""))])

(def mobSquad
  [{:id "mobSquad"
    :name "MobSquad Cafe"
    :color "#9b54f3"
    :summary "gorgeous views"}
   [:p "absolutely the best views in the city. if you can sneak in with a thermos of coffee from elsewhere, you've got the best of both worlds. inside, it feels like the decor was decided by an up and coming oil-sands failson with lots of capital and little taste."]
   [:p "MobSquad cafe doubles as someone to marry to get a green card."]
   (cutout (location 51.04511335156388 -114.06521097396123))
   (aspect "coffee" 1 "genuinely the worst coffee i've had in years"
           [:p "bitter, acidic, sharp. mild petroleum note. primary notes include ash and dust. i wipe my ass and i slap my nuts. this is it, the apocalypse."]
           (sub-aspect "americano" 1 "the americano misto tasted like nothing, thankfully. whispers of ash and paper."))
   (aspect "price" 2 "")
   (aspect "vibes" 2 "get a window seat"
           [:p "the views are nice. i can see telus sky; the top of the bay building; the building Major Toms' is in; the head; &c. i can see the trains and the people come and go, the cars stop and start and idle."]
           [:p "inside is supposed to be some sort of co-working space, so there's plenty of seating. i've never seen this place more than 25% full. outlets are plentiful."]
           [:p "as for the \"vibe\" proper: absolute airspace. a charmless corporate appropriation of third wave. it "
            [:a {:href "https://alexanderpruss.blogspot.com/2022/01/a-horizontal-aspect-to.html"} "transubstantiates"]
            " the fairly standard "
            [:a {:href "https://www.siriusxm.com/channels/the-coffee-house)"} "sirius xm coffee house"]
            " bops into "
            [:a {:href "https://en.wikipedia.org/wiki/Bathos"} " pure bathos."]]
           [:p "truly an edifying experience."]
           (sub-aspect "view" 3 "")
           (sub-aspect "seating" 3 nil))])

(def q-lab
  [{:id "q-lab" :name "Q Lab" :color "#c85b00" :summary "a great place to try all kinds of stuff"}
   [:p "i've got a feeling that i didn't get the full Q Lab experience. sure, i got the flight, and i was walked through the options, and i chose cool options and spoke with the barista. somewhere along the line, i got the feeling i missed something essential. when most coffee houses put the word \"coffee\" on the menu, they mean only the \"coffee liqueur\", the liquid byproduct of the coffee brewing process. when Q Labs uses the word coffee, they mean liquid, roasted beans, green coffee, &c."]
   [:p "There's something very special here that's easily missed."]
   (aspect "coffee" 2 "closer to cupping"
           [:p "this is what's convinced me i need an internal ranking system among 2s. this is the strongest 2 on the site. the coffee is good. the coffee is great."]
           [:p "it just isn't in the same league as Sought and Found, Paradigm Spark, T2722, &c., because the place is more about green than they are about brown. it's closer to cupping in the sense that this is a pure exploration of terroir and processing technique, to the exclusion of brewing technique."]
           [:p "again, not bad. better than ever other two. it's just focused on something else."]
           (sub-aspect "flights" 3 "a collaborative, exploratory experience"
                       [:p "this is the crown jewel of the experience. you need to get the flight. you _need_ to get the flight. just get the flight."]))
   (aspect "price" 2 [:a {:href "https://youtu.be/9xZb4AMi--c?si=p4SRydcNxVfDi1J1&t=59"} "no complaints"])
   (aspect "vibes" 2 "spartan, technical, focused"
           [:p "the first thing you're struck with on your way in is that you're not struck by any single thing. there's a lot going on, especially when you first enter. there's wall of windows—the torrent of cold light—leading to the wall of bean bags, leading to a wall of equipment, leading to an espresso machine??? a deeply mysterious espresso machine on a table, leading to the cash register."]
           [:p "as it stands—which may end soon—the storefront oscillates between spartan and cluttered. it feels like a _lab_; a place where people work on and with coffee. it's neat. i wouldn't be surprised if this feeling was just part of the opening process, but i would be depressed."])
   (aspect "staff" 3 "super chill cat")])

(def analog-bankers-hall
  [{:id "analog-bankers-hall" :name "Analog — Banker's Hall" :color "#008c5c" :summary "Really weirdly beautiful"}
   [:p "a plus 15 is a glass box, 15 meters above the ground. you pass through that glass box into a tall, slim glass box. You pass countless, uniform glass boxes, housing countless, multiform businesses. tinted windows chill light; it spills out onto marble."]
   [:p "marble is the odd accent in dark wood. green snakes down the rafters, and faded ruby tiling holds up the espresso machine. dark wood laminates spills out from the machine, bearing low leather seating and wide wooden tables. the lights are round and warm."]
   [:p "does this location have a distinct identity to—say—the Deville in the Google building? no. does it—as the kids say—beat the airspace allegations? certainly not. but the way it suddenly shatters the undifferentiated droll of the +15s is irresistible—every now and then."]
   (aspect "coffee" 2 "par. slightly worse than the 17th location.")
   (aspect "price" 2 "exactly what you'd expect")
   (aspect "vibes" 2 "see above"
           [:p "anywhere else, it'd be pretty boring. but the use of space, here, is stunning. it's warm and open and airy, and intimate and round."]
           [:p "the layout is exceptionally praiseworthy. the pillars and planters partition the space into private places, while preserving the open, inviting atmosphere. it should be a case study."]
           (sub-aspect "seating" 3 "bountiful"))
   (aspect "staff" 2 "cool enough people")])

(defn particle []
  (defn md [s]
    (->> s
         (format "./resources/static/particle/%s.md")
         m/file->hiccup
         list))
  [{:id "particle" :name "Particle Coffee" :color "#72b622"
    :summary "clean washed coffees and fantastic seasonals"}
   (md "write-up")
   (cutout (location 51.03766708785928 -114.08121351161644)
           (insta "particlecoffee"))
   (aspect "coffee" 3 "clean, complex, complete"
           (pour-over 3 "big on the nose; round in the mouth; elegant on the palate" (md "coffee/pourOver"))
           (short-drink 3 "my man makes a mean cortado" [:p "you can't go to Particle without getting an oat milk one and one. if you're feeling saucy, consider the 1&amp;1&amp;1: a spro, a cortado, and a dirty or a shakerato."]))
   (other-bevvies 3 "some really neat stuff" (md "OtherBevvies"))
   (aspect "vibes" 2 "tech worker" (md "vibes/index"))
   (aspect "food" 2 "above average, but nothing groundbreaking" [:p "sourced from the lovely " [:a {:href "https://www.kanyoucake.com/"} "Kan U Cake"]])
   (aspect "staff" 3 "Alex is really good at his job")
   (aspect "price" 2 "very reasonable" "The pour overs are a bit more expensive than like, Phil and Seb, but when you factor in quality, you're getting exceptional value. espresso based drinks and seasonals are prefectly on par.")])

(def cafes [european-bakery velet monogram t2722 mobSquad q-lab analog-bankers-hall semantics (particle)])
