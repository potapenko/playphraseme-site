(ns playphraseme.common.prerender
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [etaoin.api :as etaoin]
            [mount.core :as mount]
            [playphraseme.api.queries.prerenders :as prerenders]
            [playphraseme.app.config :refer [env]]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.common.util :as util]))

(defn- make-phrase-url [search-text]
  (str "/search/"
       (some-> search-text
               nlp/remove-punctuation
               string/trim string/lower-case (string/replace #" +" "_") util/encode-url)
       "/"))

(defn- server-uri [search-text]
  (let [{:keys [prerender-host port]} env]
    (str prerender-host ":" port (make-phrase-url search-text))))

;; document.getElementById("app").innerHTML

(defn wait-ready [driver]
  (loop [counter 0]
    (if (< counter 20)
      (if (etaoin/js-execute driver "return playphraseme.core.ready_for_prerender_QMARK_()")
        true
        (do
          (Thread/sleep 300)
          (recur (inc counter))))
      (throw (new IllegalStateException "Chrome not responded")))))

(defn prerender [search-text]
  (when-not (prerenders/get-prerender-by-text search-text)
    (let [driver (etaoin/chrome)]
      (util/catch-and-log-err "Work with chrome browser"
                              (etaoin/go driver (server-uri search-text))
                              (wait-ready driver)
                              (let [html-text (etaoin/js-execute driver "return document.getElementById('app').innerHTML;")]
                                (prerenders/insert-prerender! search-text html-text)
                                (println ">>>" html-text)))
      (etaoin/quit driver))))

(comment

  (prerender "hello")


  )

