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

(defn get-config-by-id [^String config-id]
  (stringify-id
   (get-doc-by-id coll (str->id config-id))))

(defn find-configs [params]
  (stringify-id
   (find-docs coll params)))

(defn find-one-config [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-config! [data]
  (stringify-id
   (add-doc coll data)))

(defn update-config!
  [^String config-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id config-id) user-data))

(defn delete-config!
  [^String config-id]
  (delete-doc-by-id coll (str->id config-id)))


