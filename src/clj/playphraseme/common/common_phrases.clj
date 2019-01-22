(ns playphraseme.common.common-phrases
  (:require [playphraseme.api.queries.search-strings :as search-strings]
            [clojure.string :as string]
            [ring.util.http-response :refer :all]
            [playphraseme.common.nlp :as nlp]
            [playphraseme.api.queries.common-phrases :as common-phrases-db]
            [playphraseme.common.util :as util]
            [playphraseme.common.urban-dictionary :as urban-dictionary]
            [playphraseme.api.queries.phrases :as phrases]))

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

(defn get-bad-common-phrases []
  (->> ["fuck" "bitter" "wanker" "bitch" "pussy" "shit" "crap" "ass" "asshole" "fucker"
        "blowjob" "dumbass" "dickhead" "fucking" "motherfucker"]
       (map get-common-phrases)
       flatten
       distinct))

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

(defn get-search-strings [skip limit]
  (search-strings/find-search-strings {:count {"$gte" 10}} skip limit
                                      {:words-count -1 :words-count-without-stops -1 :count -1}))

(defn get-all-common-phrases
  ([] (get-all-common-phrases 0 10))
  ([limit] (get-all-common-phrases 0 limit))
  ([skip limit]
   (->> (get-search-strings skip limit)
        (map #(select-keys % [:text :count]))
        distinct-texts
        ;; distinct-texts-by-stops
        (remove #(-> % :text search-string-is-ignored?)))))

(defn get-all-common-phrases-response [skip limit]
  #_(ok (get-all-common-phrases skip limit))
  (ok (common-phrases-db/find-common-phrases {:pred {} :skip skip :limit limit :sort {:index 1}}) ))

(defn generate-common-phrases []
  (let [index (atom 0)]
   (->> (range 1 31)
        (map (fn [pos]
               (println "\n")
               (println "pos:" pos)
               (println "\n")
               (let [phrases (get-all-common-phrases (* (dec pos) 100) 100)]
                 (println "phrases:" (count phrases))
                 (->> phrases
                      (map-indexed
                       (fn [i {:keys [count text]}]
                         (if-let [exists (common-phrases-db/find-one-common-phrase {:text text})]
                           (common-phrases-db/update-common-phrase!
                            (assoc exists :index (swap! index inc) :count count))
                           (let [urban (urban-dictionary/search text)]
                             (common-phrases-db/insert-common-phrase!
                              {:index      (swap! index inc)
                               :count      count
                               :text       text
                               :dictionary urban})))))
                      doall))))
        doall
        flatten
        count)))

(comment


  (generate-common-phrases)

  (common-phrases-db/count-common-phrases {})

  (string/lower-case "2TlGRIY")

  )
