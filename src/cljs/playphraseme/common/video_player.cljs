(ns playphraseme.common.video-player
  (:require [clojure.string :as string]
            [cljs-await.core :refer [await]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [playphraseme.common.ui :as ui :refer [flexer spacer]]
            [playphraseme.common.util :as util]
            [clojure.data :refer [diff]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn- extract-props [argv]
  #_(reagent.impl.util/extract-props argv))

(defn- index->id [index]
  (str "video-player-" index))

(defn- index->element [index]
  (-> index index->id js/document.getElementById))

(defn- add-video-listener [index event-name cb]
  (when cb
    (-> index index->element (.addEventListener event-name cb))))

(defn ended? [index]
  (when-let [el (index->element index)]
    (-> el .-ended)))

(defn playing? [index]
  (when-let [el (index->element index)]
    #_(println
     {:current-time (-> el .-currentTime)
      :paused       (-> el .-paused)
      :ended        (-> el .-ended)
      :ready-state  (-> el .-readyState)})
    (and (pos? (-> el .-currentTime))
         (not (-> el .-paused))
         (not (-> el .-ended))
         (> (-> el .-readyState) 2))))

(defn jump [index position]
  (let [el (some-> index index->element)]
    (when el
      (aset el "currentTime" (/ position 1000)))))

(defn jump-and-play [index position]
  (println ">>> jump and play:" index position)
  (let [el (some-> index index->element)]
    (when el
      (aset el "currentTime" (/ position 1000))
      (-> el .play (.catch #())))))

(defn stop [index]
  (println ">>> stop:" index)
  (some-> index index->element .pause))

(def play-count (atom 0))

(defn play [index]
  (println ">>> play:" index)
  (let [success (r/atom false)
        c       (swap! play-count inc)]
    (when-let [el (some-> index index->element)]
      (when-not (playing? index)
        (when (ended? index)
          (jump index 0))
        (-> el .play
            (.then (fn []
                     (println ">>>> autoplay works!")
                     (reset! success true)
                     (rf/dispatch [:playing true])
                     (rf/dispatch [:autoplay-enabled true])))
            (.catch (fn [e]
                      (when (->> e .-message (re-find #"pause") nil?)
                        (println "error play video:" e)
                        (reset! success false)))))
        (go
          (<! (timeout 1000))
          (when-not @success
            (rf/dispatch [:playing false])
            (rf/dispatch [:autoplay-enabled false])))))))

(defn enable-inline-video [index]
  (-> index index->element js/enableInlineVideo))

(defn video-player [{:keys [hide? stopped? phrase
                            on-load on-pause on-play on-load-start
                            on-end on-pos-changed on-error]}]
  (let [{:keys [index]} phrase]
   (r/create-class
    {:component-will-receive-props
     (fn [this])
     :component-did-mount
     (fn []
       (enable-inline-video index)
       (add-video-listener index "loadstart" on-load-start)
       (add-video-listener index "play" on-play)
       (add-video-listener index "error" (fn [e]
                                           (println ">>>> errorr!")
                                           (on-error e)))
       (add-video-listener index "stalled" (fn [e]
                                             (println ">>> stalled!")
                                             (on-error e)))
       (add-video-listener index "abort" (fn [e]
                                           (println ">>> abort!")
                                           (on-error e)))
       (add-video-listener index "pause" #(when (playing? index) on-pause))
       (add-video-listener index "ended" on-end)
       (add-video-listener index "timeupdate"
                           #(on-pos-changed
                             (-> %
                                 .-target .-currentTime
                                 (* 1000) js/Math.round)))
       (add-video-listener index "canplaythrough" on-load))
     :reagent-render
     (fn [{:keys [hide? stopped? phrase
                  on-load on-pause on-play on-load-start
                  on-end on-pos-changed on-play-click]}]
       (let [{:keys [index video-info]} phrase]
         [:div.video-player-box.d-flex
          {:style    {:opacity (if hide? 0 1)}
           :on-click on-play-click}
          [:video.video-player
           {:src          (:video-url phrase)
            :auto-play    (and (not stopped?) (not hide?))
            :plays-inline true
            :controls     false
            :id           (index->id index)}]
          (when (and stopped? (not util/ios?))
            [:div.overlay-play-icon.d-flex.flex-column
             [:div.grow]
             [:div
              [:span.fa-stack.fa-1x
               [:i.fa.fa-circle.fa-stack-2x.fa-inverse]
               [:i.fa.fa-play.fa-stack-1x.play-icon2]]]
             [:div.grow]])
          (let [{:keys [imdb info]} video-info]
            [:a.overlay-video-info
             {:href (str "https://www.imdb.com/title/" imdb) :target "_blank"}
             (if-not stopped?
               (string/replace info #"\s*\[.+\]" "")
               info)])]))})))

