(ns playphraseme.api.queries.playlists
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.api.queries.phrases :as phrases]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.users-db :refer :all]))

(def coll "playlists")

(defn- migrate [])

(mount/defstate migrations-playlists
  :start (migrate))

(defn get-playlist-by-id [^String playlist-id]
  (stringify-id
   (get-doc-by-id coll (str->id playlist-id))))

(defn find-playlists [params]
  (stringify-id
   (find-docs coll params)))

(defn find-one-playlist [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-playlist! [data]
  (stringify-id
   (add-doc coll data)))

(defn update-playlist!
  [^String playlist-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id playlist-id) user-data))

(defn delete-playlist!
  [^String playlist-id]
  (delete-doc-by-id coll (str->id playlist-id)))


