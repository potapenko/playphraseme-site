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
            [playphraseme.common.nlp :as nlp]
            [re-frame.core :as rf])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn toggle-play []
  (let [now-stopped? @(rf/subscribe [::model/stopped])
        index @(rf/subscribe [::model/current-phrase-index])]
    (if now-stopped?
      (player/play index)
      (player/stop index))
    (rf/dispatch [::model/stopped (not now-stopped?)])))

(defn search-phrase [text]
  (rf/dispatch [::model/search-text text])
  (rf/dispatch [::model/search-result []])
  (rf/dispatch [::model/current-phrase-index nil])
  (when text
    (util/set-url! "search" {:q text})
    (go
      (let [res (<! (rest-api/search-phrase text 10 0))]
        (rf/dispatch [::model/search-result res])))))

(defn scroll-end []
  (let [count-all    @(rf/subscribe [::model/search-count])
        count-loaded (-> @(rf/subscribe [::model/phrases]) count)]
    (when (< count-loaded count-all)
      (go
        (let [res (<! (rest-api/search-phrase
                       @(rf/subscribe [::model/search-text])
                       10 count-loaded))]
          (rf/dispatch [::model/search-result-append res]))))))

(defn on-phrases-scroll [e]
  (let [sh (-> e .-target .-scrollHeight)
        st (-> e .-target .-scrollTop)
        oh (-> e .-target .-offsetHeight)
        th 50]
    (when (= st 0)
      #_(println "start"))
    (when (>= (+ oh st) sh)
      #_(println "end"))
    (when (>= (+ oh st th) sh)
      #_(println "load new")
      (scroll-end))))

(defn scroll-to-phrase [index]
  (when-let [elem (js/document.getElementById (str "phrase-text-" index))]
    (-> elem (.scrollIntoView #js{:behavior "smooth" :block "start"}))))

(defn next-phrase []
  (rf/dispatch [::model/next-phrase])
  (let [current @(rf/subscribe [::model/current-phrase-index])
        loaded  (count @(rf/subscribe [::model/phrases]))]
    (when (-> current (+ 5) (> loaded))
      (scroll-end))))

(defn  update-current-word [pos]
  (println "update current world: " pos)
  (let [phrases @(rf/subscribe [::model/phrases])
        current-phrase-index @(rf/subscribe [::model/current-phrase-index])
        current-phrase (nth phrases current-phrase-index)]
    (when current-phrase
      (let [current-word (->> current-phrase :words (filter #(-> % :start (< pos))) last)]
        (rf/dispatch-sync [::model/current-word-index (:index current-word)])))))

(defn favorite-current-phrase [])
(defn show-config [])
(defn download-video [])
(defn show-search-help [])

(defn search-input []
  [:div.filters-container
   [:input#search-input.filter-input.form-control.input-lg
    {:type      "text" :placeholder "Search Phrase"
     :value     @(rf/subscribe [::model/search-text])
     :on-change #(search-phrase (-> % .-target .-value))}
    ]
   [:ul.filter-input-icons
    [:li [:div.search-result-count @(rf/subscribe [::model/search-count])]]
    [:li
     [:div.filter-input-icon
      {:on-click toggle-play}
      [:span.fa-stack.fa-1x
       [:i.fa.fa-circle.fa-stack-2x]
       (if @(rf/subscribe [::model/stopped])
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

(defn karaoke-words [words]
  [:div.karaoke
   (for [w    words
         :let [{:keys [formated-text index]} w]]
     ^{:key (str "word-" index)}
     [:a.s-word {:href ""} formated-text])])

(defn karaoke-words-current [words]
  [:div.karaoke
   (let [played-word-index @(rf/subscribe [::model/current-word-index])]
     (for [w    words
           :let [{:keys [formated-text text index]} w]]
       ^{:key (str "word-" index)}
       [:a.s-word {:href  ""
                   :class (util/class->str
                           (when (= index played-word-index) "s-word-played"))}
        formated-text]))])

(defn karaoke [phrase]
  (let [{:keys [words text id index]} phrase
        nlp-words               (nlp/create-words text)
        words                   (map (fn [w1 w2] (assoc w1 :formated-text w2))
                                     words nlp-words)
        current-index           (rf/subscribe [::model/current-phrase-index])]
    (fn []
      (if (= @current-index index)
        [karaoke-words-current words]
        [karaoke-words words]))))

(defn phrase-text [x]
  (r/create-class
   {:reagent-render
    (fn []
      (let [lang (util/locale-name)
            {:keys [index text id]} x]
        (fn []
          [:tr {:id       (str "phrase-text-" index)
                :on-click #(rf/dispatch [::model/current-phrase-index (:index x)])}
           [:td [:div.phrase-number (-> x :index inc)]]
           [:td.phrase-text [karaoke x]]
           [:td.translate-icons
            [:a.lang-in-circle
             {:href "" :on-click #(favorite-phrase x)}
             [:i.fa.fa-star.fa-1x]]
            [:a.lang-in-circle
             {:href (str "https://translate.google.com/#en/" lang "/" text) :target "_blank"} lang]
            [:a.lang-in-circle
             {:href (str "/#/phrase/" id)} "#"]]])))}))


(defn page [params]
  (r/create-class
   {:component-will-mount
    (fn [this])
    :component-did-mount
    (fn [this]
      (when-let [elem (some-> "search-input" js/document.getElementById)]
        (aset elem "selectionStart" (-> elem .-value count))
        (-> elem .focus)))
    :reagent-render
    (fn []
      (let [lang    (util/locale-name)
            current (rf/subscribe [::model/current-phrase-index])
            phrases (rf/subscribe [::model/phrases])
            stopped (rf/subscribe [::model/stopped])]
        (let [q (some-> params :q)]
          (search-phrase q))
        (fn []
          [:div.search-container
           [:div.search-content
            ^{:key (str "video-list- " @current)}
            [:div.video-player-container
             (doall
              (for [x     @phrases
                    :let  [{:keys [index id]} x]
                    :when (<= @current index (inc @current))]
                ^{:key (str "phrase-" index "-" id)}
                [player/video-player {:phrase         x
                                      :hide?          (not= @current index)
                                      :on-play        #(scroll-to-phrase index)
                                      :on-end         next-phrase
                                      :on-pos-changed update-current-word
                                      :stopped?       @stopped}]))]
            [:div.search-ui-container [search-input]]
            [:div#search-result.search-results-container
             {:on-scroll #(on-phrases-scroll %)}
             [:table.table.table-hover.phrase-table.borderless
              [:tbody
               (doall
                (for [x @phrases]
                  ^{:key (str "phrase-" x)}
                  [phrase-text x]))]]]]])))}))

