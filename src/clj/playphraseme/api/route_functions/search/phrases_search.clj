(ns playphraseme.api.route-functions.search.phrases-search
  (:require [ring.util.http-response :refer :all]
            [clj-http.client :as http]
            [clojure.core.async :refer [<! >! chan close! go go-loop put! timeout]]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :refer :all]
            [playphraseme.common.util :as util]
            [playphraseme.api.queries.phrases :as db]
            [playphraseme.api.queries.search-strings :as search-strings]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [noir.response :as resp]
            [mount.core :as mount]
            [clojure.walk :as walk]
            [monger.operators :refer :all]
            [clojure.java.io :as io]
            [playphraseme.common.nlp :as nlp]
            [ring.util.io :as ring-io]
            [ring.util.response :refer [response]]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [clojure.tools.logging :as log]
            [playphraseme.api.queries.movies :as movies]
            [playphraseme.common.suggestions :refer [phrase-candidates]]
            [playphraseme.api.queries.phrases :as phrases]))

(defn drop-last-word [s]
  (-> s
      (string/split #" +")
      drop-last
      (->> (string/join " "))))

(defn- remove-punctuation [s]
  (-> s
      string/lower-case
      (string/replace #"[.,\/#!$%\^&\*;:{}=\-_`~()—]" "")
      (string/replace #"\s+" " ")
      string/lower-case
      string/trim))

(defn- get-video-file [id]
  (let [phrase (db/get-phrase-by-id id)]
    (str (:id phrase) ".mp4")))

(defn- get-video-url [id]
  (let [cdn-url (:cdn-url env)
        phrase (db/get-phrase-by-id id)]
    (str cdn-url (:movie phrase) "/" (:id phrase) ".mp4")))

(defn get-video-info [movie-id]
  (let [movie (movies/get-movie-by-id movie-id)
        serie (some-> movie :serie-imdb movies/get-movie-by-imdb)]
    {:info (if-not serie
             (:title movie)
             (string/join " / " [(:title serie) (:title movie) (:season movie) (:episode movie)]))
     :imdb (:imdb movie)}))

(defn prepare-phrase-data [phrase]
  (some-> phrase
          (util/remove-keys [:random :have-video :__v :state])
          (util/remove-keys :words [:id])
          (assoc :video-info (get-video-info (:movie phrase)))
          (assoc :video-url (get-video-url (:id phrase)))))

(defn get-phrase-data [id]
  (prepare-phrase-data (db/get-phrase-by-id id)))

(defn- get-phrases [phrases-ids]
  (pmap get-phrase-data phrases-ids))

(defn search-next-word-search-string
  ([text] (search-next-word-search-string text false))
  ([text word-end?]
   (let [text      (-> text string/trim string/lower-case)
         text-pred (if word-end? (drop-last-word text) text)
         rx        (str "^" text (if-not word-end? " " "") "\\S+$")
         strings   (->>
                    (search-strings/find-search-strings
                     {:search-pred text-pred
                      :text       {$regex rx}} 20)
                    (remove #(-> % :count (= 0)))
                    (map #(select-keys % [:text :count])))]
     (loop [[v & t] strings
            result  []]
       (if v
         (recur
          (remove #(-> % :text (= (:text v))) t)
          (conj result {:text  (:text v)
                        :count (:count v)}))
         result)))))

(defn- build-map-left [text]
  (->>
   (search-strings/find-search-strings {:search-next text :count {"$gt" 5}})
   (map #(select-keys % [:text :count]))
   (map #(assoc %
                :left (build-map-left (:text %))
                :right (build-map-right (:text %))))
   vec))

(declare build-map-right build-map-left)

(defn- build-map-right [text]
  (->>
   (search-strings/find-search-strings {:search-pred text :count {"$gt" 5}})
   (map #(select-keys % [:text :count]))
   (map #(assoc %
                :right (build-map-right (:text %))
                :left (build-map-left (:text %))))
   vec))

(defn create-phrases-map [text]
  {:text text
   :count (search-strings/count-search-strings {:text text})
   :left (build-map-left text)
   :right (build-map-right text)})

(defn flat-phrases-map [m]
  (->> m
   (clojure.walk/postwalk
    (fn [e]
      (if (and (map? e))
        [(select-keys e [:text :count]) (:left e) (:right e)]
        e)))
   flatten
   (remove nil?)
   distinct
   (remove #(-> % :count (< 5)))
   (sort-by #(-> % :text nlp/count-words))
   reverse
   (take 10)
   vec))

(defn update-phrases-suggestions [{:keys [count suggestions] :as search-result} text]
  (if (string/blank? text)
    search-result
    (cond
      (and (= count 0)
           (empty? suggestions)) (let [suggestions (search-next-word-search-string text true)]
                                   (assoc search-result
                                          :suggestions suggestions
                                          :next-word-suggestion (-> suggestions first :text)))
      (empty? suggestions)       (let [suggestions (search-next-word-search-string text)]
                                   (assoc search-result
                                          :suggestions suggestions
                                          :next-word-suggestion (-> suggestions first :text)))
      :else (assoc
             search-result
             :next-word-suggestion (-> text search-next-word-search-string first :text)))))

(defn search-response [q skip limit]
  (assert (< limit 100))
  (let [search-string (first
                       (search-strings/find-search-strings
                        {:text (nlp/remove-punctuation q)}))]
    (ok
     (update-phrases-suggestions
      (if-not search-string
        {:count 0 :phrases [] :suggestions []}
        (let [phrases (->> (phrases/find-phrases {:search-strings (:text search-string)
                                                  :have-video     true}
                                                 (if (> skip 1000) 1000 skip)
                                                 limit)
                           (map prepare-phrase-data))]
          (merge
           {:count (:count search-string) :phrases phrases}
           (when (empty? phrases)
             {:suggestions (phrase-candidates q)}))))
      q))))

(defn phrase-response [id]
  (ok (util/nil-when-throw
       (get-phrase-data id))))

(defn count-response [q]
  (let [url (str (:indexer-url env) "/count")
        res (http/get url {:query-params {:q q} :accept :json})]
    (ok (some-> res :body (parse-string true) :count))))

(defn all-phrases-count-response []
  (ok (db/get-phrases-count)))

(defn all-movies-count-response []
  (ok (db/get-movies-count)))

(defn video-url-response [id]
  (ok (get-video-url id)))

(defn video-response [id]
  (resp/redirect (get-video-url id)))

;; (defn video-stream-response [id]
;;   (response
;;    (ring-io/piped-input-stream
;;     (fn [out-stream]
;;       ))))

(defn video-download-response [id]
  (-> id
      get-video-url
      resp/redirect
      (assoc-in [:headers "Content-Disposition"]
                (format "attachment; filename=\"%s\"" (get-video-file id)))))


(comment

  (add-search-string-pred "55da1457c6384911f4a22bbe")
  (search-next-word-search-string "are you")
  (search-next-word-search-string "are you s" true)
  (add-search-string-search-pred "55bf95c5d18e85856832e9d0")
  (add-search-string-pred "55bf95c5d18e85856832e9d0")
  (time (count-response "hello"))
  (time (search-response "hello" 0 10))
  (-> (search-response "hello" 0 1) :body)
  (get-phrase-data "543bd8c8d0430558da9bfeb1"))
