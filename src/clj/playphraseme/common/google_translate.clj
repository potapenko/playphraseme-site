(ns playphraseme.common.google-translate
  (:require [org.httpkit.client :as client]
            [playphraseme.app.config :refer [env]]
            [clojure.data.json :as json]))

(defn- make-url [text from to]
  (str "https://www.googleapis.com/language/translate/v2"
       "?key=" (env :google-translate-key)
       "&source=" from
       "&target=" to
       "&q=" text))

(defn- translate-async [text from to]
  "Translates a string. Returns promise which is either translated string
  or a map {:error response}"
  (client/get (make-url text from to) {}
              (fn [{:keys [status body] :as resp}]
                (if (and (>= status 200) (< status 300))
                  (json/read-str body :key-fn keyword)
                  {:error  resp}))))

(defn translate [text from to]
  "Translate a string. returns translated string or throws an exception"
  (let [result @(translate-async text from to)]
    (if (:error result)
      (throw (Exception. (str (:error result))))
      (->> result :data :translations (map :translatedText) first))))
