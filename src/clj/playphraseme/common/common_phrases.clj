(ns playphraseme.common.common-phrases
  (:require [playphraseme.api.queries.search-strings :as search-strings]
            [clojure.string :as string]
            [ring.util.http-response :refer :all]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.api.queries.common-phrases :as db-common-phrases]
            [playphraseme.common.util :as util]))

(declare build-map-right build-map-left)

(defn- build-map-left [text]
  (->>
   (search-strings/find-search-strings {:search-next text :count {"$gt" 10}})
   (pmap #(select-keys % [:text :count :words-count :words-count-without-stops]))
   (pmap #(assoc %
                :left (build-map-left (:text %))
                :right (build-map-right (:text %))))
   vec))

(defn- build-map-right [text]
  (->>
   (search-strings/find-search-strings {:search-pred text :count {"$gt" 10}})
   (pmap #(select-keys % [:text :count :words-count :words-count-without-stops]))
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
           (select-keys e [:count :text :words-count :words-count-without-stops]))
         (:left e)
         (:right e)]
        e)))
   flatten
   (remove nil?)
   distinct
   (sort-by :words-count-without-stops)
   reverse
   vec))


(defn- compact-common-phrases [phrases]
  (loop [min-count 10
         phrases   phrases]
    (let [new-phrases (->> phrases
                           (filter #(-> % count (>= min-count))))]
      (if (-> new-phrases count (< 20))
        phrases
        (recur (inc min-count) new-phrases)))))


(defn- distinct-texts [strings]
  (util/distinct-by :text (fn [a b]
                            (or
                             (re-find (re-pattern a) b)
                             (re-find (re-pattern b) a)))
                    strings))

(defn distinct-texts-by-stops [phrases]
  (if (-> phrases count (<= 20))
    phrases
    (util/distinct-by #(-> % :text string/lower-case nlp/remove-stop-words) phrases)))

(defn get-common-phrases [text]
  (let [text (-> text string/trim string/lower-case)]
    (if (-> text count (<= 2))
      []
      (->> text
           create-phrases-map
           flat-phrases-map
           (remove #(-> % :text (= text)))
           (take 100)
           distinct-texts-by-stops
           compact-common-phrases
           (sort-by #(+ (:count %) (* 1000 (:words-count-without-stops %))))
           reverse
           (take 20)
           (map #(dissoc % :words-count-without-stops))
           (map #(dissoc % :words-count))))))

(defn get-common-phrases-response [text]
  (ok (get-common-phrases text)))



(def ^:private ignore-strings [#"captain's log stardate" #"final frontier"
                               #"starship" #"hunting things"
                               #"saving people" #"people hunting"
                               #"captain" #"log supplemental"
                               #"sheldon" #"hailing"
                               #"ahead warp" #"farrah"
                               #"spock" #"penny" #"\d" #"sam"
                               #"dean" #"alert one"
                               #"winchester" #"dr\.? house"
                               #"warp" #"desperate housewives"])

(defn search-string-is-ignored? [text]
  (let [text (string/lower-case text)]
   (->> ignore-strings
        (drop-while #(nil? (re-find % text)))
        empty?
        not)))

(defn get-all-common-phrases
  ([] (get-all-common-phrases 0 10))
  ([limit] (get-all-common-phrases 0 limit))
  ([skip limit]
   (->> (search-strings/find-search-strings {:count {"$gte" 10}} skip limit
                                            {:words-count -1 :words-count-without-stops -1 :count -1})
        (map #(select-keys % [:text :count]))
        distinct-texts
        ;; distinct-texts-by-stops
        (remove #(-> % :text search-string-is-ignored?)))))

(defn get-all-common-phrases-response [skip limit]
  (ok (get-all-common-phrases skip limit)))

(defn generate-common-phrases []
  #_(loop [phrases ]))

(comment

  (time
   (get-all-common-phrases 1 100))

  (get-common-phrases "j")


  (count (get-all-common-phrases (* 30 58) 30))

  (get-all-common-phrases 0 50)

  )
