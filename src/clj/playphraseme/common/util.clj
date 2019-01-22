(ns playphraseme.common.util
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [playphraseme.common.nlp :as nlp])
  (:import [java.net URL URLEncoder]))

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

(defn resource-file [path]
  (-> path
      io/resource
      io/file))

(defn resource-path [path]
  (-> path io/resource .toURI .normalize .getPath))

(defn time-stamp-10-min []
  (-> (System/currentTimeMillis)
      (float)
      (/ 1000.0)
      (/ 1000.0)
      (/ 10.0)
      (int)))

(defn distinct-by
  ([key-fn coll] (distinct-by key-fn = coll))
  ([key-fn eq-fn coll]
   (loop [[v & t] (->> coll
                       (map (fn [x]
                              {:key   (key-fn x)
                               :value x})))
          result  []]
     (if v
       (recur
        (remove #(-> % :key (eq-fn (:key v))) t)
        (conj result (:value v)))
       result))))

(defn encode-url [s]
  (URLEncoder/encode s))

(defn make-phrase-url [search-text]
  (str "https://www.playphrase.me/phrase/"
       (some-> search-text
               nlp/remove-punctuation
               string/trim string/lower-case (string/replace #" +" "_") encode-url)))

(defn format-phrase-text [s]
  (format "\"%s\"" (string/capitalize s)))

(comment

  (distinct-by :a [{:a 1} {:a 1} {:a 2}])


  )
