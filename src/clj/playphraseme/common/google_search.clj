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
            [clj-time.format :as f]))

(def default-title "PlayPhrase.me: Improve your pronunciation. Endless stream of movie clips of specific phrases")
(def default-description "Improve your pronunciation. Look for phrases in movies and watch videos with them.")
(def default-search-text "hello")
(def sitemap-f "./resources/public/sitemap.xml")

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
  (if-not search-text
    default-description
    (->> (search-phrases search-text)
         (map util/format-phrase-text)
         (reduce (fn [x val]
                   (let [new-val (if (string/blank? x)
                                   val
                                   (str val ", " x))]
                     (if (-> new-val count (< 500))
                       new-val
                       val))) ""))))

(defn- make-phrase-url [search-text]
  (str "/?q=" (-> search-text nlp/remove-punctuation string/trim string/lower-case util/encode-url)))

(defn generate-page-static-content [search-text]
  (let [search-text (if (string/blank? search-text) "hello" search-text)]
    (str
     (format "<h1 style=\"color:white;\">%s</h1>" (string/capitalize search-text))
     (format "<div style=\"color:white;\"/>%s</div>" (generate-page-description search-text))
     (->> (search-phrases search-text)
          (map string/capitalize)
          (map (fn [x] (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url x) x)))
          (string/join "\n"))
     "\n"
     (let [t (nlp/remove-first-word search-text)]
       (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url t) t))
     "\n"
     (let [t (nlp/remove-last-word search-text)]
       (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url t) t)))))

(defn- timestamp []
  (->> (t/now) (f/unparse (:year-month-day f/formatters))))

(defn- sitemap-entry [url priority]
  (format
        "<url>
  <loc>
      %s
  </loc>
  <lastmod>%s</lastmod>
  <changefreq>monthly</changefreq>
  <priority>%s</priority>
</url>
" url (timestamp) priority))

(defn- save-sitemap-part
  ([pos part]
   (println ">>" pos)
   (save-sitemap-part
    (common-phrases/get-all-common-phrases pos part)))
  ([phrases]
   (->> phrases
        (map (fn [{:keys [text]}]
               (spit
                sitemap-f
                (sitemap-entry (util/make-phrase-url text) 0.8)
                :append true)))
        (string/join "")
        doall)))

(defn generate-sitemap []
  (let [header "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">"
        footer "</urlset>"]
    (spit sitemap-f header)
    (spit
     sitemap-f
     (sitemap-entry "https://www.playphrase.me/" 1.0)
     :append true)
    (dotimes [pos 49]
      (save-sitemap-part pos 1000))
    (save-sitemap-part
     (common-phrases/get-bad-common-phrases))
    (spit sitemap-f "\n" :append true)
    (spit sitemap-f footer :append true)))

(comment
  (generate-sitemap)



  )
