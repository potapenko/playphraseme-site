(ns playphraseme.common.util
  (:require [clojure.string :as string]
            [clojure.walk :as walk]))

(defn- change-keys
  [data pred]
  (let [transform-map (fn [form]
                        (if (map? form)
                          (reduce-kv (fn [acc k v] (assoc acc (pred k) v)) {} form)
                          form))]
    (walk/postwalk transform-map data)))

(defn- remove-dots-from-keys
  [data]
  (change-keys data #(-> % name (string/replace "." "__") keyword)))

(defn- restore-dots-to-keys
  [data]
  (change-keys data #(-> % name (string/replace "__" ".") keyword)))

(defn remove-keys
  ([obj keys]
   (walk/postwalk (fn [x]
                    (if (map? x)
                      (apply dissoc (concat [x] keys))
                      x))
                  obj))
  ([obj scope keys]
   (walk/postwalk (fn [x]
                    (if (and (map? x) (contains? x scope))
                      (update x :words remove-keys [:id])
                      x)) obj)))

(defn update-dafault [m k v]
  (if-not (contains? m k)
    (assoc m k v)
    m))

(defmacro nil-when-throw [& body]
  `(try
    ~@body
    (catch Throwable e#
      nil)))
