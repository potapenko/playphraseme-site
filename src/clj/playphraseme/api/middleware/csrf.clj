(ns playphraseme.api.middleware.csrf
  (:require [clojure.pprint :refer :all]))

(defn csrf-mw [handler]
  (fn [request]
    (let [response (handler request)]
      ;; TODO - csrf check
      response)))
