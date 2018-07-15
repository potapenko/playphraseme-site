(ns playphraseme.common.suggestions
  (:require [clojure.string :as string]
            [playphraseme.api.queries.search-strings :as search-strings])
  (:gen-class))

(defn- words [text]
  (reverse (re-seq #"[a-z]+" (string/lower-case text))))

(defn- train [features]
  (loop [[v & t] features model {} pos 1]
    (if v
      (recur t (assoc model v pos) (inc pos))
      model)))

(def nwords (train (words (slurp "resources/search/big-text.txt"))))

(defn- edits [word]
  (let [alphabet "abcdefghijklmnopqrstuvwxyz" n (count word)]
    (distinct (concat
               (for [i (range n)] (str (subs word 0 i) (subs word (inc i))))
               (for [i (range (dec n))]
                 (str (subs word 0 i) (nth word (inc i)) (nth word i) (subs word (+ 2 i))))
               (for [i (range n) c alphabet] (str (subs word 0 i) c (subs word (inc i))))
               (for [i (range (inc n)) c alphabet] (str (subs word 0 i) c (subs word i)))))))

(defn- known [words nwords] (let [result (set (for [w words :when (nwords w)]  w))]
                              (if (empty? result)
                                nil
                                result)))

(defn- known-edits2 [word nwords] (let [result (set (for [e1 (edits word)
                                                          e2 (edits e1)
                                                          :when (nwords e2)]  e2))]
                                    (if (empty? result)
                                      nil
                                      result)))
(defn get-candidates [word]
  (sort #(compare (get nwords %2 1) (get nwords %1 1))
        (or (known [word] nwords)
            (known (edits word) nwords)
            (known-edits2 word nwords) [word])))

(defn correct [word]
  (first (get-candidates word)))

(defn- count-in-db [text]
  (or
   (:count (first (search-strings/find-search-strings {:text text})))
   0))

(defn- create-words [text]
  (for [w (string/split text #"\s+")]
    (if (> (count-in-db w) 0)
      (list w)
      (get-candidates w))))

(defn- create-generation [current mutations]
  (if (empty? current)
    mutations
    (loop [[c & t] current result []]
      (if c
        (recur t (concat result (for [m mutations] (str c " " m))))
        result))))

(defn- generate-phrases [phrase]
  (println "phrase:" phrase ())
  (loop [[c & t] (create-words phrase) phrases []]
    (if c
      (recur t (create-generation phrases c))
      phrases)))

(defn phrase-candidates [phrase]
  (let [phrase (string/trim phrase)]
    (if (empty? phrase)
      []
      (sort #(compare (:count %2) (:count %1))
            (filter #(> (:count %) 0)
                    (map
                     (fn [x]
                       {:text x :count (count-in-db x)}) (generate-phrases phrase)))))))
