(ns playphraseme.common.prerender
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [etaoin.api :as etaoin]
            [mount.core :as mount]
            [playphraseme.app.config :refer [env]]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.common.util :as util]))

(defonce driver
  (do
    (log/info "Starting prerendering driver")
    (etaoin/chrome)))

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

(defn wait-ready []
  (loop [counter 0]
    (when (> counter 20)
      (if (etaoin/js-execute driver "return playphraseme.core.ready_for_prerender_QMARK_()")
        true
        (do
          (Thread/sleep 300)
          (recur (inc counter)))))))

(defn prerender [search-text]
  (etaoin/go driver (server-uri search-text))
  (let [html-text (etaoin/js-execute driver "return document.getElementById('app').innerHTML;")]
    html-text

    ))

(comment

  (prerender "hello")


  )

