(ns playphraseme.api.queries.prerender
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.prerenders-db :refer :all]))

(def coll "prerenders")

(mount/defstate migrations-prerenders
  :start (migrate))

(defn get-prerender-by-id [^String prerender-id]
  (stringify-id
   (get-doc-by-id coll (str->id prerender-id))))

(defn find-prerenders [params]
  (stringify-id
   (find-docs coll params)))

(defn find-one-prerender [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-prerender! [data]
  (stringify-id
   (add-doc coll data)))

(defn update-prerender!
  [^String prerender-id {:keys [email name password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id prerender-id) user-data))

(defn delete-prerender!
  [^String prerender-id]
  (delete-doc-by-id coll (str->id prerender-id)))

(defn get-prerender-by-text [text]
  (find-one-prerender {:text text}))
