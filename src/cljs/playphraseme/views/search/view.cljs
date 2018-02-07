(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.video-player :as player]
            [re-frame.core :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn toggle-play [])

(defn search-phrase [text]
  (rf/dispatch [::model/search-text text])
  (rf/dispatch [::model/search-result []])
  (rf/dispatch [::model/current-phrase-index nil])
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
    [:li [:div.search-result-count @(rf/subscribe [::model/search-count])]]
    [:li
     [:div.filter-input-icon
      {:on-click toggle-play}
      [:span.fa-stack.fa-1x
       [:i.fa.fa-circle.fa-stack-2x]
       (if-not @(rf/subscribe [::model/stopped])
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
      (let [lang    (util/locale-name)
            current (rf/subscribe [::model/current-phrase-index])
            phrases (rf/subscribe [::model/phrases])
            stopped (rf/subscribe [::model/stopped])]
        (fn []
          [:div.search-container
           [:div.search-content
            [:div.video-player-container
             (doall
              (for [x     @phrases
                    :let  [{:keys [index id]} x]
                    :when (<= @current index (inc @current))]
                ^{:key (str "phrase-" index "-" id)}
                [player/video-player {:phrase         x
                                      :hide?          (not= @current index)
                                      :on-pause       #(println "video pause")
                                      :on-play        #(println "video play")
                                      :on-end         #(println "video ended")
                                      :on-pos-changed #(println "video position changed")
                                      :on-load        #(println "video loaded")
                                      :stopped?       @stopped
                                      :position       0}]))]
            [:div.search-ui-container [search-input]]
            [:div.search-results-container
             [:table.table.table-hover.phrase-table.borderless
              [:tbody
               (doall
                (for [x @phrases]
                  ^{:key (str "phrase-" x)}
                  [:tr {:on-click #(rf/dispatch [::model/current-phrase-index (:index x)])}
                   [:td.phrase-number (-> x :index inc)]
                   [:td.phrase-text (:text x)]
                   [:td.translate-icons
                    [:a.lang-in-circle
                     {:href "" :on-click #(favorite-phrase x)}
                     [:i.fa.fa-star.fa-1x]]
                    [:a.lang-in-circle
                     {:href (str "https://translate.google.com/#en/" lang "/" (:text x)) :target "_blank"} lang]
                    [:a.lang-in-circle
                     {:href (str "/#/phrase" x)} "#"]]]))]]]]])))}))

