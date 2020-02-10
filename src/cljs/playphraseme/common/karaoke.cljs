(ns playphraseme.common.karaoke
  (:require [playphraseme.common.nlp :as nlp]
            [playphraseme.common.util :as util]))

(defn- remove-words-padding [words]
  (loop [[w1 w2 & t] words
         result      []]
    (if w1
      (recur
       (concat [w2] t)
       (conj result
               (or
                (when w2
                  (let [d (- (:start w2) (:end w1))]
                    (when (> d 0)
                      (assoc w1
                       :end (:start w2)))))
                w1)))
      result)))

(defn- add-missing-words [words]
  (->> words
       (reduce
        (fn [{:keys [res missing last-word]} {:keys [start end missing?] :as w}]
          (let [last?         (= w (last words))
                missing-last? (and missing? last?)]
            (if (and missing? (not last?))
              {:res       res
               :missing   (conj missing w)
               :last-word last-word}
              {:res       (concat
                           res
                           (if (empty? missing)
                             []
                             (let [missing  (concat
                                             missing
                                             (when missing-last?
                                               [w]))
                                   from     (or (:end last-word) 0)
                                   to       (if-not missing-last? start end)
                                   one-word (-> to (- from) double (/ (count missing)) int)]
                               (->> missing
                                    (map-indexed
                                     (fn [idx x]
                                       (assoc x
                                              :start (-> from (+ (* idx one-word)))
                                              :end (-> from (+ (* idx one-word)) (+ one-word))))))))
                           (when-not missing-last?
                             [w]))
               :missing   []
               :last-word w})))
        {:res [] :missing [] :last-word nil})
       :res))

(defn- make-karaoke-words2 [{:keys [words text] :as phrase}]
  (let [n-words (nlp/create-words text)]
   (loop [[v & t] n-words
          index   0
          order   words
          result  []]
     (if v
       (let [first?    (util/first? index)
             last?     (util/last? index n-words)
             {:keys [start end]
              :as   w} (->> order
              (filter #(-> % :text
                           nlp/clean-text
                           (= (nlp/clean-text v))))
              first)]
         (recur t
                (inc index)
                (if w
                  (->> order (drop-while #(not= % w)) rest)
                  order)
                (conj
                 result
                 {:text     v
                  :missing? (nil? w)
                  :start    (if first?
                              0
                              (if w
                                start
                                -1))
                  :end      (if last?
                              (- (:end phrase) (:start phrase))
                              (if w end -1))})))
       (-> result
           add-missing-words)))))

(defn- make-karaoke-words [{:keys [words text] :as phrase}]
  (let [n-words (nlp/create-words text)]
   (loop [[v & t] n-words
          order   words
          result  []]
     (if v
       (let [{:keys [start end]
              :as   w} (->> order
              (filter #(-> % :text
                           nlp/clean-text
                           (= (nlp/clean-text v))))
              first)]
         (recur t
                (if w
                  (->> order (drop-while #(not= % w)) rest)
                  order)
                (conj
                 result
                 {:text     v
                  :start    (if w start (or (-> result last :end) 0))
                  :end      (if w end (or (-> result last :end) 0))})))
       result))))

(defn- add-start-ends [{:keys [] :as phrase} words]
  (concat
   [{:start 0
     :end   (-> words first :start)
     :text ""}]
   words
   [{:start (-> words last :end)
     :end   (- (:end phrase) (:start phrase))
     :text ""}]))

(defn make-karaoke [phrase]
 (->> phrase make-karaoke-words remove-words-padding (add-start-ends phrase)))
