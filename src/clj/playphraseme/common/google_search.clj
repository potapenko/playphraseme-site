(ns playphraseme.common.google-search
  (:require [clojure.string :as string]
            [playphraseme.common.suggestions :as suggestions]
            [playphraseme.api.queries.search-strings :as search-strings]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.api.queries.phrases :as phrases]
            [playphraseme.common.util :as util]))

(def default-title "PlayPhrase.me: Endless stream of movie clips of specific phrases")
(def default-description "Look for phrases in movies and watch videos with them.")
(def default-search-text "hello")

;; count of found video
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
  (if-not search-text
    (str
     (format "<div style=\"color:white;\">%s</div>" default-description)
     (let [t "hello"]
       (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url t) t)))
    (do
     (str
      (format "<h1 style=\"color:white;\">%s</h1>" (string/capitalize search-text))
      (->> (search-phrases search-text)
           (map string/capitalize)
           (map (fn [x] (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url x) x)))
           (string/join "\n"))
      "\n"
      (let [t (nlp/remove-first-word search-text)]
        (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url t) t))
      "\n"
      (let [t (nlp/remove-last-word search-text)]
        (format "<a href=\"%s\" style=\"color:white;\">%s</a>" (make-phrase-url t) t))))))

