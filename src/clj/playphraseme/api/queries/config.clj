(ns playphraseme.api.queries.config
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.api.queries.phrases :as phrases]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.users-db :refer :all]))

(def coll "configs")

(defn- migrate [])

(mount/defstate migrations-configs
  :start (migrate))

(defn get-config [^String key]
  (:value (find-doc coll {:key key})))

(defn update-config! [^String key value]
  (update-doc
   {:key key}
   {"$set" {:value value}}
   {:upsert true}))

(defn delete-config!
  [^String config-id]
  (delete-doc-by-id coll (str->id config-id)))


