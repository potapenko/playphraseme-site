(ns playphraseme.api.queries.prerender
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.prerenders-db :refer :all]))

(def coll "prerenders")

(defn migrate []
  (mc/ensure-index db coll {:search-strings 1 :have-video 1 :random 1})
  (mc/ensure-index db coll {:random 1}))

(mount/defstate migrations-prerenders
  :start (migrate))

(defn get-prerender-by-id [^String prerender-id]
  (stringify-id
   (get-doc-by-id coll (str->id prerender-id))))

(defn find-prerenders
  ([pred] (find-prerenders pred 0 10))
  ([pred limit] (find-prerenders pred 0 limit))
  ([pred skip limit]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit :sort {:random -1}}))))

(def get-prerenders-count
  (memoize
   (fn [_]
     (count-docs coll {:have-video true}))))

(def get-movies-count
  (memoize
   (fn [_]
     (->> (aggregate-docs
           coll [{$match {:have-video true}}
                 {$group {:_id {:movie "$movie"}}}
                 {"$count" "count"}])
          first
          :count))))
