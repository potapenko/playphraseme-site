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

(defn get-current-phrase-index []
  @(rf/subscribe [::model/current-phrase-index]))

(defn get-current-phrase []
  (let [phrases @(rf/subscribe [::model/phrases])
        index (get-current-phrase-index)]
    (some->> phrases (drop-while #(-> % :index (not= index))) first)))

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

(defn highlite-word [current-word]
  (doseq [x (:words (get-current-phrase))]
    (some-> x :index (->> (str "word-")) util/id->elem (util/remove-class "s-word-played")))
  (some-> current-word :index (->> (str "word-")) util/id->elem (util/add-class "s-word-played"))
  #_(rf/dispatch-sync [::model/current-word-index (:index current-word)]))

(defn update-current-word [pos]
  (let [current-word (some->> (get-current-phrase) :words (filter #(-> % :start (< pos))) last)]
    (when current-word
      (highlite-word current-word))))

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

(defn goto-word [e phrase-index word-index]
  (-> e .preventDefault)
  (let [phrase (nth @(rf/subscribe [::model/phrases]) phrase-index)
        word (-> phrase :words (nth word-index))]
    (rf/dispatch-sync [::model/current-word-index] (:index phrase))
    (player/jump (:index phrase) (+ 400 (:start word)))
    (highlite-word word)
    (player/play (:index phrase))))

(defn get-searched-words [current-words]
  (let [text             @(rf/subscribe [::model/search-text])
        all-search-words (-> text string/lower-case nlp/create-words)]
    (loop [[v & t]      current-words
           search-words all-search-words
           result       []]
      (let [star?          (= "*" (first search-words))
            current-search (->> search-words (drop-while #{"*"}) first)]
        (if (and v current-search)
          (cond
            (= (:text v) current-search) (recur t (->> search-words (drop-while #{"*"}) rest) (conj result v))
            star?                        (recur t search-words (conj result v))
            :else                        (recur t all-search-words []))
          result)))))

(defn karaoke-words-current [phrase-index words]
  [:div.karaoke
   (let [played-word-index @(rf/subscribe [::model/current-word-index])]
     (for [w    words
           :let [{:keys [formated-text text index searched]} w]]
       ^{:key (str "word-" index)}
       [:a.s-word {:href     ""
                   :on-click #(goto-word % phrase-index index)
                   :id       (str "word-" index)
                   :class    (util/class->str
                              (when searched "s-word-searched")
                              (when (= index played-word-index) "s-word-played"))}
        formated-text]))])

(defn karaoke-words [phrase-index words]
  [:div.karaoke
   (for [w    words
         :let [{:keys [formated-text index searched]} w]]
     ^{:key (str "word-" index)}
     [:a.s-word {:href "" :class (when searched "s-word-searched")
                 :on-click #(goto-word % phrase-index index)} formated-text])])

(defn karaoke [phrase]
  (let [{:keys [words text id index]} phrase
        nlp-words                     (nlp/create-words text)
        searched-words                (set (get-searched-words words))
        words                         (map (fn [w1 w2]
                                             (assoc w1
                                                    :formated-text w2
                                                    :searched (-> w1 searched-words nil? not)))
                                           words nlp-words)
        current-index                 (rf/subscribe [::model/current-phrase-index])]
    (fn []
      (if (= @current-index index)
        [karaoke-words-current index words]
        [karaoke-words index words]))))

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
   {:component-did-mount
    (fn [this]
      (when-let [elem (some-> "search-input" js/document.getElementById)]
        (-> elem .-selectionStart (set! (-> elem .-value count)))
        (-> elem .focus)))
    :reagent-render
    (fn []
      (let [lang        (util/locale-name)
            current     (rf/subscribe [::model/current-phrase-index])
            phrases     (rf/subscribe [::model/phrases])
            stopped     (rf/subscribe [::model/stopped])
            suggestions (rf/subscribe [::model/suggestions])]
        (let [q (some-> params :q)]
          (search-phrase q))
        (fn []
          [:div.search-container
           [:div.search-content
            ^{:key (str "video-list- " @current)}
            [:div.video-player-container
             {:on-click toggle-play}
             (doall
              (for [x     @phrases
                    :let  [{:keys [index id]} x]
                    :when (<= @current index (inc @current))]
                ^{:key (str "phrase-" index "-" id)}
                [player/video-player {:phrase         x
                                      :hide?          (not= @current index)
                                      :on-play        #(do
                                                         #_(rf/dispatch [::model/stopped false])
                                                         (scroll-to-phrase index))
                                      :on-pause       #(rf/dispatch [::model/stopped true])
                                      :on-end         next-phrase
                                      :on-pos-changed update-current-word
                                      :stopped?       @stopped}]))
             [:div.inline-logo
              [:span.red "Play"]
              [:span.black "Phrase"]
              [:span.gray ".me"]]
             ]
            [:div.search-ui-container [search-input]]
            (if-not (empty? @suggestions)
              [:div.suggestions-container (str @suggestions)]
              [:div#search-result.search-results-container
               {:on-scroll #(on-phrases-scroll %)}
               [:table.table.table-hover.phrase-table.borderless
                [:tbody
                 (doall
                  (for [x @phrases]
                    ^{:key (str "phrase-" x)}
                    [phrase-text x]))]]])]])))}))

