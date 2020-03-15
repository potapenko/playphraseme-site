(ns playphraseme.views.search.ctrl
  (:require [clojure.string :as string]
            [playphraseme.common.nlp :as nlp]
            [re-frame.core :as rf]
            [cljs.pprint :as pp]
            [playphraseme.common.util :as util]))

(defn get-searched-words [current-words text]
  (->> current-words
       util/build-all-sequences
       (filter
        (fn [words]
          (= (->> words (map :text) (map nlp/clean-text) (string/join " "))
             (nlp/clean-text text))))
       first))
