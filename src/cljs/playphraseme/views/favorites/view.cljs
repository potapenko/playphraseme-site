(ns playphraseme.views.favorites.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [playphraseme.common.util :as util]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.ui :as ui :refer [spacer flexer]]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.favorites.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(def scroll-loaded (atom -1))

(defn load-favotites-part []
  (let [skip (-> @(rf/subscribe [::model/favorites-list]) count)]
    (go
      (when-not (= @scroll-loaded skip)
        (reset! scroll-loaded skip)
        (let [res (<! (rest-api/favorites 30 skip))]
          (rf/dispatch [::model/favorites-append res]))))))

(defn reload []
  (reset! scroll-loaded -1)
  (rf/dispatch-sync [::model/favorites-list []])
  (load-favotites-part))

(defn elements-list []
  (let-sub [::model/favorites-list]
    [:div.elements-container
     (doall
      (for [{:keys [deleted phrase text index movieInfo]} @favorites-list]
        ^{:key (str "element-" index)}
        [:div.element
         [:div.counter (-> index inc str)]
         [spacer 22]
         [:div
          [:a.text {:href  (str "/#/search?q=" text)}
           (str "\"" text "\"")]
          [:div.video-info movieInfo]]
         [:div.grow]
         (if-not deleted
           [:div.icon
            {:on-click (fn []
                         (rf/dispatch [::model/favorite-mark-delete phrase true])
                         (rest-api/delete-favorite phrase))}
            [:i.material-icons "close"]]
           [:div.icon
            {:on-click (fn []
                         (rf/dispatch [::model/favorite-mark-delete phrase false])
                         (rest-api/add-favorite phrase))}
            [:i.material-icons "favorite_border"]])
         ]))]))

(defn page []
  (r/create-class
   {:component-did-mount
    (fn []
      (reload))
    :reagent-render
    (fn []
      [:div.page-container
       {:on-scroll #(util/on-scroll-end % load-favotites-part)}
       [:h1 "Favorited Phrases"]
       [elements-list]])}))
