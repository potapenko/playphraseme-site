(ns playphraseme.api.queries.common-phrases
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.api.queries.phrases :as phrases]
            [mount.core :as mount]
            [monger.collection :as mc]
            [playphraseme.db.phrases-db :refer :all]
            [playphraseme.common.util :as util]
            [clojure.string :as string]))

(def coll "common-phrases")

(defn- migrate []
  (mc/ensure-index db coll {:index 1}))

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
  ([data] (update-common-phrase! (:id data) (dissoc data :id)))
  ([^String common-phrase-id data]
   (update-doc-by-id coll (str->id common-phrase-id) data)))

(defn delete-common-phrase!
  [^String common-phrase-id]
  (delete-doc-by-id coll (str->id common-phrase-id)))

(defn count-common-phrases [pred]
  (count-docs coll pred))

(defn generate-sitemap []
  (let [header "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">"
        footer "</urlset>"
        url-temlate "
  <url>
    <loc>
      %s
    </loc>
    <lastmod>2019-01-20T12:47:38+00:00</lastmod>
  </url>"
        sitemap-f "./resources/public/sitemap.xml"]

    (spit sitemap-f header)
    (->> (find-common-phrases {:pred {} :limit 10000 :sort {:index 1}})
         (map (fn [{:keys [text]}]
                (spit sitemap-f (format url-temlate (util/make-phrase-url text)) :append true)))
         (string/join "")
         doall)
    (spit sitemap-f "\n" :append true)
    (spit sitemap-f footer :append true)))

(comment
  (generate-sitemap)



  )
