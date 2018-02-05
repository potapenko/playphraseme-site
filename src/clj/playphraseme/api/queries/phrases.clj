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
  [^String user-id]
  (stringify-id
   (get-doc-by-id coll (str->id user-id))))

(defn get-phrase-by-refresh-token
  [refresh-token]
  (stringify-id
   (get-doc coll {:refresh-token refresh-token})))

(defn get-phrase-by-username
  [username]
  (stringify-id
   (get-doc coll {:username username})))

(defn get-phrase-by-email
  [email]
  (stringify-id
   (get-doc coll {:email email})))

(defn insert-phrase!
  [{:keys [email username password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-phrase!
  [^String user-id {:keys [email username password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id user-id) user-data))

(defn update-phrase-password!
  [^String user-id password]
  (update-doc-by-id coll (str->id user-id) {:password password}))

(defn delete-phrase!
  "Delete a single user matching provided id"
  [^String user-id]
  (delete-doc-by-id coll (str->id user-id)))

