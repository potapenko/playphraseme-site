(ns playphraseme.api.queries.movies
  (:require [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.db.phrases-db :refer :all]))

(def coll "movies")

(defn- migrate []
  (mc/ensure-index db coll {:imdb 1 :title 1})
  (mc/ensure-index db coll {:title 1 :serie 1 :imdb 1 :year 1}))

(mount/defstate migrations-movies
  :start
  (migrate))

(defn get-movie-by-id [^String movie-id]
  (stringify-id
   (get-doc-by-id coll (str->id movie-id))))

(defn count-all []
  (count-docs coll {}))

(defn count-movie [pred]
  (count-docs coll pred))

(defn find-movies
  ([pred] (find-movies pred 0 10))
  ([pred limit] (find-movies pred 0 limit))
  ([pred skip limit]
   (stringify-id
    (find-docs coll {:pred pred :skip skip :limit limit}))))

(defn find-one-movie [pred]
  (first
   (find-movies pred 0 1)))


(defn get-movie-by-imdb [imdb]
  (find-one-movie {:imdb imdb}))

(defn get-movie-serie [movie]
  (when-let [serie (:serie movie)]
    (find-one-movie {:imdb (:serie-imdb movie)})))
