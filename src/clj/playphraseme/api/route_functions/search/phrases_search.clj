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
            [clojure.java.io :as io]
            [ring.util.io :as ring-io]
            [ring.util.response :refer [response]]))

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

(defn search-response [q skip limit]
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (ok (some-> res :body (parse-string true) (update :phrases get-phrases)))))

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
    (-> doc
        (update :text (fn [t]
                        (-> t
                            (string/replace "\"" "")
                            (string/trim))))
        (assoc :needRecalculate true)
        search-strings/update-search-string!)))

(defn fix-all-search-strings []
  (let [part-size 1000]
    (loop []
      (let [part (search-strings/find-search-strings {:needRecalculate false} part-size 0)]
        (when-not (empty? part)
          (doseq [{:keys [id]} part]
            (fix-search-string id))
          (recur))))))

(comment

  (fix-all-search-strings)




  (fix-search-string "55bf95c5d18e85856832e9d0")




  (time (count-response "hello"))
  (time (search-response "hello" 0 10))
  (-> (search-response "hello" 0 1) :body)

  (get-phrase-data "543bd8c8d0430558da9bfeb1")




  )
