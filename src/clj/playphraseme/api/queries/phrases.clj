(ns playphraseme.api.queries.phrases
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "phrases")

(defn migrate []
  (mc/ensure-index db coll {:search-strings 1 :random 1}))

(mount/defstate migrations-phrases
  :start (migrate))

(defn get-phrase-by-id [^String phrase-id]
  (stringify-id
   (get-doc-by-id coll (str->id phrase-id))))

(defn find-phrases
  ([pred] (find-phrases pred 0 10))
  ([pred limit] (find-phrases pred 0 limit))
  ([pred skip limit]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit :sort {:random -1}}))))

(def get-phrases-count
  (memoize
   (fn []
     (count-docs coll {}))))

(def get-movies-count
  (memoize
   (fn []
     (->> (aggregate-docs
           coll [{$match {}}
                 {$group {:_id   {:movie "$movie"}}}
                 {"$count" "count"}])
          first
          :count))))
