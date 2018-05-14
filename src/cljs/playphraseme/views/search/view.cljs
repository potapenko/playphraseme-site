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
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.video-player :as player]
            [playphraseme.views.favorites.view :as favorites-page]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.ga :as ga]
            [playphraseme.common.nlp :as nlp])
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

(defn search-phrase
  ([text] (search-phrase text nil))
  ([text first-phrase]
   (when text
     (when (or
            first-phrase
            (not= (some-> text string/trim)
                  (some-> @(rf/subscribe [:search-text]) string/trim)))
       (util/set-url! "search" (merge {:q text} (when first-phrase {:p first-phrase})))
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
               (rf/dispatch-sync [::model/search-result
                                  (if first-phrase
                                    (if-let [first-phrase-info (<! (rest-api/get-phrase first-phrase))]
                                      (update res :phrases
                                              (fn [ex]
                                                (if first-phrase-info
                                                  (concat [first-phrase-info] ex)
                                                  ex)))
                                      res)
                                    res)])
               (load-favorited)))))))
   (rf/dispatch [:search-text text])))

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
  (when-let [elem (js/document.getElementById (str "phrase-text-" index))]
    (-> elem (.scrollIntoView #js{:behavior "smooth" :block "start"}))))

(defn load-favorited []
  (when (rest-api/authorized?)
   (when-let [{:keys [id] :as phrase} (get-current-phrase)]
     (when-not (contains? phrase :favorited)
       (go
         (let [favorited (-> (<! (rest-api/get-favorite id)) nil? not)]
           (rf/dispatch [::model/favorite-phrase id (-> favorited)])))))))

(defn next-phrase []
  (rf/dispatch-sync [::model/next-phrase])
  (load-favorited)
  (let [current @(rf/subscribe [:current-phrase-index])
        loaded  (count @(rf/subscribe [::model/phrases]))
        text @(rf/subscribe [:search-text])]
    (scroll-to-phrase current)
    (util/set-url! "search" {:q text :p (:id (get-current-phrase))})
    (when (-> current (+ 5) (> loaded))
      (scroll-end))))

(defn prev-phrase []
  (rf/dispatch-sync [::model/prev-phrase])
  (load-favorited)
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
        13 (toggle-play) ;; enter
        nil))))

(defn favorite-current-phrase [e]
  (-> e .preventDefault)
  (if-not (rest-api/authorized?)
    (util/go-url! "/#/login")
    (let [{:keys [id favorited]} (get-current-phrase)]
      (rf/dispatch [::model/favorite-phrase id (not favorited)])
      (go
        (if-not favorited
          (<! (rest-api/add-favorite id))
          (<! (rest-api/delete-favorite id)))
        (favorites-page/reload)))))

(defn play-button []
  [:div.filter-input-icon
   {:on-click toggle-play}
   [:span.fa-stack
    (if (or @(rf/subscribe [:stopped])
            (false? @(rf/subscribe [:autoplay-enabled])))
      [:i.material-icons "play_circle_filled"]
      [:i.material-icons "pause_circle_filled"])]])

(defn search-input []
  [:div.filters-container
   [:input#search-input.filter-input.form-control.input-lg
    {:type      "text" :placeholder "Search Phrase"
     :value     @(rf/subscribe [:search-text])
     :on-change #(search-phrase (-> % .-target .-value))}]
   [:ul.filter-input-icons
    [:li [:div.search-result-count @(rf/subscribe [::model/search-count])]]
    (when-not resp/mobile?
      [:li
       [play-button]])]])

(defn goto-word [e phrase-index word-index]
  (-> e .preventDefault)
  (let [{:keys [index] :as phrase} (nth @(rf/subscribe [::model/phrases]) phrase-index)
        word (-> phrase :words (nth word-index))]
    (rf/dispatch [:stopped false])
    (rf/dispatch-sync [::model/current-word-index] (:index phrase))
    (player/jump index (+ 400 (:start word)))
    (highlite-word word)
    (player/play index)))

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
  (let-sub [::model/current-word-index]
           (fn []
             [:div.karaoke
              (for [w    words
                    :let [{:keys [formated-text text index searched]} w]]
                ^{:key (str "word-" index)}
                [:a.s-word {:href     ""
                            :on-click #(goto-word % phrase-index index)
                            :id       (str "word-" index)
                            :class    (util/class->str
                                       (when searched "s-word-searched")
                                       (when (= index current-word-index) "s-word-played"))}
                 formated-text])])))

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
   {:on-scroll #(util/on-scroll-end % scroll-end)}
   [:table.table.phrase-table.borderless
    {:class (util/class->str (when-not resp/mobile? "table-hover"))}
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

(defn page [{:keys [q p]}]
  (let-sub [:current-phrase-index
            ::model/phrases
            :stopped
            ::model/suggestions
            :mobile?]
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
        (let [lang (util/locale-name)]
          (search-phrase q p)
          (fn []
            [:div.search-container
             ^{:key (str "video-list- " @current-phrase-index)}
             [:div.video-player-container
              (doall
               (for [x     @phrases
                     :let  [{:keys [index id]} x]
                     :when (<= @current-phrase-index index (inc @current-phrase-index))]
                 ^{:key (str "phrase-" index "-" id)}
                 [player/video-player {:phrase         x
                                       :hide?          (not= @current-phrase-index index)
                                       :on-play        (fn []
                                                         (scroll-to-phrase index))
                                       :on-pause       (fn []
                                                         (rf/dispatch [:stopped true]))
                                       :on-play-click  toggle-play
                                       :on-end         (fn []
                                                         (rf/dispatch [:playing false])
                                                         (next-phrase))
                                       :on-pos-changed update-current-word
                                       :stopped?       @stopped}]))
              [:div.video-overlay {:class (util/class->str (when @stopped :stopped))}
               [:ul.video-overlay-menu
                [:li
                 {:on-click favorite-current-phrase}
                 [:i.material-icons (if (:favorited (get-current-phrase))
                                      "favorite"
                                      "favorite_border")]
                 [:div.info-text "Favorite"]
                 [:div.info-text "This Phrase"]]
                [:li
                 {:on-click #(util/go-url! "/#/favorites")}
                 [:i.material-icons "featured_play_list"]
                 [:div.info-text "Favorited"]
                 [:div.info-text "Pharases"]]
                [:li
                 {:on-click #(util/go-url! "/#/history")}
                 [:i.material-icons "search"]
                 [:div.info-text "Search"]
                 [:div.info-text "History"]]
                [:li
                 {:on-click #(util/go-url! "/#/learn")}
                 [:i.material-icons "school"]
                 [:div.info-text "Learn"]
                 [:div.info-text "Phrases"]]
                [:li
                 {:on-click #(util/go-url! "/#/settings")}
                 [:i.material-icons "settings"]
                 [:div.info-text "Your"]
                 [:div.info-text "Settings"]]
                [:li
                 {:on-click #(util/go-url!
                              (str "/api/v1/phrases/video-download?id="
                                   (:id (get-current-phrase)))
                              true)}
                 [:i.material-icons "file_download"]
                 [:div.info-text "Download"]
                 [:div.info-text "Video"]]]
               [:div.overlay-logo
                [:span.red "Play"]
                [:span.black "Phrase"]
                [:span.gray ".me"]]
               (when (resp/landscape?)
                 ^{:key [@(rf/subscribe [:layout])]}
                 [overlay-current-phrase])]]
             [:div.search-ui-container
              [search-input]]
             (if-not (empty? @suggestions)
               [suggestions-list @suggestions]
               [search-results-list @phrases])
             (when (and resp/mobile?
                        (not @(rf/subscribe [:playing])))
               [:div.overlay-play-icon-bottom
                [play-button]])])))})))



