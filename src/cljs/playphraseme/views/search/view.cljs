(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [re-frame.core :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn favorite-phrase [id]
  (println "favorite pharase:" id))


(defn page []
  (let [lang (util/locale-name)]
   [:div.search-container
    [:div.search-content
     [:div.video-player-container ""]
     [:div.search-ui-container ""]
     [:div.search-results-container
      [:div.table.table-hover.phrase-table.borderless
       [:tbody
        (doall
         (for [x (range 100)]
           ^{:key (str "elem-" x)}
           [:tr
            [:td.phrase-number (inc x)]
            [:td.phrase-text "any text"]
            [:td.translate-icons
             [:a.lang-in-circle
              {:href ""
               :on-click #(favorite-phrase x)}
              [:i.fa.fa-star.fa-1x] ]
             [:a.lang-in-circle
              {:href (str "https://translate.google.com/#en/" lang "/text_here") :target "_blank"} lang]
             [:a.lang-in-circle
              {:href (str "/#/phrase" x)} "#"]]]))]]]]]))


