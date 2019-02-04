(ns playphraseme.common.util
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [clojure.walk :as walk]
            [goog.string :as gstring]
            [goog.string.format]
            [camel-snake-kebab.core :as keb :refer [->camelCase ->kebab-case ->kebab-case-keyword]]
            [cljs.pprint :refer [pprint]]
            [playphraseme.common.nlp :as nlp]
            [goog.crypt.base64 :as base-64]
            [re-frame.core :refer [subscribe dispatch reg-event-db reg-event-fx reg-sub] :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn class->str [& cls]
  (->> cls
       (filter (complement nil?))
       (map name)
       (string/join " ")))

(defn flag-map
  [& params]
  (apply merge
         (map (fn [k]
                (if (map? k)
                  k
                  {k true})) params)))

(defn go-url!
  ([url] (go-url! url false))
  ([url new-window?]
   (if new-window?
     (js/window.open url "_blank")
     (aset js/window.location "href" url))
   url))

(defn locale-name []
  (some-> @(rf/subscribe [:locale]) name))

(defn transform-keys [m f]
  (let [f (fn [[k v]] [(f k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn cemelify-keys [x]
  (some-> x (transform-keys ->camelCase) (transform-keys name)))

(defn keywordize [x]
  (some-> x (transform-keys keyword)))

(defn all-keys-camel-to-dash [x]
  (some-> x (transform-keys ->kebab-case-keyword)))

(defn prepare-to-clj [x]
  (some-> x js->clj keywordize all-keys-camel-to-dash))

(defn prepare-to-js [m]
  (clj->js (cemelify-keys m)))

(defn catch-err [fn]
  (try
    (fn)
    (catch js/Error e (js/console.error " -> " e))))

(defn ignore-err [f]
  (try
    (f)
    (catch js/Error e "nothing")))

(defn target->value [e]
  (some-> e .-target .-value))

(defn index-of [v e]
  (.indexOf v e))

(def ^:private lazy-id (atom 0))

(defn- clear-lazy []
  (js/clearInterval @lazy-id))

(defn lazy-call
  ([cb] (lazy-call cb 400))
  ([cb idle]
   (clear-lazy)
   (reset! lazy-id
           (js/setInterval
            (fn []
              (clear-lazy)
              (cb)) idle))))

(defn params-to-url [params]
  (string/join "&" (map (fn [[k v]]
                          (str (name k) "=" (-> v
                                                (string/replace #"\s+" "+")
                                                js/encodeURIComponent
                                                (string/replace #"(%2B)+" "+"))))
                        params)))

(defn set-url! [url params]
  (js/history.replaceState nil nil (str "/#/" url "?" (params-to-url params))))

(def history-last (atom nil))

(defn set-history-url! [url params]
  (when-not (= @history-last [url params])
    (reset! history-last [url params])
    (js/history.pushState nil nil (str "/#/" url "?" (params-to-url params)))))

(defn body []
  js/document.body)

(defn document []
  js/document.documentElement)

(defn selector [s]
  (-> s js/document.querySelector))

(defn get-computed-style-property [el prop]
  (-> el
      js/window.getComputedStyle
      (.getPropertyValue (name prop))))

(defn id->elem [id]
  (-> id js/document.getElementById))

(defn add-listener [elem event-name cb]
  (-> elem (.addEventListener event-name cb)))

(defn remove-listener [elem event-name cb]
  (-> elem (.removeEventListener event-name cb)))

(defn add-document-listener [event-name cb]
  (-> (body) (.addEventListener event-name cb false)))

(defn remove-document-listener [event-name cb]
  (-> (body) (.removeEventListener event-name cb false)))

(defn add-class [elem class]
  (let [classes (.-classList elem)]
    (-> classes (.add class))))

(defn remove-class [elem class]
  (let [classes (.-classList elem)]
    (-> classes (.remove class))))

(defn- check-navigator [rx]
  (when-let [user-agent (some-> js/window .-navigator .-userAgent)]
    (some->> user-agent string/lower-case (re-find rx) nil? not)))

(def ios? (check-navigator #"ipad|iphone"))
(def macos? (check-navigator #"mac os"))
(def android? (check-navigator #"android"))
(def windows-phone? (check-navigator #"windows phone"))
(def chrome? (check-navigator #"chrome"))
(def safari? (and (check-navigator #"safari") (not chrome?)))

(def mobile? (or ios? android? windows-phone?))

(defn capitalize-first-letter [s]
  (str (-> s first string/capitalize)
       (->> s rest (apply str))))

(defn create-prefixes
  [param]
  [(str param)
   (str "moz" param)
   (str "moz" (capitalize-first-letter param))
   (str "webkit" param)
   (str "webkit" (capitalize-first-letter param))
   (str "ms" param)
   (str "ms" (capitalize-first-letter param))])

(defn prefixed-param
  [el param]
  (loop [[v & t] (create-prefixes param)]
    (when v
      (let [res (aget el v)]
        (if-not (nil? res)
          res
          (recur t))))))

(defn add-prefixed-listener [el event-name pred]
  (doseq [x (create-prefixes event-name)]
    (-> el (.addEventListener x pred))))

(defn remove-prefixed-listener [el event-name pred]
  (doseq [x (create-prefixes event-name)]
    (-> el (.removeEventListener x pred))))

(defn fullscreen? []
  (-> (prefixed-param js/document "fullscreenElement") nil? not))

(defn fullscreen-enabled? []
  (prefixed-param js/document "fullscreenEnabled"))

(defn fullscreen!
  ([] (fullscreen! (document)))
  ([elem]
   (when-let [func (prefixed-param elem "requestFullscreen")]
     (-> func (.call elem)))))

(defn exit-fullscreen!
  ([] (fullscreen! (document)))
  ([elem]
   (when-let [func (prefixed-param elem "exitFullscreen")]
     (-> func (.call elem)))))

(defn toggle-fullscreen!
  ([] (if (fullscreen?)
        (exit-fullscreen!)
        (fullscreen!)))
  ([elem]
   (if (fullscreen?)
     (exit-fullscreen! elem)
     (fullscreen! elem))))

(defn nil-or-blank? [v]
  (or (nil? v)
      (string/blank? v)))

(defn or-str [& values]
  (loop [[v & t] values]
    (println v t)
    (if-not (nil-or-blank? v)
      v
      (when-not (empty? t)
          (recur t)))))

(defn on-scroll-end [e cb]
  (let [sh (-> e .-target .-scrollHeight)
        st (-> e .-target .-scrollTop)
        oh (-> e .-target .-offsetHeight)
        th 50]
    #_(println "on-scroll")
    (when (= st 0)
      #_(println "start"))
    (when (>= (+ oh st) sh)
      #_(println "end"))
    (when (>= (+ oh st th) sh)
      #_(println "load new")
      (cb))))

(defn format
  "Formats a string using goog.string.format.
   e.g: (format \"Cost: %.2f\" 10.0234)"
  [fmt & args]
  (apply gstring/format (concat [fmt] args)))


(defn encode-url [s]
  (js/encodeURIComponent s))

(defn make-phrase-url [search-text]
  (str "https://www.playphrase.me/search/"
       (some-> search-text
               nlp/remove-punctuation
               string/trim string/lower-case (string/replace #" +" "_") encode-url)
       "/"))

(comment

  (make-phrase-url "hello world")

  )
