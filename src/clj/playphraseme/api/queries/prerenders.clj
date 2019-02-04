(ns playphraseme.api.queries.prerenders
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.prerenders-db :refer :all]
            [clojure.string :as string]))

(def coll "prerenders")

(defn migrate []
  (mc/ensure-index db coll {:search-strings 1 :have-video 1 :random 1})

  )

(mount/defstate migrations-prerenders
  :start (migrate))

(defn- trim [text]
  (some-> text
          string/trim
          string/lower-case))

(defn get-prerender-by-id [^String prerender-id]
  (stringify-id
   (get-doc-by-id coll (str->id prerender-id))))

(defn find-prerenders [params]
  (stringify-id
   (find-docs coll params)))

(defn find-one-prerender [pred]
  (stringify-id
   (find-doc coll pred)))

(defn insert-prerender! [text html]
  (stringify-id
   (add-doc coll {:text (trim text) :html html})))

(defn delete-prerender-by-text!
  [^String text]
  (delete-docs coll {:text (trim text)}))

(defn get-prerender-by-text [text]
  (:html (find-one-prerender {:text (trim text)})))
