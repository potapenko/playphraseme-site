(ns playphraseme.common.prerender
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [etaoin.api :as etaoin]
            [mount.core :as mount]
            [playphraseme.api.queries.prerenders :as prerenders]
            [playphraseme.app.config :refer [env]]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.common.util :as util]
            [playphraseme.api.queries.search-strings :as search-strings]
            [playphraseme.api.queries.phrases :as phrases]))

(defn- make-phrase-url [search-text]
  (str "/search/"
       (some-> search-text
               nlp/remove-punctuation
               string/trim string/lower-case (string/replace #" +" "_") util/encode-url)
       "/"))

(defn- server-uri [search-text]
  (let [{:keys [prerender-host port]} env]
    (str prerender-host ":" port (make-phrase-url search-text))))

(defn wait-ready [driver]
  (loop [counter 0]
    (Thread/sleep 500)
    (if (< counter 20)
      (if (etaoin/js-execute driver "return playphraseme.core.ready_for_prerender_QMARK_()")
        true
        (do
          (recur (inc counter))))
      (throw (new IllegalStateException "Chrome not responded")))))

(defn prerender [driver search-text]
  (when-not (prerenders/get-prerender-by-text search-text)
    (util/catch-and-log-err "Work with chrome browser"
                            (etaoin/go driver (server-uri search-text))
                            (wait-ready driver)
                            (let [html-text (-> (etaoin/js-execute driver "return document.getElementById('app').innerHTML;")
                                                (string/replace #"<!--.+?-->" ""))]
                              (prerenders/insert-prerender! search-text html-text)
                              (println search-text)))))

(defn generate-prerenders []
  (let [driver (etaoin/chrome {:headless true})]
   (->>
    (concat
     (search-strings/find-search-strings
      {:count {"$gte" 5} :words-count {"$gt" 1 "$lte" 5}} 0 0
      {:words-count -1 :words-count-without-stops -1 :count -1}))
    (take 50000)
    (shuffle)
    (map :text)
    (map #(prerender driver %))
    doall)
   (etaoin/quit driver)))

(comment

  (generate-prerenders)




  )

