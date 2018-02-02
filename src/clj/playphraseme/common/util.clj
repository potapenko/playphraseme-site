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

