(ns playphraseme.common.google-translate
  (:require [clj-http.client :as client]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :as parser]
            [clojure.data.json :as json]
            [playphraseme.api.queries.common-phrases :as common-phrases-db]
            [clojure.string :as string]))

(defn- make-url [text from to]
  (str "https://www.googleapis.com/language/translate/v2"
       "?key=" (env :google-translate-key)
       "&source=" from
       "&target=" to
       "&q=" text))

(defn translate [text from to]
  "Translate a string. returns translated string or throws an exception"
  (if-let [cached (when (= from "en")
                    (some-> (common-phrases-db/find-one-common-phrase {:text (-> text
                                                                                 string/lower-case
                                                                                 string/trim)})
                            keys
                            :localizations
                            (keyword to)
                            :title))]
    cached
    (let [result (-> (client/get (make-url text from to))
                     :body (parser/parse-string keyword))]
      (if (:error result)
        (throw (Exception. (str (:error result))))
        (->> result :data :translations (map :translatedText) first)))))

(comment
  (translate "would you like me to" "en" "ja")

)
