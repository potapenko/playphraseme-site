(ns playphraseme.api.queries.phrases
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "phrases")

(defn get-phrase-by-id
  [^String phrase-id]
  (stringify-id
   (get-doc-by-id coll (str->id phrase-id))))

(defn insert-phrase!
  [{:keys [email name password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-phrase!
  [^String phrase-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id phrase-id) user-data))

(defn delete-phrase!
  [^String phrase-id]
  (delete-doc-by-id coll (str->id phrase-id)))

(defn get-phrases-count []
  (+ (count-docs coll {:state 1})
     #_(count-docs coll {:state 0})))

(defn get-movies-count []
  322
  #_(count-docs "movie" {}))

