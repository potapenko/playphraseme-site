(ns playphraseme.api.route-functions.search.phrases-search
  (:require [ring.util.http-response :refer :all]
            [clj-http.client :as http]
            [clojure.core.async :refer [<! >! chan close! go go-loop put! timeout]]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :refer :all]
            [playphraseme.api.queries.phrases :as db]
            [clojure.string :as string]))

(defn get-phrase-data [id]
  (db/get-phrase-by-id id))

(defn get-phrases [phrases-ids]
  (map get-phrase-data phrases-ids))

(defn search-response [q skip limit]
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (some-> res :body (parse-string true) (update :phrases get-phrases))))

(comment

  (search-response "hello" 0 10)


  (get-phrase-data "549add9136a7787a04182da8")

  )
