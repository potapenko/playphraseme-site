(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [re-frame.core :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn search-input []
  [:div.filters-container-top
   [:input.filter-input.form-control.input-lg
    {:type "text" :placeholder "Search Text"}]
   [:div.filters-container
    [:ul.filter-input-icons
     [:li [:div.numbers {:ng-bind "searchCount"}]]
     [:li
      [:div.filter-input-icon
       {:ng-click "togglePlay()"}
       [:span.fa-stack.fa-1x
        [:i.fa.fa-circle.fa-stack-2x]
        [:i.fa.fa-play.fa-stack-1x.fa-inverse.play-icon
         {:ng-show "!userPlayed"}]
        [:i.fa.fa-pause.fa-stack-1x.fa-inverse.pause-icon
         {:ng-show "userPlayed"}]]]]
     [:li
      [:div.filter-input-icon
       {:ng-click "favoritePhrase(currentPhrase)"}
       [:md-tooltip "{{'tooltip.favorite.phrase' | translate}}"]
       [:span.fa-stack.fa-1x
        [:i.fa.fa-circle.fa-stack-2x]
        [:i.fa.fa-star.fa-stack-1x.fa-inverse]]]]
     [:li
      [:div.filter-input-icon
       {:ng-click "showConfigPopup()"}
       [:md-tooltip "{{'tooltip.show.config' | translate}}"]
       [:i.fa.fa-cog.fa-2x]]]
     [:li
      [:div.filter-input-icon
       {:ng-mouseout "hideHelp();" :ng-mouseover "showHelp()"}
       [:i.fa.fa-question-circle.fa-2x]]]]]]
  )


(defn favorite-phrase [id]
  (println "favorite pharase:" id))


(defn page []
  (let [lang (util/locale-name)]
    [:div.search-container
     [:div.search-content
      [:div.video-player-container ""]
      [:div.search-ui-container [search-input]]
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


