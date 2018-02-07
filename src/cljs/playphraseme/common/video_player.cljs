(ns playphraseme.common.video-player
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [playphraseme.common.util :as util]
            [clojure.data :refer [diff]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn- extract-props [argv]
  #_(reagent.impl.util/extract-props argv))

(defn index->id [index]
  (str "video-player-" index))

(defn stop [index]
  (println "stop")
  )

(defn play [index]
  (println "play")
  )

(def cdn-url "https://cdn.playphrase.me/phrases/")

(defn video-player [{:keys [phrase hide? stopped? position]}]
  (r/create-class
   {:component-will-receive-props
    (fn [this]
      (let [{:keys [hide? stopped? phrase] :as props} (r/props this)
            {:keys [index]} phrase
            playing?        (and (not hide?) (not stopped?))]
        (if playing?
          (stop index)
          (play index))))
    :component-did-mount
    (fn [this]
      (let [{:keys [hide? stopped? phrase] :as props} (r/props this)]
        (when-not stopped?
          (play index))))
    :reagent-render
    (fn []
      [:div.video-player-box
       (when hide? {:display :none})
       [:video.video-player
        {:src   (str cdn-url (:movie phrase) "/" (:id phrase) ".mp4")
         :id    (index->id (:number phrase))
         :style {:z-index (:index phrase)}}]])}))

