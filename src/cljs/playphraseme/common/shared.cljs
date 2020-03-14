(ns playphraseme.common.shared
  (:require [clojure.string :as string]
            [cljs-await.core :refer [await await-cb]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [re-catch.core :as rc]
            [playphraseme.common.util :as util]
            [playphraseme.common.ui :as ui :refer [flexer spacer]]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.phrases :as phrases]
            [cljs.pprint :as pp])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn facebook-like-button []
  (let-sub [scale :responsive-scale]
    [:div.fb-like
     {:style {:transform "scale3d(3,3,1)"
              :margin "70px 22px 70px 77px"}
      :data-share      "false"
      :data-show-faces "false"
      :data-size       "large"
      :data-width      "100"
      :data-action     "like"
      :data-layout     "button"
      :data-href       "https://www.facebook.com/playphrase/"}]))

(defn under-construction []
  [:div.under-contsuction
   "Page Under Construction."
   [ui/spacer 16]
   [:div {:style {:display "flex" :flex-direction "row"}}]])

(defn test-data [data]
  [:pre (with-out-str (cljs.pprint/pprint data))])

(defn component-base [& body]
  (into [rc/catch] body))

(defn repeat-tag [n tag]
  [:<>
   (->> (range n)
        (map
         (fn [x]
           ^{:key [(hash tag) x]}
           tag))
        doall)])

(defn html-div [html-content]
  [:div {"dangerouslySetInnerHTML"
         #js{:__html html-content}}])

(defn html-span [html-content]
  [:span {"dangerouslySetInnerHTML"
          #js{:__html html-content}}])

(defn highlite-search [text search-text-rx]
  (if (and text search-text-rx)
    (if (re-find search-text-rx text)
      [html-span
       (string/replace
        text search-text-rx
        "<span class=\"d-inline-block bg-gray-300\">$1</span>")]
      text)
    text))
