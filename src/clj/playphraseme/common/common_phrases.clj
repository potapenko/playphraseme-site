(ns playphraseme.common.common-phrases
  (:require [playphraseme.api.queries.search-strings :as search-strings]
            [clojure.string :as string]
            [ring.util.http-response :refer :all]
            [playphraseme.common.nlp :as nlp]))

(declare build-map-right build-map-left)

(defn- build-map-left [text]
  (->>
   (search-strings/find-search-strings {:search-next text :count {"$gt" 10}})
   (pmap #(select-keys % [:text :count :words-count-without-stops]))
   (pmap #(assoc %
                :left (build-map-left (:text %))
                :right (build-map-right (:text %))))
   vec))

(defn- build-map-right [text]
  (->>
   (search-strings/find-search-strings {:search-pred text :count {"$gt" 10}})
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
         (take 50)
         (sort-by #(+ (:count %) (* 1000 (:words-count-without-stops %))))
         (map #(dissoc % :words-count-without-stops))
         reverse)))

(defn get-common-phrases-response [text]
  (ok (get-common-phrases text)))


(defn- distinct-texts [strings]
  (let [exists (->> strings (map :text))]
    (->> strings
         (remove (fn [{:keys [text] :as e}]
                   (loop [[v & t] exists]
                     (when v
                       (if (and (not= v text)
                                (re-find (re-pattern text) v))
                         true
                         (recur t)))))))))

(def ^:private ignore-strings [#"captain's log stardate"
                               #"final frontier"
                               #"starship"
                               #"hunting things"
                               #"captain"
                               #"log supplemental"
                               #"ahead warp"
                               #"Dr. House"
                               #"warp"
                               #"desperate housewives"])

(defn search-string-is-ignored? [text]
  (->> ignore-strings
       (drop-while #(nil? (re-find % text)))
       empty?
       not))

(defn get-all-common-phrases
  ([] (get-all-common-phrases 0 10))
  ([limit] (get-all-common-phrases 0 limit))
  ([skip limit]
   (->> (search-strings/find-search-strings {:count {"$gte" 10}} skip limit
                                            {:words-count-without-stops -1 :text -1})
        (map #(select-keys % [:text :count]))
        distinct-texts
        (remove #(-> % :text search-string-is-ignored?)))))

(defn get-all-common-phrases-response [skip limit]
  (ok (get-all-common-phrases skip limit)))

(comment

  (time
   (get-all-common-phrases 1 100))


  )
