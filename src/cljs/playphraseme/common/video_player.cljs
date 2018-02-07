(ns playphraseme.common.video-player
  (:require [clojure.string :as string]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn index->id [index]
  (str "video-player-" index))

(def cdn-url "https://cdn.playphrase.me/phrases/")

(defn video-player [{:keys [phrase download? hide? played? position]}]
  (r/create-class
   {:should-component-update
    (fn [])
    :component-did-mount
    (fn [])
    :reagent-render
    (fn []
      (when download?
        [:div.video-player-box
         [:video.video-player
          {:src   (str cdn-url (:movie phrase) "/" (:id phrase) ".mp4")
           :id    (index->id (:number phrase))
           :style (merge
                   (when hide? {:display :none})
                   {:z-index index})}]]))}))

