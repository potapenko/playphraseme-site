(ns playphraseme.views.search.view
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.crypt.base64 :as base-64]
            [playphraseme.common.util :as util]
            [playphraseme.common.responsive :as resp]
            [playphraseme.views.search.model :as model]
            [playphraseme.common.ui :as ui :refer [spacer flexer]]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.video-player :as player]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.ga :as ga]
            [playphraseme.views.search.ctrl :as ctlr]
            [playphraseme.common.nlp :as nlp]
            [cljs.pprint :as pp]
            [playphraseme.common.shared :as shared])
  (:import goog.async.Debouncer)
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))


(declare load-favorited)

(defn play []
  (rf/dispatch [:stopped false])
  (player/play @(rf/subscribe [:current-phrase-index])))

(defn toggle-play []
  (let [index @(rf/subscribe [:current-phrase-index])]
    (if (false? @(rf/subscribe [:autoplay-enabled]))
      (player/play index)
      (let [now-stopped? @(rf/subscribe [:stopped])]
        (if now-stopped?
          (player/play index)
          (player/stop index))
        (rf/dispatch [:stopped (not now-stopped?)])))))

(def search-id (atom 0))
(def scroll-loaded (atom 0))

(defn load-common-phrases [text]
  (go
    (let [common-phrases (<! (rest-api/common-phrases text))]
      (rf/dispatch [::model/common-phrases common-phrases]))))

(defn search-phrase
  ([text] (search-phrase text nil))
  ([text first-phrase]
   (println ">>> search:" text)
   (when-let [text (some-> text
                      (string/replace #"\s+" " ")
                      (string/replace #"^\s+" ""))]
    (when (or
           first-phrase
           (not= (some-> text string/trim)
                 (some-> @(rf/subscribe [:search-text]) string/trim)))
      (let [id  (swap! search-id inc)]
        (rf/dispatch [::model/search-result []])
        (rf/dispatch [:current-phrase-index nil])
        (go
          (reset! scroll-loaded 0)
          (let [res (<! (rest-api/search-phrase text 10 0))]
            ;; (ga/track (str "/#/search=q=" text))
            (when (= id @search-id)
              (when (= id @search-id)
                (when (and
                       @(rf/subscribe [:first-search])
                       (pos? @(rf/subscribe [:search-count])))
                  (rf/dispatch [:first-search false])
                  (rf/dispatch [:stopped false])))
              (rf/dispatch [:search-count])
              (when (-> res :count pos?)
                (when-not first-phrase
                  (util/lazy-call
                   #(util/set-history-url!
                     "search" (merge {:q text}
                                     #_(when first-phrase {:p first-phrase}))))))
              (rf/dispatch [::model/search-result res])
              (load-favorited)
              (load-common-phrases text))))))
    (rf/dispatch [:search-text text]))))

(def search-debouncer
  (new Debouncer
       (fn [text]
         (search-phrase text)) 300))


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

(defn get-current-phrase []
  (let-sub [::model/phrases
            :current-phrase-index]
           (some->> @phrases
                    (drop-while #(-> % :index (not= @current-phrase-index)))
                    first)))

(defn scroll-to-phrase [index]
  ;; legacy
  #_(when-let [elem (js/document.getElementById (str "phrase-text-" index))]
    (-> elem (.scrollIntoView #js{:behavior "smooth" :block "start"}))))

(defn load-favorited []
  ;; legacy
  #_(when (rest-api/authorized?)
    (when-let [{:keys [id] :as phrase} (get-current-phrase)]
      (when-not (contains? phrase :favorited)
        (go
          (let [favorited (-> (<! (rest-api/get-favorite id)) nil? not)]
            (rf/dispatch [::model/favorite-phrase id (-> favorited)])))))))

(defn play-phrase [change-fn]
  (let-sub [::model/phrases
            :current-phrase-index
            :search-text]
   (let [loaded  (count @phrases)
         current (max
                  0
                  (if (-> @current-phrase-index change-fn (= loaded))
                    0 (change-fn @current-phrase-index)))]
     (util/set-url! "search" {:q @search-text #_:p #_(:id (get-current-phrase))})
     (when (-> current (+ 5) (> loaded))
       (scroll-end))
     (player/jump-and-play current 0))))

(defn next-phrase []
  (let-sub [:search-text]
   (play-phrase inc)
   (rf/dispatch [::model/next-phrase])))

(defn prev-phrase []
  (rf/dispatch [::model/prev-phrase])
  (play-phrase dec))

(defn highlite-word [current-word]
  (doseq [x (:words (get-current-phrase))]
    (some-> x :index (->> (str "word-")) util/id->elem (util/remove-class "s-word-played")))
  (some-> current-word :index (->> (str "word-")) util/id->elem (util/add-class "s-word-played")))

(defn update-current-word [pos]
  (let [current-word (some->> (get-current-phrase)
                              :words
                              (filter
                               (fn [{:keys [start end left-padding]
                                     :or   {left-padding 150}}]
                                 (let [start (-> start (- left-padding))
                                       end   (-> end (- left-padding))]
                                   (< start pos end))))
                              last)]
    (when current-word
      (highlite-word current-word))))

(defn scroll-to-suggestion [index]
  (when-let [elem (js/document.getElementById (str "suggestion-" index))]
    (-> elem (.scrollIntoView #js{:behavior "smooth" :block "start"}))))

(defn next-suggestion []
  (rf/dispatch [::model/next-suggestion])
  (let [index @(rf/subscribe [::model/current-suggestion-index])]
    (when-not (nil? index)
      (scroll-to-suggestion index))))

(defn prev-suggestion []
  (rf/dispatch [::model/prev-suggestion])
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

(defn set-input-cursor []
  (when-let [elem (some-> "search-input" js/document.getElementById)]
    (-> elem .-selectionStart (set! (-> elem .-value count)))))

(defn get-input-cursor []
  (when-let [elem (some-> "search-input" js/document.getElementById)]
    (-> elem .-selectionStart)))

(defn next-word-search [e]
  (let-sub [::model/next-word-suggestion
            ::model/input-focused?]
           (when (and @next-word-suggestion
                      @input-focused?)
             (-> e .preventDefault)
             (search-phrase @next-word-suggestion))))

(defn go-next-word-suggestion []
  (let-sub [::model/suggestions
            ::model/next-word-suggestion
            :search-text]
    (when (and (= (get-input-cursor)
                  (count @search-text))
           (not (empty? @suggestions)))
      (let [current-index (loop [[v & t] @suggestions
                                 i       0]
                            (when v
                              (if (= (:text v) @next-word-suggestion)
                                i
                                (recur t (inc i)))))
            next-index    (let [i (inc current-index)]
                            (if (>= i (count @suggestions))
                              0
                              i))
            next-word     (-> @suggestions (get next-index) :text)]
        (when next-word
          (rf/dispatch [::model/next-word-suggestion next-word]))))))

(defn work-with-keys-down [e]
  (let [key-code    (-> e .-keyCode)
        suggestions @(rf/subscribe [::model/suggestions])]
    ;; (println "key-down:" key-code)
    (case key-code
      9 (next-word-search e)
      nil)))

(defn prevent-call [e f]
  (-> e .preventDefault)
  (f))

(defn show-suggestions-list? []
  (let-sub [::model/suggestions
            ::model/phrases]
   (and
    (not (empty? @suggestions))
    (empty? @phrases))))

(defn work-with-keys-up [e]
  (let [key-code    (-> e .-keyCode)
        suggestions @(rf/subscribe [::model/suggestions])]
    (when @(rf/subscribe [::model/input-focused?])
     (case key-code
       39 (go-next-word-suggestion)
       nil))
    (if (show-suggestions-list?)
      (case key-code
        38 (prevent-call e prev-suggestion) ;; up
        40 (prevent-call e next-suggestion) ;; down
        13 (prevent-call e goto-suggestion) ;; enter
        32 (prevent-call e goto-suggestion) ;; space
        27 (prevent-call e focus-input) ;; esc
        nil)
      (case key-code
        38 (prevent-call e prev-phrase) ;; up
        40 (prevent-call e next-phrase) ;; down
        27 (prevent-call e focus-input) ;; esc
        13 (prevent-call e toggle-play) ;; enter
        nil))))

(defn favorite-current-phrase [e]
  (-> e .preventDefault)
  #_(if-not (rest-api/authorized?)
    (util/go-url! "/#/login")
    (let [{:keys [id favorited]} (get-current-phrase)]
      (rf/dispatch [::model/favorite-phrase id (not favorited)])
      (go
        (if-not favorited
          (<! (rest-api/add-favorite id))
          (<! (rest-api/delete-favorite id)))
        (favorites-page/reload)))))

(defn- input-button [icon on-click]
  [:div.play-icon
   {:on-click toggle-play}
   [:i.material-icons icon]])

(defn- play-button []
  [input-button
   (if (or
        resp/ios?
        @(rf/subscribe [:stopped])
        (false? @(rf/subscribe [:autoplay-enabled])))
     "play_circle_filled"
     "pause_circle_filled")
   toggle-play])

(defn- update-music-volume []
  (let-sub [:audio-volume
            :audio-muted]
           (let [audio-muted (or @audio-muted @(rf/subscribe [:stopped]))]
             (some-> (util/selector "#music-player")
                     (aset "volume" (if audio-muted 0 @audio-volume))))))

(defn search-input []
  (let-sub [::model/next-word-suggestion
            ::model/input-focused?
            :current-phrase-index
            ::model/search-count
            :search-text]
    (fn []
      (let [show-suggestion? (and
                              @next-word-suggestion
                              @input-focused?)]
       [:div.filters-container.grow
        [:input#search-input.filter-input.form-control.input-lg
         {:type            "text"
          :placeholder     "Search Phrase"
          ;; :style           (when-not show-suggestion? {:color :transparent})
          :value           @search-text
          :auto-correct    "off"
          :auto-complete   "off"
          :auto-capitalize "off"
          :on-focus        (fn []
                             (rf/dispatch [::model/input-focused? true])
                             (set-input-cursor))
          :on-blur         #(rf/dispatch [::model/input-focused? false])
          :on-change       #(search-phrase (-> % .-target .-value))}]
        (when show-suggestion?
          [:div.next-word-suggestion.no-select
           {:on-click focus-input}
           [:span {:style {:color :tranparent}}
            @search-text]
           [:span
            (string/replace-first
             (string/lower-case @next-word-suggestion)
             (string/lower-case @search-text) "")]])
        [:ul.filter-input-icons
         [:li
          [input-button "skip_previous" prev-phrase]]
         [:li
          [:div.search-result-count (str (inc @current-phrase-index) "/" @search-count)]]
         [:li
          [input-button "skip_next" next-phrase]]
         [:li
          [play-button]]]]))))

(defn goto-word [e phrase-index word-index]
  (-> e .preventDefault)
  (let [{:keys [index] :as phrase} (nth @(rf/subscribe [::model/phrases]) phrase-index)
        word (-> phrase :words (nth word-index))]
    (rf/dispatch [:stopped false])
    (rf/dispatch [::model/current-word-index] (:index phrase))
    (player/jump index (:start word))
    (highlite-word word)
    (player/play index)))

(defn copy-icon [text]
  [:div.copy-icon
   {:on-click    (fn [e]
                   (-> e .preventDefault)
                   (-> e .stopPropagation)
                   (js/copyToClipboard text))
    :data-toggle "tooltip"
    :title       "Copy phrase to clipboard"}
   [:i.material-icons
    {:style {:color     "rgba(0,0,0,0.5)"
             :margin    0
             :font-size "22px"
             :padding   0}}
    "content_copy"]])

(defn karaoke-words-current []
  (let-sub [::model/current-word-index]
    (fn [phrase-index words text]
      [:a.karaoke
       ;; {:href (util/make-phrase-url text)}
       (->> words
            (map-indexed
             (fn [idx {:keys [formated-text text index searched?]}]
               ^{:key idx}
               [:span.s-word
                {:on-click #(goto-word % phrase-index index)
                 :id       (str "word-" index)
                 :class    (util/class->str
                            (when searched? "s-word-searched")
                            (when (= index current-word-index) "s-word-played"))}
                formated-text]))
            doall)])))

(defn karaoke []
  (let-sub [:current-phrase-index
            :search-text]
    (fn [{:keys [words text id index]}]
      (let [nlp-words      (nlp/create-words text)
            words          (if (empty? words)
                             (->> nlp-words
                                  (map-indexed
                                   (fn [idx x]
                                     {:text  x
                                      :index idx})))
                             words)
            searched-words (set (ctlr/get-searched-words words @search-text))
            words          (map (fn [w1 w2]
                                  (assoc w1
                                         :formated-text w2
                                         :searched? (-> w1 searched-words nil? not)))
                                words nlp-words)]
       [karaoke-words-current index words text]))))

(defn suggestions-list [list]
  (let-sub [::model/current-suggestion-index]
    (fn []
      [:div.suggestions-container
       (->> list
            (map-indexed
             (fn [idx {:keys [text count] :as x}]
               ^{:key idx}
               [:a.suggestion
                {:id    (str "suggestion-" idx)
                 :class (when (= idx @current-suggestion-index) "higlited")
                 :href  (str "/#/search?q=" text)}
                [:div.text text]
                [:div.grow]
                [:div.counter (str count)]]))
            doall)])))

(defn overlay-current-phrase []
  (let-sub [::model/phrases
            :current-phrase-index]
    (fn []
      (when-let [phrase (some->> @phrases
                                 (drop-while #(-> % :index (not= @current-phrase-index)))
                                 first)]
       [:div.phrase-text.d-flex
        [:div.grow]
        [karaoke phrase]
        [:div.grow]]))))

(defn page [{:keys [q p]}]
  (let-sub [:current-phrase-index
            ::model/phrases
            :stopped
            ::model/suggestions
            :search-text
            :first-render
            :mobile?
            replay-counter (r/atom 0)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (when-not @(rf/subscribe [::model/inited])
          (util/add-document-listener "keyup" work-with-keys-up)
          (util/add-document-listener "keydown" work-with-keys-down)
          (rf/dispatch [::model/inited false]))
        (update-music-volume)
        (focus-input))
      :component-will-unmount
      (fn [this]
        (when @(rf/subscribe [::model/inited])
          (util/remove-document-listener "keyup" work-with-keys-up)
          (util/remove-document-listener "keydown" work-with-keys-down)
          (rf/dispatch [::model/inited true])))
      :component-did-update
      (fn []
        (if (and (not @first-render) @search-text (not (nil? @phrases)))
          (rf/dispatch [:first-render true])))
      :reagent-render
      (fn []
        (let [lang (util/locale-name)]
          (search-phrase q true)
          (fn []
            [:div.d-flex.flex-column.grow
             (if (show-suggestions-list?)
              [:div.search-bottom-container.grow
               [suggestions-list @suggestions]]
              [:div.video-player-container.grow
               (->> @phrases
                    (map-indexed
                     (fn [idx {:keys [index id] :as x}]
                       ^{:key (str "phrase-" idx "-" @replay-counter)}
                       [:div
                        (when (or
                               (<= @current-phrase-index idx (inc @current-phrase-index))
                               (= @current-phrase-index (dec (count @phrases))))
                          [:div
                           [player/video-player
                            {:phrase         x
                             :hide?          (not= @current-phrase-index index)
                             :on-play        #(scroll-to-phrase index)
                             :on-pause       #(rf/dispatch [:stopped true])
                             :on-error       next-phrase
                             :on-play-click  toggle-play
                             :on-end         (fn []
                                               (if (-> @phrases count (= 1))
                                                 (do
                                                   (swap! replay-counter inc)
                                                   (player/jump-and-play 0 index))
                                                 (next-phrase)))
                             :on-pos-changed update-current-word
                             :stopped?       @stopped}]])]))
                    doall)
               [:div.video-overlay
                {:class (util/class->str (when @stopped :stopped))}
                [:ul.video-overlay-menu
                 [:li
                  {:on-click #(util/go-url!
                               (str "/api/v1/phrases/video-download?id="
                                    (:id (get-current-phrase)))
                               true)}
                  [:i.material-icons "file_download"]
                  [:div.info-text "Download"]
                  [:div.info-text "Video"]]]
                [overlay-current-phrase]]])
             [:div.search-ui-container
              [search-input]]])))})))

