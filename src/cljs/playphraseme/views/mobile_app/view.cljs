(ns playphraseme.views.mobile-app.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.ui :as ui :refer [flexer spacer]]
            [playphraseme.common.shared :as shared]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.common.responsive :as responsive]
            [playphraseme.views.mobile-app.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn modal-image [{:keys [image horizontal? index]}]
  (let [src (str "/img/mobile-app/" image ".png")]
    [:img {:src      src
           :on-click (fn []
                       (rf/dispatch [::model/modal-img-src src])
                       (rf/dispatch [::model/modal-img-horzontal? horizontal?]))
           :class    (util/class->str
                      (if horizontal?
                        "horizontal-modal-image"
                        "vertical-modal-image"))}]))

(defn page []
  (let-sub [::model/modal-img-src
            ::model/modal-img-horzontal?
            :layout
            :mobile?]
   (r/create-class
    {:component-did-mount
     (fn [])
     :reagent-render
     (fn []
       [:div.page-container.mobile-app-page
        [:h1
         #_[:div
            [:span.logo
             [:span.red "Play"]
             [:span.black "Phrase"]
             [:span.gray ".me"]]]
         "Mobile App for IOS is out!"
         [:div.android-coming-soon "(Android app is coming soon)"]]
        [spacer 18]
        [:h5 "Mobile app have all goodies of site plus some unique features."]
        [spacer 18]
        [:h2 "Key features of mobile application:"]
        [:ol
         [:li "Fast and native mobile interface"]
         [:li "Advanced phrases search"]
         [:li "Offline"]
         [:li "Playlists"]
         [:li "Play phrases in background"]
         [:li "Playlists sharing"]]
        [:h2 "Screenshots:"]
        [:ul
         [:li "Advanced search with " [:span.futures-app "common phrases:"]
          [:div.modal-image-containter
           [modal-image {:image "search"}]
           [modal-image {:image "search-suggestions"}]
           [modal-image {:image "player"}]
           [modal-image {:image "video-5" :horizontal? true}]
           [modal-image {:image "video-3" :horizontal? true}]]]
         [:li "Auto-generated playlists for listening common phrases for more than " [:span.futures-app "1000 days:"] ""
          [:div.modal-image-containter
           [modal-image {:image "phrases-of-the-day"}]
           [modal-image {:image "phrases-of-the-day-downloading"}]
           [modal-image {:image "select-day-53"}]
           [modal-image {:image "select-day-1085"}]]]
         [:li [:span.futures-app2 "Custom playlists " ]
          "- save interesting phrases and words to playlists for listening and studying:"
          [:div.modal-image-containter
           [modal-image {:image "playlists"}]
           [modal-image {:image "current-playlist"}]
           [modal-image {:image "playlists-create-playlist"}]
           [modal-image {:image "playlists-word-add"}]]]
         [:li [:span.futures-app "Share"] " playlists to the web and " [:span.futures-app "import"] " them into your mobile app."
          [:div.modal-image-containter
           [modal-image {:image "playlist-share-1"}]
           [modal-image {:image "playlist-share-2"}]
           [modal-image {:image "playlist-share-3"}]
           [modal-image {:image "playlist-share-4"}]
           [modal-image {:image "playlist-share-5"}]]]
         [:li "Listen to playlists in headphones for a walk or in transport. Playlists can be listened to with a "
          [:span.futures-app  "locked screen"] "."
          [:div.modal-image-containter
           [modal-image {:image "play-background"}]]]
         [:li "Download playlists " [:span.futures-app "offline" ] " and listen to them without access to the Internet!"
          [:div.modal-image-containter
           [modal-image {:image "offline-1"}]
           [modal-image {:image "offline-2"}]
           [modal-image {:image "offline-3" :horizontal? true}]]]]
        [spacer 18]
        [:h2 "View key features in action:"]
        [spacer 18]
        [:iframe
         {:allowfullscreen "allowfullscreen",
          :allow
          "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture",
          :frameborder     "0",
          :src             "https://www.youtube.com/embed/pPwxHrUeIGM",
          :height          "315",
          :width           "560"}]
        [spacer 18]
        [:h2 "Get mobile app:"]
        [spacer 32]
        [:div.install-buttons
         [:a {:href "https://itunes.apple.com/app/playphraseme/id1441967668" :target "_blank"}
          [:img.app-button {:src "./img/apple-store-button@1x.png"}]]
         [ui/spacer 32]
         #_[:a {:href ""}
            [:img.app-button {:src "./img/google-play-button@1x.png"}]]
         [:div.android-coming-soon "(Android app is coming soon)"]]
        [spacer 32]
        [:div.modal
         (when @modal-img-src
           {:style    {:display "block"}
            :on-click #(rf/dispatch [::model/modal-img-src nil])})
         [:span.close  "×"]
         (let [style (->
                      (if @modal-img-horzontal?
                        (let [h (-> @layout :width (* 0.46))]
                         {:width         (-> @layout :width)
                          :height        h
                          :margin-top    (-> @layout :height (- h) (/ 2))})
                        {:width         (-> @layout :height (* 0.46))
                         :height        (-> @layout :height)
                         :margin-top    0
                         :margin-bottom 0})
                      (update :width #(/ % (:scale @layout)))
                      (update :margin-top #(/ % (:scale @layout)))
                      (update :height #(/ % (:scale @layout))))]
           [:img.modal-content {:src @modal-img-src :style style}])
         [:div.caption]]])})))

(comment

  (js/require "react-image-lightbox")

  )
