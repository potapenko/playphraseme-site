(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [re-frame.core :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn toggle-play [])

(defn search-phrase [text]
  (rf/dispatch [::model/search-text text])
  (rf/dispatch [::model/search-result []])
  (when text
    (go
      (let [res (<! (rest-api/search-phrase text))]
        (rf/dispatch [::model/search-result res])))))

(defn scroll-end []
  (let [count-all    @(rf/subscribe [::model/search-count])
        count-loaded @(-> (rf/subscribe [::model/phrases]) count)]
    (when (< count-loaded count-all)
      (go
        (let [res (<! (rest-api/search-phrase @(rf/subscribe [::model/search-text])))]
          (rf/dispatch [::model/search-result-append res]))))))

(defn favorite-current-phrase [])
(defn show-config [])
(defn download-video [])
(defn show-search-help [])

(defn search-input []
  [:div.filters-container
   [:input#search-input.filter-input.form-control.input-lg
    {:type      "text" :placeholder "Search Phrase"
     :on-change #(search-phrase (-> % .-target .-value))}]
   [:ul.filter-input-icons
    [:li [:div.numbers @(rf/subscribe [::model/search-count])]]
    [:li
     [:div.filter-input-icon
      {:on-click toggle-play}
      [:span.fa-stack.fa-1x
       [:i.fa.fa-circle.fa-stack-2x]
       (if-not @(rf/subscribe [::model/stoped])
         [:i.fa.fa-play.fa-stack-1x.fa-inverse.play-icon]
         [:i.fa.fa-pause.fa-stack-1x.fa-inverse.pause-icon])]]]
    [:li
     [:div.filter-input-icon
      {:on-click favorite-current-phrase}
      [:span.fa-stack.fa-1x
       [:i.fa.fa-circle.fa-stack-2x]
       [:i.fa.fa-star.fa-stack-1x.fa-inverse]]]]
    [:li
     [:div.filter-input-icon
      {:on-click show-config}
      [:i.fa.fa-cog.fa-2x]]]
    [:li
     [:div.filter-input-icon
      {:on-click show-search-help}
      [:i.fa.fa-question-circle.fa-2x]]]]])

(defn favorite-phrase [id]
  (println "favorite pharase:" id))

(defn page []
  (r/create-class
   {:component-did-mount
    (fn []
      (some-> "search-input" js/document.getElementById .focus))
    :reagent-render
    (fn []
      (let [lang (util/locale-name)]
        [:div.search-container
         [:div.search-content
          [:div.video-player-container ""]
          [:div.search-ui-container [search-input]]
          [:div.search-results-container
           [:table.table.table-hover.phrase-table.borderless
            [:tbody
             (doall
              (for [x (range 100)]
                ^{:key (str "elem-" x)}
                [:tr
                 [:td.phrase-number (inc x)]
                 [:td.phrase-text "any text"]
                 [:td.translate-icons
                  [:a.lang-in-circle
                   {:href "" :on-click #(favorite-phrase x)}
                   [:i.fa.fa-star.fa-1x]]
                  [:a.lang-in-circle
                   {:href (str "https://translate.google.com/#en/" lang "/text_here") :target "_blank"} lang]
                  [:a.lang-in-circle
                   {:href (str "/#/phrase" x)} "#"]]]))]]]]]))}))

