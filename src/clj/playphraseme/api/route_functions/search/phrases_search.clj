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
            [ring.util.io :as ring-io]
            [ring.util.response :refer [response]]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [clojure.tools.logging :as log]))

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

(defn add-search-string-pred [id]
  (let [doc (search-strings/get-search-string-by-id id)]
    (let [new-text (-> doc
                       :text
                       (string/replace "\"" "")
                       (string/trim))]
      (-> doc
          (assoc :text new-text)
          (assoc :searchPred (drop-last-word new-text))
          search-strings/update-search-string!))))

(defn add-search-strings-pred-migration []
  (log/info "count search strings without searchPred:" (search-strings/count-search-string {:searchPred nil}))
  (let [part-size 1000]
    (loop [pos 0]
      (log/info "pos:" pos)
      (let [part (search-strings/find-search-strings {:searchPred nil} part-size)]
        (when-not (empty? part)
          (pmap (fn [{:keys [id]}]
                  (add-search-string-pred id)) part)
          (recur (+ pos part-size)))))
    (log/info "done")))


(defn remove-search-string-punctuation [id]
  (let [doc (search-strings/get-search-string-by-id id)]
    (-> doc
        (assoc :text (remove-punctuation (:text doc)))
        (assoc :searchPred (remove-punctuation (:searchPred doc)))
        search-strings/update-search-string!)))

(defn remove-string-search-punctuation-migration []
  (let [part-size 1000]
    (loop [pos 0]
      (log/info "fix punctuation pos:" pos)
      (let [part (search-strings/find-search-strings {:text       {$regex "[.,\\/#!$%\\^&\\*;:{}=\\-_`~()—]"}
                                                      :searchPred {$ne nil}})]
        (when-not (empty? part)
          (pmap (fn [{:keys [id]}]
                  (remove-search-string-punctuation id)) part)
          (recur (+ pos part-size)))))
    (log/info "done")))

(mount/defstate search-phrases-fixes
  :start (future
           (remove-string-search-punctuation-migration)
           (add-search-strings-pred-migration)))

(defn- get-video-file [id]
  (let [phrase (db/get-phrase-by-id id)]
    (str (:id phrase) ".mp4")))

(defn- get-video-url [id]
  (let [cdn-url "https://cdn.playphrase.me/phrases/"
        phrase (db/get-phrase-by-id id)]
    (str cdn-url (:movie phrase) "/" (:id phrase) ".mp4")))

(defn use-shifts [p]
  (-> p
      (update :start + (or (some-> p :shifts :left) 0))
      (update :end + (or (some-> p :shifts :right) 0))
      (dissoc :shifts)))

(defn get-phrase-data [id]
  (some-> (db/get-phrase-by-id id)
          (util/remove-keys [:random :haveVideo :__v :state])
          (util/remove-keys :words [:id])
          (assoc :video-url (get-video-url id))
          use-shifts))

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
                     {:searchPred text-pred
                      :text       {$regex rx}} 20)
                    (remove #(-> % :validCount (= 0)))
                    (map #(select-keys % [:text :validCount])))]
     (loop [[v & t] strings
            result  []]
       (if v
         (recur
          (remove #(-> % :text (= (:text v))) t)
          (conj result {:text  (:text v)
                        :count (:validCount v)}))
         result)))))

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
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (ok (some-> res
                :body (parse-string true)
                (update :phrases get-phrases)
                (update-phrases-suggestions q)))))

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
