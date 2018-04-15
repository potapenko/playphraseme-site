(ns playfavoriteme.api.queries.favorites
  (:require [monger.core :as mg]
            [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.db.core :refer :all]))

  (def coll "favoritePhrases")

(defn get-favorite-by-id
  [^String favorite-id]
  (stringify-id
   (get-doc-by-id coll (str->id favorite-id))))

(defn insert-favorite!
  [{:keys [email name password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-favorite!
  [^String favorite-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id favorite-id) user-data))

(defn delete-favorite!
  "Delete a single user matching provided id"
  [^String favorite-id]
  (delete-doc-by-id coll (str->id favorite-id)))

(defn get-favorites-count []
  (+ (count-docs coll {:state 1})
     #_(count-docs coll {:state 0})))




