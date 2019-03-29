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

(defn find-playlists-by-device-id [device-id]
  (stringify-id
   (find-docs coll {:pred {:device-id device-id}})))

(defn find-one-playlist [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-playlist! [data]
  (stringify-id
   (add-doc coll data)))

(defn update-playlist!
  ([data] (update-playlist! (:id data) (dissoc data :id)))
  ([^String playlist-id data]
   (update-doc-by-id coll (str->id playlist-id) data)))

(defn delete-playlist!
  [^String playlist-id]
  (delete-doc-by-id coll (str->id playlist-id)))


