(ns playfavoriteme.api.queries.favorites
  (:require [monger.core :as mg]
            [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.db.core :refer :all :as db]
            [playphraseme.api.queries.phrases :as phrases]))

  (def coll "favoritePhrases")

(defn get-favorite-by-id
  [^String favorite-id]
  (stringify-id
   (get-doc-by-id coll (str->id favorite-id))))

(defn insert-favorite!
  [^String phrase-id]
  (stringify-id
   (add-doc coll {})))

(defn update-favorite!
  [^String favorite-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id favorite-id) user-data))

(defn delete-favorite!
  "Delete a single user matching provided id"
  [^String favorite-id]
  (delete-doc-by-id coll (str->id favorite-id)))

(defn get-favorite-by-user
  [^String user-id skip limit]
  (stringify-id
   (db/find-docs coll {:pred  {:user (str->id user-id)}
                       :skip  skip
                       :limit limit})))

(defn get-favorites-count
  [^String user-id]
  (count-docs coll {:user (str->id user-id)}))




