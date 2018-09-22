(ns playphraseme.common.common-phrases
  (:require [playphraseme.api.queries.search-strings :as search-strings]
            [clojure.string :as string]
            [ring.util.http-response :refer :all]
            [playphraseme.common.nlp :as nlp]))

(declare build-map-right build-map-left)

(defn- build-map-left [text]
  (->>
   (search-strings/find-search-strings {:search-next text :count {"$gt" 5}})
   (pmap #(select-keys % [:text :count :words-count-without-stops]))
   (pmap #(assoc %
                :left (build-map-left (:text %))
                :right (build-map-right (:text %))))
   vec))

(defn- build-map-right [text]
  (->>
   (search-strings/find-search-strings {:search-pred text :count {"$gt" 5}})
   (pmap #(select-keys % [:text :count :words-count-without-stops]))
   (pmap #(assoc %
                :right (build-map-right (:text %))
                :left (build-map-left (:text %))))
   vec))

(defn create-phrases-map [text]
  {:text  text
   :count (search-strings/count-search-strings {:text text})
   :left  (build-map-left text)
   :right (build-map-right text)})

(defn- flat-phrases-map [m]
  (->> m
   (clojure.walk/postwalk
    (fn [e]
      (if (and (map? e))
        [(when (and (-> e :left (= [])) (-> e :right (= [])))
           (select-keys e [:count :text :words-count-without-stops]))
         (:left e)
         (:right e)]
        e)))
   flatten
   (remove nil?)
   (sort-by :words-count-without-stops)
   (map #(dissoc % :words-count-without-stops))
   distinct
   reverse
   vec))

(defn get-common-phrases [text]
  (if (nlp/stop-word? text)
    []
    (->> text
         string/trim
         string/lower-case
         create-phrases-map
         flat-phrases-map
         (remove #(-> % :text (= text)))
         (take 10))))

(defn get-common-phrases-response [text]
  (ok (get-common-phrases text)))

(comment

  (time
   (get-common-phrases "a book"))


  )
