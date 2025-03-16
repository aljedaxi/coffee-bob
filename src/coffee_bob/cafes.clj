(ns coffee-bob.cafes
  (:require [coffee-bob.util :refer [depn]]
            [coffee-bob.html-utils :as h]))

(def bob-prefix "/taxonomy/")
(defn bobbery [s] (format "%s%s" bob-prefix s))

(def bottom-links
  [[:a {:href "about"} "about"]
   [:a {:href "about-me"} "about me"]
   [:a {:href "/coffee-bob"} "home"]
   [:a {:href "https://wiki.p2pfoundation.net/Peer_Production_License"} "PPL"]
   [:button {:type "button" :style "margin: 0" :onclick "void dispenseMittens()"} "coffee"]
   [:span "v0.1.0"]])

(defn layout [{:keys [headstuff id]} & children]
  [:html {:lang "en-CA" :id id}
   [:head
    headstuff
    (h/stylesheet "https://unpkg.com/normalize.css@8.0.1/normalize.css")
    (h/stylesheet "https://unpkg.com/concrete.css@2.1.1/concrete.css")
    [:meta {:charset "utf-8"}]
    [:link {:rel "icon" :href "/public/favicon.ico" :sizes "any"}]
    [:script {:src "/public/mittens.js"}]
    [:style "header {padding: 8rem 0}"]]
   [:body {:vocab "https://schema.org/" :typeof "WebPage"}
     children
     [:footer (interpose " ❧ " bottom-links)]]])

(defn coord [x y] (format "https://www.openstreetmap.org/#map=20/%f/%f" x y))
(defn location [x y] [:a {:href (coord x y)} "location"])
(defn cutout [& children]
  (list [:hr] [:div.centered children] [:hr]))

(def silly-details
  [:style " details[open] > summary {list-style-type: \"❦ \";} details > summary {list-style-type: \"❧ \";}"])

(defn cafe [{:keys [id name summary color]} & children]
  (layout
   {:id id
    :headstuff
    (list
     [:title name]
     [:style "svg > text {fill: var(--fg);}"]
     [:script {:type "module" :async true :src "/public/spider.js"}]
     [:style ".centered {display: grid; place-items: center}"]
     silly-details
     [:style "section {display: contents;}"]
     [:style ".spread {display: flex; justify-content: space-between; align-items: baseline; padding-block: 1rem; & h2, h3 {margin: 0;}}"]
     [:style ".dented {margin-inline-start: 20.75px; padding: 0} .golden-ratio + p {margin: 0}"])}
   [:main {:resource "" :property "review" :typeof "CriticReview"}
    [:hgroup
     [:h1.coffeehouse {:property "itemReviewed"
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

(defn section-header [level nps title]
  [:div.spread
   [level [:a {:href (bobbery title) :property "reviewAspect"} title]]
   (rating nps)])

(defn heading [level & children] [(symbol (format "h%d" level)) children])

(defn aspect-body [summary children]
  (when (not= 0 (count children))
    [:details [:summary {:property "ratingExplanation"} summary]
     [:div.dented children]]))

(defn reviewRating [level nps title & children]
  [:section {:id title
             :resource (format "#%s" title)
             :property "reviewRating"
             :typeof "Rating"}
   (section-header level nps title)
   children])

; TODO lookup prefLabel for title if exists
(defn sub-aspect [title nps sum & children]
  (reviewRating :h3 nps title (aspect-body sum children)))

; TODO try running nix run github:aljedaxi/rdfa2ttl "http://localhost:3000/euro/" to test
; TODO everything is currently fucked. consult
; * https://schema.org/CriticReview
; * https://schema.org/Rating --- consider that only the first p of the body should be the ratingExplanation
; * https://www.w3.org/TR/rdfa-lite
; * https://www.w3.org/TR/rdfa-primer/
(defn aspect [title nps sum & children]
  (reviewRating :h2 nps title (aspect-body sum children)))

(def european-bakery
  [{:id "european-bakery"
    :name "European Bakery"
    :color "#e83326"
    :summary "a bakery that serves surprisingly good turkish coffee"}
  [:p "the European bakery is, foremostly, a eastern European bakery. they advertise the Turkish coffee on a small sign above the baked goods in the corner of the cafe. it's absolutely one of the best in downtown, and only about 3 dollars.\n\n\t\t\tall in all, this is a hidden gem, and the reason i made this website."]
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
(def cafes [european-bakery velet])


; (
;  (:cafe
;  {:id "monogram"}
;  (:name "Monogram")
;  (:color "#997600")
;  (:summary "what was the crown jewel of the calgary scene")
;  (:recc "short-drinks")
;  (:write-up
;   "i don't have—a lot of—insider knowledge. but i pay close attention to some things when i go to cafes, especially when i go to the same cafe almost every day. Monogram was that for me. even [aubade](https://www.vancouvercoffeesnob.com/chinatown/aubade-coffee-2/) ran Monogram as their espresso.\n\n\t\t\tbut things have been shifting. many things that i need to write a general \"scene think piece\" on that don't fit in this article, but also smaller things. the cortados went from being routinely excellent, to sometimes excellent, to always good. staff churn has been increasing. the seasonal drinks have all kinda sucked recently (including the hot chocolate fest hot chocolates).\n\n\t\t\tMonogram is still better than like, Deville or Analog or Phil and Sebastian, but the margin gets slimmer every year. It's all a bit depressing to think about.")
;  (:coords "51.049096124603615, -114.06718542186842")
;  (:impression
;   {:timestamp "2024-03-21"}
;   (:coffee
;    {:summary "2"}
;    (:summary "Monogram is a no-mans-land between your 2s and your 3s")
;    (:write-up
;     "you should think of Monogram as quitessential third wave, calgary's revolver.")
;    (:short-drinks {:summary "2"} (:summary "very dependably good"))
;    (:pour-overs {:summary "2"} (:summary "chill. pretty standard."))
;    (:variety
;     {:summary "3"}
;     (:summary
;      "they run the standard gamut of coffee drinks, with little seasonal additions"))))                     )
;  (:cafe
;  {:id "t2722"}
;  (:name "T2722")
;  (:color "#1077f3")
;  (:coords "51.043029027997044, -114.03897643163158")
;  (:summary
;   "genuinely world class. consider yourself fortunate we have access to this")
;  (:reqq "flight")
;  (:write-up
;   "we might think of T2722 as having three prongs: coffee, tea, and pastry. this is paralleled in the staff, 3 experts: Elle, Julian, and the mysterious french baker who i haven't spoken with. these three work 12 hour shifts, every day. the depths of their passion is a mystery even to me.\n\n\t\t\tthe core idea is pairing these prongs. for each baked good, there's a matching tea or coffee drink. from each pairing emerges—at times—a truly sublime experience. i mean this very specifically and technically. it's like walking through a hallway that opens onto a cliff face, stumbling and flailing as you try not to fall [into a distractingly breath-taking vista](https://www.youtube.com/watch?v=Zya8jdPa-rU). after that first time, every pairing placed before you is a mountain. the sublime seizes.\n\n\t\t\tT2722 started as a pop-up for hot chocolate fest in the Weslian hotel. they were easily the best hot chocolate that year, and have continued to levitate above the competition thence. for 2 years they've been doing pop-ups for some of the worlds biggest brands, and hosting tastings for some of the city's richest people. all ingredients are of a highest quality you can find in this city. it's definitely the priciest cafe on this list, but you can only get comparable quality in Tokyo or Paris for twice the price.")
;  (:impression
;   {:timestamp "2024-02-18"}
;   (:coffee
;    {:summary "3"}
;    (:summary "a principled, french approach to specialty")
;    (:write-up
;     "a facet is a window; the facet holds the gem up, and pours light into it, and sucks light out of it.\n\n\t\t\t\t\tto mill or grind coffee is not to facet it. coffee as such is not coffee itself. coffee is this other thing. when i talk about coffee, there's the taste, the aroma, the sight, the feel of the heat in your hands. there's the atmosphere, the thought you spare to the people you bring with you.\n\n\t\t\t\t\tthere's the mouth feel, the heaviness, the comfort of coffee. as Paul Zits wrote, \"day break[s]//under the coffee pot\". T2722 captures this in a way i haven't felt since [aubade](https://www.vancouvercoffeesnob.com/chinatown/aubade-coffee-2/). it may not—it certainly doesn't—do high clarity the way other top tier coffeehouses do, but they do something special; something at the top of that game.\n\n\t\t\t\t\tplease note that the variety is massive and constantly shifting, so i can't walk you through the experience of drinking what's currently on. just know that, with a bit of a conversation, you'll be able to find exactly what you want.")
;    (:americano
;     {:summary "3"}
;     (:summary "proper, hearty, heartwarming"))
;    (:price
;     {:summary "2"}
;     (:summary "strongly depends on what you're getting")
;     (:write-up
;      "on the other hand, i had something of a similar quality at [glitch](https://tokyocoffee.org/2016/04/15/glitch-coffee-roasters/) in Tokyo—admittedly in ochanomizu—for 4 times the price? so, again, reasonable."))
;    (:short-drinks "3")
;    (:variety
;     {:summary "3"}
;     (:summary "genuinely absurd")
;     (:write-up
;      "the mind boggles. come in at a slow time and ask Julien for the list. then just ask him for a recommendation cuz it's all too much to take in."))
;    (:pour-overs
;     {:summary "3"}
;     (:summary "actually french presses")
;     (:write-up
;      "the french press gives body in a way that pour overs don't.")))
;   (:price {:summary "1"} (:summary "but well worth it"))
;   (:food
;    {:summary "3"}
;    (:summary
;     "finally, [some good fucking food](https://www.youtube.com/watch?v=yWKeZe6ggJI)")
;    (:write-up
;     "i've had a lot of very good coffee. when i had the coffee here, it didn't blow my mind[1]. the food was genuinely life changing. i'm not certain why i've focused exclusively on beverages; why i've never sought out solid food this good before. i now feel this folly.\n\n\t\t\t\t\ti can't capture the actual experience, nor the techniques nor ingredients. i asked Julien how to categorize the dishes and he hasn't come up with the words yet, so i don't feel too bad.\n\n\t\t\t\t\ttldr; get in before they raise the price.\n\n\t\t\t\t\t[1] tbh, i'd never had a _really_ good medium-dark roast, so it did open my mind in that regard.")
;    (:baked-goods "3"))
;   (:vibes {:summary "3"} (:apollonian-aestheticism "3"))
;   (:other-bevvies
;    {:summary "3"}
;    (:summary
;     "While the emphasis is on coffee and tea, they have a few fascinating items at the back of the menu")
;    (:write-up)
;    (:tea {:summary "3"})
;    (:hot-chocolate "3")
;    (:misc "3")
;    (:variety "3")))                                                     )
;  (:cafe
;  {:id "mobSquad"}
;  (:name "MobSquad Cafe")
;  (:color "#9b54f3")
;  (:coords "51.04511335156388, -114.06521097396123")
;  (:summary "excellent views")
;  (:write-up
;   "absolutely the best views in the city. if you can sneak in with a thermos of coffee from elsewhere, you've got the best of both worlds. inside, it feels like the decor was decided by an up and coming oil-sands failson with lots of capital and little taste.\n\n\t\t\tMobSquad cafe doubles as someone to marry to get a green card.")
;  (:impression
;   {:timestamp "distant past"}
;   (:coffee
;    {:summary "1"}
;    (:short-drinks
;     {:summary "1"}
;     (:summary "genuinely the worst coffee i've had in years")
;     (:write-up
;      "bitter, acidic, sharp. mild petroleum note. primary notes include ash and dust. i wipe my brow and i slap my nuts. this is it, the apocalypse."))
;    (:americano
;     {:summary "1"}
;     (:summary
;      "the americano misto tasted like—nothing. whispers of ash and paper.")))
;   (:price {:summary "2"})
;   (:vibes
;    {:summary "2"}
;    (:summary "get a window seat")
;    (:write-up
;     "the views are nice. i can see telus sky; the top of the bay building; the building Major Toms' is in; the head; &c. i can see the trains and the people come and go, the cars stop and start and idle.\n\n\t\t\t\t\tinside is supposed to be some sort of co-working space, so there's plenty of seating. i've never seen this place more than 25% full. outlets are plentiful.\n\n\t\t\t\t\tas for the \"vibe\" proper: absolute airspace. a charmless corporate appropriation of third wave. it [transubstantiates](https://alexanderpruss.blogspot.com/2022/01/a-horizontal-aspect-to.html) the fairly standard [sirius xm coffee house](https://www.siriusxm.com/channels/the-coffee-house) bops into [pure bathos](https://en.wikipedia.org/wiki/Bathos).\n\n\t\t\t\t\ttruly an edifying experience.")
;    (:view "3")
;    (:seating "3")))                            )
;  (:cafe
;  {:id "treno"}
;  (:name "Treno")
;  (:color "#cc34cd")
;  (:coords "51.04388902810284, -114.0717094825913")
;  (:summary "it's hard to think of something to say about Treno")
;  (:write-up
;   "my cat always used to say: <q>better to say nothing at all than something rude. focus on the positives!</q>. to honour his cat-swag, i'll end the review here.")
;  (:impression
;   {:timestamp "distant past"}
;   (:coffee {:summary "1"} (:espresso "1"))
;   (:price {:summary "2"}))           )
;  (:cafe
;  {:id "q-lab"}
;  (:name "Q Lab")
;  (:color "#c85b00")
;  (:summary "a great place to try all kinds of stuff")
;  (:write-up
;   "i've got a feeling that i didn't get the full Q Lab experience. sure, i got the flight, and i was walked through the options, and i chose cool options and spoke with the barista. somewhere along the line, i got the feeling i missed something essential. when most coffee houses put the word \"coffee\" on the menu, they mean only the \"coffee liqueur\", the liquid byproduct of the coffee brewing process. when Q Labs uses the word coffee, they mean liquid, roasted beans, green coffee, &c.\n\n\t\t\tThere's something very special here that's easily missed.")
;  (:impression
;   {:timestamp "2024-03-23"}
;   (:coffee
;    {:summary "2"}
;    (:summary "closer to cupping")
;    (:write-up
;     "this is what's convinced me i need an internal ranking system among 2s. this is the strongest 2 on the site. the coffee is good. the coffee is great.\n\n\t\t\t\t\tit just isn't in the same league as Sought and Found, Paradigm Spark, T2722, &c., because the place is more about green than they are about brown. it's closer to cupping in the sense that this is a pure exploration of terroir and processing technique, to the exclusion of brewing technique.\n\n\t\t\t\t\tagain, not bad. better than ever other two. it's just focused on something else.")
;    (:flights
;     {:summary "3"}
;     (:summary "a collaborative, exploratory experience")
;     (:write-up
;      "this is the crown jewel of the experience. you need to get the flight. you _need_ to get the flight. just get the flight.")))
;   (:price
;    {:summary "2"}
;    (:summary
;     "[no complaints](https://youtu.be/9xZb4AMi--c?si=p4SRydcNxVfDi1J1&t=59)"))
;   (:vibes
;    {:summary "2"}
;    (:summary "spartan, technical, focused")
;    (:write-up
;     "the first thing you're struck with on your way in is that you're not struck by any single thing. there's a lot going on, especially when you first enter. there's wall of windows—the torrent of cold light—leading to the wall of bean bags, leading to a wall of equipment, leading to an espresso machine??? a deeply mysterious espresso machine on a table, leading to the cash register.\n\n\t\t\t\t\tas it stands—which may end soon—the storefront oscillates between spartan and cluttered. it feels like a _lab_; a place where people work on and with coffee. it's neat. i wouldn't be surprised if this feeling was just part of the opening process, but i would be depressed."))
;   (:staff {:summary "3"} (:summary "super chill cat")))                            )
;  (:cafe
;  {:id "velet"}
;  (:impression
;   {:timestamp "2024-12-09"}
;   (:coffee
;    {:summary "2"}
;    (:summary "good! really reminds me of whistler.")
;    (:write-up "nothing terribly special about it."))
;   (:vibes
;    {:summary "3"}
;    (:write-up
;     "what truly pulls this place together is the exposed brick. the chairs are draped in marathon numbers; half the wall decor is skis, the other half is pictures of mountains and drawings of skiiers; the coat rack and the divider between the seating and brewing are both made of reclaimed skis. it's being among unfinished wood beams and the raw concrete floor that reveals these materials as honest, functional, and stylish")))           )
;  (:chain
;  {:id "analog"}
;  (:cafe
;   {:id "bankers-hall"}
;   (:name "Analog — Banker's Hall")
;   (:color "#008c5c")
;   (:summary "Really weirdly beautiful")
;   (:write-up
;    "a plus 15 is a glass box, 15 meters above the ground. you pass through that glass box into a tall, slim glass box. You pass countless, uniform glass boxes, housing countless, multiform businesses. tinted windows chill light; it spills out onto marble.\n\n\t\t\t\tbut this marble is the odd accent in dark wood. green snakes down the rafters, and faded ruby tiling holds up the espresso machine. dark wood laminates spills out from the machine, bearing low leather seating and wide wooden tables. the lights are round and warm.\n\n\t\t\t\tdoes this location have a distinct identity to—say—the Deville in the Google building? no. does it—as the kids say—beat the airspace allegations? certainly not. but the way it suddenly shatters the undifferentiated droll of the +15s is irresistible—every now and then.")
;   (:impression
;    {:timestamp "2024-05-03"}
;    (:coffee
;     {:summary "2"}
;     (:summary "par. slightly worse than the 17th location."))
;    (:price {:summary "2"} (:summary "exactly what you'd expect"))
;    (:vibes
;     {:summary "2"}
;     (:summary "see above")
;     (:write-up
;      "anywhere else, it'd be pretty boring. but the use of space, here, is stunning. it's warm and open and airy, and intimate and round.\n\n\t\t\t\t\t\tthe layout is exceptionally praiseworthy. the pillars and planters partition the space into private places, while preserving the open, inviting atmosphere. it should be a case study.")
;     (:seating {:summary "3"} (:summary "bountiful")))
;    (:staff {:summary "2"} (:summary "cool enough people."))))                     )
;  (:cafe
;  {:id "particle"}
;  (:name "Particle Coffee")
;  (:links (:insta "https://www.instagram.com/particlecoffee/"))
;  (:summary "gorgeous, elegant washed coffees")
;  (:write-up
;   "Particle coffee is all about washed coffees: elegant, complex, restrained. the menu revolves around pour overs, usually of coffee roasted by [Tim Wendelboe](https://timwendelboe.no/).\n\n\t\t\tParticle coffee is the project of [Alex Cao](https://www.instagram.com/alex.yingjian/), lead barista at Aritzia's [A-OK Cafe](https://www.instagram.com/a.okcafe/) in [Chinook Mall](https://shops.cadillacfairview.com/property/cf-chinook-centre)")
;  (:impression
;   {:timestamp "2024-07-20"}
;   (:coffee
;    {:class "3"}
;    (:summary "exceptional washed coffees")
;    (:write-up
;     "this is absolutely the place in the city if you like that elegant, nordic flavour profile. everything i've served has ranged from top-notch to life-changing.")
;    (:flights
;     {:class "3"}
;     "the two things i get here are pour over flights and \"one and one\"s: an espresso shot, split into one cup black and the other with milk. both are always exceptional")))                ))
