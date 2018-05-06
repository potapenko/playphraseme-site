(ns playphraseme.common.shared
  (:require [clojure.string :as string]
            [cljs-await.core :refer [await await-cb]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [playphraseme.common.util :as util]
            [playphraseme.common.ui :as ui]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.views.favorites.view :as favorites-page]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.nlp :as nlp])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn under-construction []
  [:div.under-contsuction
   "Page Under Construction."
   [ui/spacer 16]
   [:div "At the moment, this functionality is available on the "
    [:a.underline {:href ""}
     "old version of our site."]]])
