(ns playphraseme.api.queries.phrases
  (:require [monger.core :as mg]
            [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.app.config :refer [env]]
            [playphraseme.db.core :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]))

(def coll "phrases")

(defn get-phrase-by-id
  [^String phrase-id]
  (stringify-id
   (get-doc-by-id coll (str->id phrase-id))))

(defn insert-phrase!
  [{:keys [email username password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-phrase!
  [^String phrase-id {:keys [email username password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id phrase-id) user-data))

(defn delete-phrase!
  "Delete a single user matching provided id"
  [^String phrase-id]
  (delete-doc-by-id coll (str->id phrase-id)))

