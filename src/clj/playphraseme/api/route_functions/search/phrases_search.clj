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
            [clojure.walk :as walk]
            [monger.operators :refer :all]
            [clojure.java.io :as io]
            [ring.util.io :as ring-io]
            [ring.util.response :refer [response]]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [clojure.tools.logging :as log]))

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

(defn drop-last-word [s]
  (-> s
      (string/split #" +")
      drop-last
      (->> (string/join " "))))

(defn search-next-word-search-string
  ([text] (search-next-word-search-string text false))
  ([text word-end?]
   (let [text-pred (if word-end? (drop-last-word text) text)
         rx        (str "^" text (if-not word-end? " " "") "\\S+$")
         strings   (->>
                    (search-strings/find-search-strings
                     {:searchPred text-pred
                      :text       {$regex rx}} 10)
                    (map #(select-keys % [:text :validCount])))]
     (loop [[v & t] strings
            result  []]
       (if v
         (recur
          (remove #(-> % :text (= (:text v))) t)
          (conj result {:text (:text v)
                        :count (:validCount v)}))
         result)))))

(defn update-phrases-suggestions [{:keys [count suggestions] :as search-result} text]
  (if (and (= count 0)
           (empty? suggestions))
    (assoc search-result
           :suggestions (search-next-word-search-string text true)
           :next-world-complete nil)
    (assoc
     search-result
     :next-world-complete (-> text search-next-word-search-string first :text))))

(defn search-response [q skip limit]
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (ok (some-> res
                :body (parse-string true)
                (update :phrases get-phrases)
                #_(update-phrases-suggestions q)))))

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

(defn fix-search-string [id]
  (let [doc (search-strings/get-search-string-by-id id)]
    (let [new-text (-> doc
                       :text
                       (string/replace "\"" "")
                       (string/trim))]
     (-> doc
         (assoc :text new-text)
         (assoc :searchPred (drop-last-word new-text))
         search-strings/update-search-string!))))

(defn fix-all-search-strings []
  (log/info "count search strings without searchPred:" (search-strings/count-search-string {:searchPred nil}))
  (let [part-size 1000]
    (loop [pos 0]
      (log/info "pos:" pos)
      (let [part (search-strings/find-search-strings {:searchPred nil} pos part-size)]
        (when-not (empty? part)
          (pmap (fn [{:keys [id]}]
                  (fix-search-string id)) part)
          (recur (+ pos part-size)))))
    (println "done")))

(future
  (fix-all-search-strings))

(comment

  (fix-search-string "55da1457c6384911f4a22bbe")
  (search-next-word-search-string "are you")

  (search-next-word-search-string "are you s" true)

  (future
    (fix-all-search-strings))
  (add-search-string-search-pred "55bf95c5d18e85856832e9d0")
  (fix-search-string "55bf95c5d18e85856832e9d0")
  (time (count-response "hello"))
  (time (search-response "hello" 0 10))
  (-> (search-response "hello" 0 1) :body)
  (get-phrase-data "543bd8c8d0430558da9bfeb1"))
