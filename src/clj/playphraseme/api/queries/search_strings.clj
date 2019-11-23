(ns playphraseme.api.queries.search-strings
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "search-strings")

(defn migrate []
  (mc/ensure-index db coll {:count 1})
  (mc/ensure-index db coll {:text 1})
  (mc/ensure-index db coll {:text 1 :count 1})
  (mc/ensure-index db coll {:search-next 1 :count 1})
  (mc/ensure-index db coll {:search-pred 1 :text 1 :count 1})
  (mc/ensure-index db coll {:words-count 1 :words-count-without-stops 1 :count 1 :text 1}))

(mount/defstate migrations-search-phrases
  :start
  (migrate))

(defn get-search-string-by-id [^String search-string-id]
  (stringify-id
   (get-doc-by-id coll (str->id search-string-id))))

(defn count-all []
  (count-docs coll {}))

(defn count-search-strings [pred]
  (count-docs coll pred))

(defn find-search-strings
  ([pred] (find-search-strings pred 0 10))
  ([pred limit] (find-search-strings pred 0 limit))
  ([pred skip limit] (find-search-strings pred skip limit {:count -1}))
  ([pred skip limit sort]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit :sort sort}))))
