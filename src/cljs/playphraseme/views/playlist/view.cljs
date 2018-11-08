(ns playphraseme.views.playlist.view
  (:require [playphraseme.common.shared :as shared]
            [cljs.core.async :refer [<! put! chan] :as async]
            [re-frame.core :as rf]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.common.ui :as ui :refer [spacer flexer]]
            [playphraseme.views.playlist.model :as model]
            [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [re-frame-macros.core :as mcr :refer [let-sub]]))


(defn phrase [index {:keys [text into]}]
  [:li.list-group-item.mobile-red
   [:span {:style {:font-size "22px"
                   :margin-right "12px"}} (str (inc index) ".")] (str "\"" text "\"")])

(defn page []
  (let-sub [:params
            ::model/playlist]
   (r/create-class
    {:component-did-mount
     (fn []
       (go
         (let [playlist-id (:playlist @params)
               playlist    (<! (rest-api/get-playlist playlist-id))]
           (rf/dispatch [::model/playlist playlist]))))
     :reagent-render
     (fn []
       (let [{:keys [title info phrases]} @playlist
             app-url (str "playphraseme://playlist/" (:playlist @params))]
        [:div.page-container
         [:h1.mobile-red title]
         [:a.mobile-red {:href app-url :style {:text-decoration :underline}}
          "Open playlist in mobile app"]
         [spacer 16]
         [:ul.list-group
          (->> phrases
               (map-indexed
                (fn [index x]
                  [phrase index x]))
               doall)]]))})))

