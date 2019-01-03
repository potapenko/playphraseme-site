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
   coll
   {:key (name key)}
   {"$set" {:key (name key) :value value}}
   {:upsert true}))

(defn delete-config!
  [^String key]
  (delete-docs coll {:key key}))

(comment

  (update-config! :any "value2")
  (update-config! :any2 "value222")

  (get-config :any)

  (delete-config! :any)

  )

