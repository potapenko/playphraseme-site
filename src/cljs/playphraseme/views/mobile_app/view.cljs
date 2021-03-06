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

(defn modal-image [{:keys [image horizontal? index have-for-android?]}]
  (let [src (str "/img/mobile-app/" image
                 (when (and
                        have-for-android?
                        util/android?) "-android") ".png")]
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
         [:div.logo
          [:span.no-wrap
           [:span.red "Play"]
           [:span.black "Phrase"]
           [:span.gray ".me "]]
          [:span.no-wrap "Mobile App"]]]
        [:h2 {:style {:background-color "white"}}
         "The web version is now only available on desktop computers. On mobile devices "
         [:a (if (or util/ios? util/macos?)
               {:href "https://itunes.apple.com/app/playphraseme/id1441967668"}
               {:href "https://play.google.com/store/apps/details?id=com.playphrasemewalk"}) "install"]
         " our mobile app."]
        [:h2 "With mobile application you get everything from the web version plus:"]
        [:ul
         [:li "+ Fast and native mobile interface"]
         [:li "+ Advanced phrases search (with common phrases)"]
         [:li "+ Offline mode (play phrases without internet)"]
         [:li "+ \"Phrases of the day\" playlists (for 1000 days)"]
         [:li "+ Custom Playlists (for collecting words and phrases)"]
         (when (or util/ios? util/macos?)
          [:li "+ Play phrases in background"])
         [:li "+ Playlists sharing"]]
        [:h2 "Screenshots:"]
        [:ul
         [:li "Advanced search with " [:span.futures-app "common phrases:"]
          [:div.modal-image-containter
           [modal-image {:image "search" :have-for-android? true}]
           [modal-image {:image "search-suggestions" :have-for-android? true}]
           [modal-image {:image "player" :have-for-android? true}]
           [modal-image {:image "video-5" :horizontal? true}]
           [modal-image {:image "video-3" :horizontal? true}]]]
         [:li "Auto-generated playlists for listening common phrases for more than " [:span.futures-app "1000 days:"] ""
          [:div.modal-image-containter
           [modal-image {:image "phrases-of-the-day" :have-for-android? true}]
           [modal-image {:image "phrases-of-the-day-downloading" :have-for-android? true}]
           [modal-image {:image "select-day-53" :have-for-android? true}]
           [modal-image {:image "select-day-1085" :have-for-android? true}]]]
         [:li [:span.futures-app2 "Custom playlists " ]
          "- save interesting phrases and words to playlists for listening and studying:"
          [:div.modal-image-containter
           [modal-image {:image "playlists" :have-for-android? true}]
           [modal-image {:image "current-playlist"}]
           [modal-image {:image "playlists-create-playlist" :have-for-android? true}]
           [modal-image {:image "playlists-word-add" :have-for-android? true}]]]
         [:li [:span.futures-app "Share"] " playlists to the web and " [:span.futures-app "import"] " them into your mobile app."
          [:div.modal-image-containter
           [modal-image {:image "playlist-share-1"}]
           [modal-image {:image "playlist-share-2"}]
           [modal-image {:image "playlist-share-3"}]
           [modal-image {:image "playlist-share-4"}]
           [modal-image {:image "playlist-share-5"}]]]
         (when (or util/ios? util/macos?)
          [:li "Listen to playlists in headphones for a walk or in transport. Playlists can be listened to with a "
           [:span.futures-app  "locked screen"] "."
           [:div.modal-image-containter
            [modal-image {:image "play-background"}]]])
         [:li "Download playlists " [:span.futures-app "offline" ] " and listen to them without access to the Internet!"
          [:div.modal-image-containter
           (when (or util/ios? util/macos?)
            [modal-image {:image "offline-1"}])
           [modal-image {:image "offline-2"}]
           [modal-image {:image "offline-3" :horizontal? true}]]]

         [:li "Google " [:span.futures-app "translate"] " integration (long press on words/phrases to use)"
          [:div.modal-image-containter
           [modal-image {:image "translate-1"}]
           #_[modal-image {:image "translate-2"}]
           [modal-image {:image "translate-3" :horizontal? true}]
           #_[modal-image {:image "translate-4" :horizontal? true}]
           [modal-image {:image "translate-5" :horizontal? true}]
           #_[modal-image {:image "translate-6"}]
           [modal-image {:image "translate-7"}]
           #_[modal-image {:image "translate-8" :horizontal? true}]]]]
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
         [:a {:href "https://play.google.com/store/apps/details?id=com.playphrasemewalk" :target "_blank"}
          [:img.app-button {:src "./img/google-play-button@1x.png"}]]]
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
