(ns playphraseme.common.util
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [clojure.walk :as walk]
            [camel-snake-kebab.core :as keb :refer [->camelCase ->kebab-case ->kebab-case-keyword]]
            [cljs.pprint :refer [pprint]]
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
  (string/join "&" (map (fn [[k v]] (str (name k) "=" (js/encodeURIComponent v))) params)))

(defn set-url! [url params]
  (js/history.pushState nil nil (str "/#/" url "?" (params-to-url params)))
  #_(aset js/window.location "hash" (str "/" url "?" (params-to-url params))))

(defn body []
  js/document.body)

(defn selector [s]
  (-> s js/document.querySelector))

(defn selector->elem [s]
  (-> s selector first))

(defn id->elem [id]
  (-> id js/document.getElementById))

(defn add-listener [elem event-name cb]
  (-> elem (.addEventListener event-name cb)))

(defn remove-listener [elem event-name cb]
  (-> elem (.removeEventListener event-name cb)))

(defn add-document-listener [event-name cb]
  (-> (body) (.addEventListener event-name cb false)))

(defn remove-document-listener [event-name cb]
  (println (-> (body) (.removeEventListener event-name cb false))))

(defn add-class [elem class]
  (let [classes (.-classList elem)]
    (-> classes (.add class))))

(defn remove-class [elem class]
  (let [classes (.-classList elem)]
    (-> classes (.remove class))))

(defn ios-safari? []
  (println ">>>" (some-> js/window #_.-navigator #_.-userAgent))
  (when-let [user-agent (some-> js/window .-navigator .-userAgent)]
    (js/console.log "User agent:" user-agent)
    (some->> user-agent string/lower-case (re-find #"ipad|iphone") nil? not)))

(comment
  (ios-safari?)

  (class->str
   :name
   :hello
   nil
   :world
   "string-class")

  (flag-map {:a "true"})

  (flag-map :a :b :c {:d "hello"})

  (go-url! "/#/metrics"))

