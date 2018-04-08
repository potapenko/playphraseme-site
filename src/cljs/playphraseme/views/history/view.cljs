(ns playphraseme.views.history.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.history.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn elements-list [list]
  (let-sub [::model/history-list]
    [:div.elements-container
     (doall
      (for [{:keys [text count index]} history-list]
        ^{:key (str "element-" index)}
        [:a.element
         {:id    (str "element-" index)
          :href  (str "/#/search?q=" text)}
         [:div.text text]
         [:div.grow]
         [:div.counter (str count)]]))]))

(defn page []
  [:div "History page"])

