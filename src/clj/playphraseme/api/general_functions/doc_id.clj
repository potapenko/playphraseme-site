(ns playphraseme.api.general-functions.doc-id
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c])
  (:import org.bson.types.ObjectId))

(defn id->str [object-id]
  (some-> object-id .toString))

(defn id->time [object-id]
  (some-> object-id .getTime c/from-long))

(defn str->id [s]
  (ObjectId. s))

(defn stringify-id [obj]
  (some-> obj
          (dissoc :_id)
          (assoc :id (id->str (:_id obj)))))

(defn objectify-id [obj]
  (some-> obj
          (dissoc :id)
          (assoc :_id (str->id (:id obj)))))

(comment

  (-> (ObjectId.) id->time)

  (stringify-id {:_id      (ObjectId.)
                :username "John"
                :smith    "Smith"})

  (objectify-id {:id       (id->str (ObjectId.))
                 :username "John"
                 :smith    "Smith"}))
