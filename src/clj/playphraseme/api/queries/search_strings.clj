(ns playphraseme.api.queries.search-strings
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "searchString")

(defn migrate []
  (mc/ensure-index db coll {:validCount 1})
  (mc/ensure-index db coll {:text 1})
  (mc/ensure-index db coll {:needRecalculate 1})
  (mc/ensure-index db coll {:text 1 :validCount 1})
  (mc/ensure-index db coll {:searchPred 1 :text 1 :validCount 1}))

(mount/defstate migrations-search-phrases
  :start
  (migrate))

(defn get-search-string-by-id
  [^String search-string-id]
  (stringify-id
   (get-doc-by-id coll (str->id search-string-id))))

(defn insert-search-string!
  [{:keys [email name password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-search-string!
  ([data] (update-search-string! (:id data) (dissoc data :id)))
  ([^String search-string-id data]
   (update-doc-by-id coll (str->id search-string-id) data)))

(defn delete-search-string!
  [^String search-string-id]
  (delete-doc-by-id coll (str->id search-string-id)))

(defn count-all []
  (count-docs coll {}))

(defn count-search-string [pred]
  (count-docs coll pred))

(defn find-search-strings
  ([pred] (find-search-strings pred 0 10))
  ([pred limit] (find-search-strings pred 0 limit))
  ([pred skip limit]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit :sort {:validCount -1}}))))
