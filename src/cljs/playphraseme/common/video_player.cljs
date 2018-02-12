(ns playphraseme.common.video-player
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [playphraseme.common.util :as util]
            [clojure.data :refer [diff]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(def cdn-url "https://cdn.playphrase.me/phrases/")

(defn- extract-props [argv]
  #_(reagent.impl.util/extract-props argv))

(defn- index->id [index]
  (str "video-player-" index))

(defn- index->element [index]
  (-> index index->id js/document.getElementById))

(defn- add-video-listener [index event-name cb]
  (when cb
    (-> index index->element (.addEventListener event-name cb))))

(defn playing? [index]
  (when-let [el (index->element index)]
    (and (pos? (-> el .-currentTime))
         (not (-> el .-paused))
         (not (-> el .-ended))
         (> (-> el .-readyState) 2))))

(defn stop [index]
  (some-> index index->element .pause))

(defn play [index]
  (when-let [el (some-> index index->element)]
    (when-not (playing? index)
      (-> el .play))))

(defn jump [index position]
  (let [el (some-> index index->element)]
    (when el
      (aset el "currentTime" (/ position 1000)))))

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
                    on-load on-pause on-play
                    on-end on-pos-changed]} (r/props this)
            index                           (:index phrase)]
        (enable-inline-video index)
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
        (when-not (or stopped? hide?)
          (play index))))
    :reagent-render
    (fn [{:keys [phrase hide? stopped?]}]
      (let [index (:index phrase)]
        [:div.video-player-box
         {:style (merge {:opacity (if hide? 0 1)} (when hide? {:display :none}))}
         [:video.video-player
          {:src         (str cdn-url (:movie phrase) "/" (:id phrase) ".mp4")
           :playsInline true
           :controls    false
           :autoplay    true
           :id          (index->id index)
           :style       {:z-index (* index 1000)}}]
         ]))}))

