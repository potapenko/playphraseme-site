(ns playphraseme.common.localstorage
  (:require [re-frame.core :as rf]
            [playphraseme.common.util :as util]
            [cognitect.transit :as transit]))

(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) (name key) val))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (.getItem (.-localStorage js/window) (name key)))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) (name key)))

(defn load-model []
  (or (transit/read (transit/reader :json)
                    (get-item :model-storage))
      {}))

(def model-store-md
  (rf/->interceptor
   :id :model-store
   :after (fn [context]
            (let [stored  (load-model)
                  before  (-> context :coeffects :db)
                  after   (-> context :effects :db)
                  [_ d _] (clojure.data/diff before after)]
              (set-item! ::model-storage
                         (transit/write (transit/writer :json)
                                        (merge stored d))))
            context)))



