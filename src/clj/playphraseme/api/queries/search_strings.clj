(ns playphraseme.api.queries.search-strings
  (:require [monger.core :as mg]
            [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.app.config :refer [env]]
            [playphraseme.db.phrases-db :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]))

(def coll "searchStrings")

(defn get-search-string-by-id
  [^String search-string-id]
  (stringify-id
   (get-doc-by-id coll (str->id search-string-id))))

(defn insert-search-string!
  [{:keys [email name password refresh-token] :as user-data}]
  (stringify-id
   (add-doc coll user-data)))

(defn update-search-string!
  [^String search-string-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id search-string-id) user-data))

(defn delete-search-string!
  "Delete a single user matching provided id"
  [^String search-string-id]
  (delete-doc-by-id coll (str->id search-string-id)))

