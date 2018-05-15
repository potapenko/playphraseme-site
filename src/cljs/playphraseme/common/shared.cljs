(ns playphraseme.common.shared
  (:require [clojure.string :as string]
            [cljs-await.core :refer [await await-cb]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [playphraseme.common.util :as util]
            [playphraseme.common.ui :as ui :refer [flexer spacer]]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.views.favorites.view :as favorites-page]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.nlp :as nlp])
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
   [:div "Functionality will be ready for several days. You can support us with like :)"]
   [:div {:style {:display "flex" :flex-direction "row"}}]])
