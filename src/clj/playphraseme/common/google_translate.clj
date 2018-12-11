(ns playphraseme.common.google-translate
  (:require [clj-http.client :as client]
            [playphraseme.app.config :refer [env]]
            [cheshire.core :as parser]
            [clojure.data.json :as json]))

(defn- make-url [text from to]
  (str "https://www.googleapis.com/language/translate/v2"
       "?key=" (env :google-translate-key)
       "&source=" from
       "&target=" to
       "&q=" text))

(defn translate [text from to]
  "Translate a string. returns translated string or throws an exception"
  (let [result (-> (client/get (make-url text from to))
                   :body (parser/parse-string keyword))]
    (if (:error result)
      (throw (Exception. (str (:error result))))
      (->> result :data :translations (map :translatedText) first))))

(comment

)
