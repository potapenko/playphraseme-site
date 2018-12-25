(ns playphraseme.api.queries.common-phrases
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.api.queries.phrases :as phrases]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "common-phrases")

(defn- migrate [])

(mount/defstate migrations-common-phrases
  :start (migrate))

(defn get-common-phrase-by-id [^String common-phrase-id]
  (stringify-id
   (get-doc-by-id coll (str->id common-phrase-id))))

(defn find-common-phrases [params]
  (stringify-id
   (find-docs coll params)))

(defn find-one-common-phrase [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-common-phrase! [data]
  (stringify-id
   (add-doc coll data)))

(defn update-common-phrase!
  [^String common-phrase-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id common-phrase-id) user-data))

(defn delete-common-phrase!
  [^String common-phrase-id]
  (delete-doc-by-id coll (str->id common-phrase-id)))


