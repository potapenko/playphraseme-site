(ns playphraseme.api.route-functions.search.phrases-search
  (:require [ring.util.http-response :refer :all]
            [clj-http.client :as http]
            [clojure.core.async :refer [<! >! chan close! go go-loop put! timeout]]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :refer :all]
            [playphraseme.api.queries.phrases :as db]
            [clojure.string :as string]
            [clojure.walk :as walk]))

(defn- remove-keys [obj keys]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (apply dissoc (concat [x] keys))
                     x))
                 obj))

(defn- remove-words-ids [result]
  (walk/postwalk (fn [x]
                   (if (and (map? x) (:words x))
                     (update x :words remove-keys [:id])
                     x)) result))

(defn get-phrase-data [id]
  (-> (db/get-phrase-by-id id)
      (remove-keys [:random :haveVideo])
      remove-words-ids))

(defn- get-phrases [phrases-ids]
  (pmap get-phrase-data phrases-ids))

(defn search-response [q skip limit]
  (let [url (str (:indexer-url env) "/search")
        res (http/get url {:query-params {:q q :skip skip :limit limit} :accept :json})]
    (some-> res :body (parse-string true) (update :phrases get-phrases))))

(comment

  (time (search-response "hello" 0 10))


  )
