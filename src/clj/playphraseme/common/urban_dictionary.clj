(ns playphraseme.common.urban-dictionary
  (:require [clj-http.client :as http]
            [camel-snake-kebab.core :as camel-kebab]
            [cheshire.core :as cheshire]
            [clojure.string :as string]))

(defn suggestions [q]
  (-> (http/get (str "https://api.urbandictionary.com/v0/autocomplete?term=" q))
      :body (cheshire/parse-string true)))

(defn- load-term [q]
  (-> (http/get (str "https://api.urbandictionary.com/v0/define?term=" q))
      :body
      (cheshire/parse-string camel-kebab/->kebab-case-keyword)))

(defn search [q]
  (->> (load-term q)
       :list
       ;; (sort-by #(- (:thumbs-down %) (:thumbs-up %)))
       (take 3)
       (map #(select-keys % [:definition :word :example]))
       (map #(update % :definition string/replace #"(\n|\r)+" "\n"))
       (map #(update % :example string/replace #"(\n|\r)+" "\n"))
       (map #(update % :word string/replace #"(\n|\r)+" "\n"))))


(comment


  (->> (search "i don't know what you're talking about")
       (map clojure.pprint/pprint))



  )
