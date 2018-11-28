(ns playphraseme.common.nlp
  (:require [clojure.string :as string]))

(def test-text " ")

(def one-dot-mark "#one#")
(def one-question-mark "#question#")
(def one-exclamation-mark "#exclamation#")
(def sentence-separator "#S#")
(def synthetic-sentence-separator "#L#")
(def escape_dot "#escape_dot#")
(def escape_dash "#escape_dash#")
(def escape_multidot "#escape_multidot#")
(def escape_longdash "#escape_longdash#")
(def escape_qustion "#escape_question#")
(def escape_exclamation "#escape_exclamation#")

(def ^:private stop-words (-> "nlp/stop-words.txt" io/resource slurp string/split-lines set))

(defn create-words [s]
  (-> s
      (->
       (string/replace #"\s+\.\.\.\s*" (str escape_dash " "))
       (string/replace #"\s+\-\s+" (str escape_dash " "))
       (string/replace #"\s*—\s*" (str escape_longdash " ")))
      (string/split #"\s+")
      (->>
       (map #(string/replace % escape_multidot " ... "))
       (map #(string/replace % escape_dash " - "))
       (map #(string/replace % escape_longdash " — "))
       (map string/trim)
       (filter (complement empty?)))))

(defn- person-dots [s]
  (let [person-words ["Dr" "Prof" "Ms" "Mrs" "Mr" "St"]]
    (loop [[v t] person-words
           s     s]
      (if v
        (recur t (string/replace s (str v ".") (str v one-dot-mark)))
        s))))

(defn remove-punctuation [s]
  (-> s
      string/lower-case
      (string/replace #"[.,\/#!$%\^&\*;:{}=\-_`~()—]" "")
      (string/replace #"\s+" " ")
      string/trim))

(defn create-sentences [s]
  (-> s
      person-dots
      (->
       (string/replace #"\.\*" escape_dot)
       (string/replace #"\?\*" escape_qustion)
       (string/replace #"\!\*" escape_exclamation))
      (->
       (string/replace #"\.\"\s+" (str one-dot-mark "\"" sentence-separator))
       (string/replace #"!\"\s+" (str one-exclamation-mark "\"" sentence-separator))
       (string/replace #"\?\"\s+\" " (str one-question-mark "\"" sentence-separator))
       (string/replace #"\.'\s+" (str one-dot-mark "'" sentence-separator))
       (string/replace #"!'\s+" (str one-exclamation-mark "'" sentence-separator))
       (string/replace #"\?'\s+" (str one-question-mark "'" sentence-separator)) ;
       (string/replace #"\.\s+" (str "." sentence-separator))
       (string/replace #"!\s+" (str "!" sentence-separator))
       (string/replace #"\?\s+" (str "?" sentence-separator)) ;
       (string/replace one-dot-mark ".")
       (string/replace one-question-mark "?")
       (string/replace one-exclamation-mark "!")
       (string/split (re-pattern (str sentence-separator "|" synthetic-sentence-separator))))
      (->>
       (map #(string/replace % escape_dot "."))
       (map #(string/replace % escape_qustion "?"))
       (map #(string/replace % escape_exclamation "!"))
       (map string/trim))))

(defn create-paragraphs [s]
  (->> s
       (#(string/split % #"(\r|\n)+"))
       (map string/trim)
       (filter (complement empty?))))

(defn create-text-parts [source]
  (let [p-counter  (atom 0)
        w-counter  (atom 0)
        s-counter  (atom 0)
        paragraphs (cond
                     (string? source) (create-paragraphs source)
                     (seq? source)    source)]
    (doall
     (for [p paragraphs]
       (do
         (println "id:" @p-counter)
         {:type      :paragraph
          :id        (swap! p-counter inc)
          :text      p
          :sentences (doall
                      (for [s (create-sentences p)]
                        {:type  :sentence
                         :id    (swap! s-counter inc)
                         :p-id  @p-counter
                         :text  s
                         :words (doall
                                 (for [w (create-words s)]
                                   {:type :word
                                    :id   (swap! w-counter inc)
                                    :p-id @p-counter
                                    :s-id @s-counter
                                    :text w}))}))})))))

(defn remove-first-word [s]
  (->> s create-words rest (string/join " ")))

(defn remove-last-word [s]
  (->> s create-words drop-last (string/join " ")))

(defn stop-word? [s]
  (-> s stop-words nil? not))

(defn count-words [s]
  (->> s
       create-words
       (remove stop-word?)
       count))
