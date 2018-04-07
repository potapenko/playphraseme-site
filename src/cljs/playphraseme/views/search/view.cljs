(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.video-player :as player]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.nlp :as nlp])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn play []
  (player/play @(rf/subscribe [:current-phrase-index])))

(defn toggle-play []
  (if (util/ios?)
    (play)
    (let [now-stopped? @(rf/subscribe [::model/stopped])
          index @(rf/subscribe [:current-phrase-index])]
      (if now-stopped?
        (player/play index)
        (player/stop index))
      (rf/dispatch [::model/stopped (not now-stopped?)]))))

(def search-id (atom 0))
(def scroll-loaded (atom 0))

(defn search-phrase [text]
  (when text
    (when-not (= (some-> text string/trim)
                 (some-> @(rf/subscribe [:search-text]) string/trim))
     (util/set-url! "search" {:q text})
     (let [id  (swap! search-id inc)]
       (rf/dispatch [::model/search-result []])
       (rf/dispatch [:current-phrase-index nil])
       (go
         (reset! scroll-loaded 0)
         (let [res (<! (rest-api/search-phrase text 10 0))]
           (println text id @search-id)
           (when (= id @search-id)
             (rf/dispatch [::model/search-result res])))))))
  (rf/dispatch [:search-text text]))

(defn scroll-end []
  (let [count-all @(rf/subscribe [::model/search-count])
        skip      (-> @(rf/subscribe [::model/phrases]) count)]
    (when (< skip count-all)
      (go
        (when-not (= @scroll-loaded skip)
         (reset! scroll-loaded skip)
         (let [res (<! (rest-api/search-phrase
                        @(rf/subscribe [:search-text])
                        10 skip))]
           (rf/dispatch [::model/search-result-append res])))))))

(defn get-current-phrase-index []
  @(rf/subscribe [:current-phrase-index]))

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
  (println "next-phrase")
  (rf/dispatch-sync [::model/next-phrase])
  (let [current @(rf/subscribe [:current-phrase-index])
        loaded  (count @(rf/subscribe [::model/phrases]))]
    (scroll-to-phrase current)
    (when (-> current (+ 5) (> loaded))
      (scroll-end))))

(defn prev-phrase []
  (rf/dispatch-sync [::model/prev-phrase])
  (let [current @(rf/subscribe [:current-phrase-index])]
    (scroll-to-phrase current)))

(defn highlite-word [current-word]
  (doseq [x (:words (get-current-phrase))]
    (some-> x :index (->> (str "word-")) util/id->elem (util/remove-class "s-word-played")))
  (some-> current-word :index (->> (str "word-")) util/id->elem (util/add-class "s-word-played")))

(defn update-current-word [pos]
  (let [current-word (some->> (get-current-phrase) :words (filter #(-> % :start (< pos))) last)]
    (when current-word
      (highlite-word current-word))))

(defn scroll-to-suggestion [index]
  (when-let [elem (js/document.getElementById (str "suggestion-" index))]
    (-> elem (.scrollIntoView #js{:behavior "smooth" :block "start"}))))

(defn next-suggestion []
  (rf/dispatch-sync [::model/next-suggestion])
  (let [index @(rf/subscribe [::model/current-suggestion-index])]
    (when-not (nil? index)
      (scroll-to-suggestion index))))

(defn prev-suggestion []
  (rf/dispatch-sync [::model/prev-suggestion])
  (let [index @(rf/subscribe [::model/current-suggestion-index])]
    (when-not (nil? index)
      (scroll-to-suggestion index))))

(defn goto-suggestion []
  (let [index @(rf/subscribe [::model/current-suggestion-index])]
    (when-not (nil? index)
      (let [current (nth @(rf/subscribe [::model/suggestions]) index)]
        (search-phrase (:text current))))))

(defn focus-input []
  (when-let [elem (some-> "search-input" js/document.getElementById)]
    (-> elem .focus)))

(defn work-with-keys [e]
  (let [key-code    (-> e .-keyCode)
        suggestions @(rf/subscribe [::model/suggestions])]
    ;; (println "key:" key-code)
    (if-not (empty? suggestions)
      (case key-code
        38 (prev-suggestion) ;; up
        40 (next-suggestion) ;; down
        13 (goto-suggestion) ;; enter
        32 (goto-suggestion) ;; space
        27 (focus-input) ;; esc
        nil)
      (case key-code
        38 (prev-phrase) ;; up
        40 (next-phrase) ;; down
        27 (focus-input) ;; esc
        ;; 13 (toggle-play) ;; enter
        nil))))

(defn favorite-current-phrase [])
(defn show-config [])
(defn download-video [])
(defn show-search-help [])
(defn favorite-phrase [id])

(defn search-input []
  [:div.filters-container
   [:input#search-input.filter-input.form-control.input-lg
    {:type      "text" :placeholder "Search Phrase"
     :value     @(rf/subscribe [:search-text])
     :on-change #(search-phrase (-> % .-target .-value))}]
   [:ul.filter-input-icons
    [:li [:div.search-result-count @(rf/subscribe [::model/search-count])]]
    (when-not (util/ios?)
      [:li
       [:div.filter-input-icon
        {:on-click toggle-play}
        [:span.fa-stack.fa-1x
         [:i.fa.fa-circle.fa-stack-2x]
         (if @(rf/subscribe [::model/stopped])
           [:i.fa.fa-play.fa-stack-1x.fa-inverse.play-icon]
           [:i.fa.fa-pause.fa-stack-1x.fa-inverse.pause-icon])]]])
    #_[:li
       [:div.filter-input-icon
        {:on-click favorite-current-phrase}
        [:span.fa-stack.fa-1x
         [:i.fa.fa-circle.fa-stack-2x]
         [:i.fa.fa-star.fa-stack-1x.fa-inverse]]]]
    #_[:li
       [:div.filter-input-icon
        {:on-click show-config}
        [:i.fa.fa-cog.fa-2x]]]
    #_[:li
       [:div.filter-input-icon
        {:on-click show-search-help}
        [:i.fa.fa-question-circle.fa-2x]]]]])

(defn goto-word [e phrase-index word-index]
  (-> e .preventDefault)
  (let [phrase (nth @(rf/subscribe [::model/phrases]) phrase-index)
        word (-> phrase :words (nth word-index))]
    (rf/dispatch-sync [::model/current-word-index] (:index phrase))
    (player/jump (:index phrase) (+ 400 (:start word)))
    (highlite-word word)
    (player/play (:index phrase))))

(defn get-searched-words [current-words]
  (let [text             @(rf/subscribe [:search-text])
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
        current-index                 (rf/subscribe [:current-phrase-index])]
    (fn []
      (if (= @current-index index)
        [karaoke-words-current index words]
        [karaoke-words index words]))))

(defn phrase-text [x]
  (r/create-class
   {:reagent-render
    (fn []
      (let [lang                    (util/locale-name)
            {:keys [index text id]} x]
        (fn []
          [:tr {:id       (str "phrase-text-" index)
                :on-click #(rf/dispatch [:current-phrase-index (:index x)])}
           [:td [:div.phrase-number (-> x :index inc)]]
           [:td.phrase-text [karaoke x]]])))}))

(defn suggestions-list [list]
  (let-sub [::model/current-suggestion-index]
    (fn []
      [:div.suggestions-container
       (doall
        (for [{:keys [text count index]} list]
          ^{:key (str "suggestion-" index)}
          [:a.suggestion
           {:id    (str "suggestion-" index)
            :class (when (= index @current-suggestion-index) "higlited")
            :href  (str "/#/search?q=" text)}
           [:div.text text]
           [:div.grow]
           [:div.counter (str count)]]))])))

(defn search-results-list [phrases]
  [:div#search-result.search-results-container
   {:on-scroll #(on-phrases-scroll %)}
   [:table.table.table-hover.phrase-table.borderless
    [:tbody
     (doall
      (for [x phrases]
        ^{:key (str "phrase-" x)}
        [phrase-text x]))]]])

(defn overlay-current-phrase []
  [:div.currrent-pharase-container-landscape
   [:div.phrase-text
    (when-let [phrase (get-current-phrase)]
      [karaoke phrase])]])

(defn page [params]
  (let-sub [:current-phrase-index
            ::model/phrases
            ::model/stopped
            ::model/suggestions]
    (r/create-class
     {:component-will-unmount
      (fn [this]
        (util/remove-document-listener "keyup" work-with-keys))
      :component-did-mount
      (fn [this]
        (util/add-document-listener "keyup" work-with-keys)
        (focus-input))
      :reagent-render
      (fn []
        (let [lang (util/locale-name)
              ]
          (let [q (some-> params :q)]
            (search-phrase q))
          (fn []
            [:div.search-container
             [:div.search-content
              ^{:key (str "video-list- " @current-phrase-index)}
              [:div.video-player-container
               (doall
                (for [x     @phrases
                      :let  [{:keys [index id]} x]
                      :when (<= @current-phrase-index index (inc @current-phrase-index))]
                  ^{:key (str "phrase-" index "-" id)}
                  [player/video-player {:phrase         x
                                        :hide?          (not= @current-phrase-index index)
                                        :on-play        #(scroll-to-phrase index)
                                        :on-pause       #(rf/dispatch [::model/stopped true])
                                        :on-play-click  toggle-play
                                        :on-end         next-phrase
                                        :on-pos-changed update-current-word
                                        :stopped?       @stopped}]))
               [:div.video-overlay {:class (util/class->str (when @stopped :stopped))}
                [:ul.video-overlay-menu
                 (when (util/fullscreen-enabled?)
                   [:li
                    {:on-click #(util/toggle-fullscreen! (util/selector ".search-container"))}
                    (if @(rf/subscribe [:fullscreen])
                      [:i.material-icons "fullscreen_exit"]
                      [:i.material-icons "fullscreen"])
                    [:div.info-text "Fullscreen"]])
                 [:li
                  [:i.material-icons "favorite_border"]
                  [:div.info-text "Favorites"]]
                 [:li
                  [:i.material-icons "school"]
                  [:div.info-text "Learn"]]
                 [:li
                  [:i.material-icons "settings"]
                  [:div.info-text "Settings"]]
                 [:li
                  [:i.material-icons "file_download"]
                  [:div.info-text "Download"]]]
                [:div.overlay-logo
                 [:span.red "Play"]
                 [:span.black "Phrase"]
                 [:span.gray ".me"]]
                (when @(rf/subscribe [:mobile?])
                  [overlay-current-phrase])]
               ]
              [:div.search-ui-container [search-input]]
              (if-not (empty? @suggestions)
                [suggestions-list @suggestions]
                [search-results-list @phrases])]])))})))

