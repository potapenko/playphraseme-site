(ns playphraseme.common.google-search
  (:require [clojure.string :as string]
            [playphraseme.common.suggestions :as suggestions]
            [playphraseme.api.queries.search-strings :as search-strings]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.api.queries.phrases :as phrases]
            [playphraseme.common.util :as util]
            [playphraseme.common.common-phrases :as common-phrases]
            [playphraseme.api.queries.common-phrases :as common-phrases-db]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [sitemap.core :as sitemap]
            [clojure.java.io :as io]
            [playphraseme.app.config :refer [env]]
            [etaoin.api :as etaoin]
            [mount.core :as mount]
            [clojure.tools.logging :as log]))

(def default-title "PlayPhrase.me: Largest collection of video quotes from movies on the web")
(def default-description "Improve your pronunciation: search phrases in movies and watch and listen videos with them.")
(def default-search-text "hello")
(def sitemap-f "./resources/public/sitemap.xml")

;; (mount/defstate driver
;;   :start
;;   (do
;;     (log/info "Starting prerendering driver")
;;     (etaoin/chrome))
;;   :stop
;;   (do
;;     (log/info "Stopping prerendering driver")
;;     (etaoin/quit driver)))

;; (defn- server-uri []
;;   (let [{:keys [prerender-host port]} env]
;;     (str prerender-host ":" port)))

;; (defn prerender []
;;   (etaoin/go driver (server-uri)))

(defn generate-page-title [search-text]
  (if-not search-text
    default-title
    (format "Playphrase.me: %s" (string/capitalize search-text))))

(defn search-phrases [q]
  (let [q (some-> q string/trim string/lower-case)
        search-string (first
                       (search-strings/find-search-strings
                        {:text (nlp/remove-punctuation q)}))]
    (if-not search-string
      []
      (->> (phrases/find-phrases {:search-strings (:text search-string)
                                  :have-video     true}
                                 0 100)
           (map :text)
           distinct))))

(defn generate-page-description [search-text]
  (str
   default-description
   " "
   (if-not search-text
     ""
     (->> (search-phrases search-text)
          (map util/format-phrase-text)
          (reduce (fn [x val]
                    (let [new-val (if (string/blank? x)
                                    val
                                    (str val ", " x))]
                      (if (-> new-val count (< 200))
                        new-val
                        val))) "")))))

(defn generate-rel-canonical [search-text]
  (if-not search-text
    "<link rel=\"canonical\" href=\"https://www.playphrase.me/\" />"
    (format "<link rel=\"canonical\" href=\"%s\" />" (util/make-phrase-url search-text))))

(defn- make-phrase-url [search-text]
  (str "/search/"
       (some-> search-text
               nlp/remove-punctuation
               string/trim string/lower-case (string/replace #" +" "_") util/encode-url)
       "/"))

(defn generate-page-static-content [search-text]
  (let [search-text (if (string/blank? search-text) "hello" search-text)]
    (str
     "<div class=\"static-content\">"
     (format "<h1 style=\"color:rgba(0,0,0,0.02);\">%s</h1>" (string/capitalize search-text))
     (format "<div style=\"color:rgba(0,0,0,0.02);\"/>%s</div>" (generate-page-description search-text))
     (->> (search-phrases search-text)
          (map string/capitalize)
          (map (fn [x] (format "<a href=\"%s\" style=\"color:rgba(0,0,0,0.02);\">%s</a>" (make-phrase-url x) x)))
          (string/join "\n"))
     "\n"
     (let [t (nlp/remove-first-word search-text)]
       (format "<a href=\"%s\" style=\"color:rgba(0,0,0,0.02);\">%s</a>" (make-phrase-url t) t))
     "\n"
     (let [t (nlp/remove-last-word search-text)]
       (format "<a href=\"%s\" style=\"color:rgba(0,0,0,0.02);\">%s</a>" (make-phrase-url t) t))
     "</div>")))

(defn- timestamp []
  (->> (t/now) (f/unparse (:year-month-day f/formatters))))

(defn generate-sitemap []
  (let [lastmod (timestamp)]
    (->>
     (concat
      ["https://www.playphrase.me/"]
      (search-strings/find-search-strings
       {:count {"$gte" 5} :words-count {"$gt" 1 "$lte" 5}} 0 0
       {:words-count -1 :words-count-without-stops -1 :count -1})
      (phrases/find-phrases {} 0 49000))
     (map :text)
     (map util/make-phrase-url)
     (take 50000)
     (map (fn [x]
            {:loc        x
             :lastmod    lastmod
             :changefreq "monthly"
             :priority   "1.0"}))
     sitemap/generate-sitemap
     (sitemap/save-sitemap
      (io/file "./resources/public/sitemap.xml")))))

(comment

  (generate-sitemap)



  )
