(ns playphraseme.common.video-player
  (:require [clojure.string :as string]
            [cljs-await.core :refer [await]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
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
    (and (pos? (-> el .-currentTime))
         (not (-> el .-paused))
         (not (-> el .-ended))
         (> (-> el .-readyState) 2))))

(defn jump [index position]
  (let [el (some-> index index->element)]
    (when el
      (aset el "currentTime" (/ position 1000)))))

(defn stop [index]
  (some-> index index->element .pause))

(defn play [index]
  (when-let [el (some-> index index->element)]
    (when-not (playing? index)
      (when (ended? index)
        (jump index 0))
      (-> el .play
          (.then (fn []
                   (rf/dispatch [:autoplay-enabled true])))
          (.catch (fn [e]
                    (go
                      (<! (timeout 1000))
                      (when-not @(rf/subscribe [:autoplay-enabled])
                        (rf/dispatch [:autoplay-enabled false])))))))))

(defn enable-inline-video [index]
  (-> index index->element js/enableInlineVideo))

(defn video-player []
  (r/create-class
   {:component-will-receive-props
    (fn [this]
      (let [{:keys [hide? stopped? phrase]} (r/props this)
            {:keys [index]}                 phrase
            playing?                        (and (not hide?) (not stopped?))]
        (add-video-listener index "canplaythrough"
                            (if playing?
                              #(play index)
                              #(stop index)))))
    :component-did-mount
    (fn [this]
      (let [{:keys [hide? stopped? phrase
                    on-load on-pause on-play on-load-start
                    on-end on-pos-changed]} (r/props this)
            index                           (:index phrase)
            autoplay                        (not (or stopped? hide?))]

        (enable-inline-video index)
        (add-video-listener index "loadstart" on-load-start)
        (add-video-listener index "play" on-play)
        (add-video-listener index "pause" #(when (playing? index) on-pause))
        (add-video-listener index "ended" on-end)
        (add-video-listener index "timeupdate"
                            #(on-pos-changed
                              (-> %
                                  .-target .-currentTime
                                  (* 1000) js/Math.round)))
        (add-video-listener index "canplaythrough" on-load)
        (jump index 0)
        (if autoplay
          (play index))))
    :reagent-render
    (fn [{:keys [phrase hide? stopped? mobile on-play-click]}]
      (let [{:keys [index video_info]} phrase]
        [:div.video-player-box
         {:style    (merge {:opacity (if hide? 0 1)}
                           (when hide? {:display :none}))
          :on-click on-play-click}
         [:video.video-player
          {:src          (:video-url phrase)
           :plays-inline true
           :controls     false
           :id           (index->id index)}]
         (when (false? @(rf/subscribe [:autoplay-enabled]))
           [:div.overlay-play-icon
            [:span.fa-stack.fa-1x
             [:i.fa.fa-circle.fa-stack-2x]
             [:i.fa.fa-play.fa-stack-1x.fa-inverse.play-icon2]]
            (when @(rf/subscribe [:desktop?])
              [:div
               [:div.auto-play-info-1 "Autoplay disabled"]
               [:div.auto-play-info "Enable Auto-Play for our site"]
               [:div.auto-play-info "in your browser settings"]])])
         (let [{:keys [imdb info]} video_info]
           [:a.overlay-video-info
            {:href imdb :target "_blank"}
            info])]))}))

