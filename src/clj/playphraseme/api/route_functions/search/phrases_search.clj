(ns playphraseme.api.route-functions.search.phrases-search
  (:require [ring.util.http-response :refer :all]
            [clj-http.client :as http]
            [clojure.core.async :refer [<! >! chan close! go go-loop put! timeout]]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :refer :all]
            [playphraseme.common.util :as util]
            [playphraseme.api.queries.phrases :as db]
            [clojure.string :as string]
            [clojure.walk :as walk]))

(defn use-shifts [p]
  (-> p
      (update :start + (-> p :shifts :left))
      (update :end + (-> p :shifts :right))
      (dissoc :shifts)))

(defn get-phrase-data [id]
  (-> (db/get-phrase-by-id id)
      (util/remove-keys [:random :haveVideo])
      (util/remove-keys :words [:id])
      use-shifts))

(defn- get-phrases [phrases-ids]
  (pmap get-phrase-data phrases-ids))

(defn search-response [q skip limit]
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (ok (some-> res :body (parse-string true) (update :phrases get-phrases)))))

(defn count-response [q]
  (let [url (str (:indexer-url env) "/count")
        res (http/get url {:query-params {:q q} :accept :json})]
    (ok (some-> res :body (parse-string true) :count))))

(comment
  (time (count-response "hello"))
  (time (search-response "hello" 0 10))
  (-> (search-response "hello" 0 1) :body :phrases first (select-keys [:start :end :shifts]))


  )
