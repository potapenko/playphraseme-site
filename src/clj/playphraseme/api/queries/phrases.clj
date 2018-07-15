(ns playphraseme.api.queries.phrases
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "phrases")

(defn migrate []
  (mc/ensure-index db coll {:search-strings 1 :random 1}))

(mount/defstate migrations-phrases
  :start (migrate))

(defn get-phrase-by-id
  [^String phrase-id]
  (stringify-id
   (get-doc-by-id coll (str->id phrase-id))))

(defn find-phrases
  ([pred] (find-phrases pred 0 10))
  ([pred limit] (find-phrases pred 0 limit))
  ([pred skip limit]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit :sort {:random -1}}))))

(defn get-phrases-count []
  (count-docs coll {}))

(defn get-movies-count []
  (count-docs "movies" {}))
