(ns playphraseme.api.general-functions.doc-id
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.walk :as walk])
  (:import org.bson.types.ObjectId))

(defn id->str [object-id]
  (some-> object-id .toString))

(defn id->time [object-id]
  (some-> object-id .getTime c/from-long))

(defn str->id [s]
  (println ">>>>" s)
  (ObjectId. s))

(defn stringify-id [obj]
  (walk/postwalk (fn [x]
                   (cond
                     (= x :_id) :id
                     (instance? ObjectId x) (id->str x)
                     :else x))
                 obj))

(defn objectify-id [obj]
  (some-> obj
          (dissoc :id)
          (assoc :_id (str->id (:id obj)))))

(comment

  (-> (ObjectId.) id->time)

  (stringify-id {:_id      (ObjectId.)
                :name "John"
                :smith    "Smith"})

  (objectify-id {:id       (id->str (ObjectId.))
                 :name "John"
                 :smith    "Smith"}))
