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
            [playphraseme.views.mobile-app.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn page []
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
       [:h2 "Key features of mobile application:"]
       [spacer 18]
       [:ul
        [:li "Search result with " [:span.futures-app "common phrases"] " of the English language."]
        [:li "Auto-generated playlists for listening common phrases for more than " [:span.futures-app "1000 days!"] ""]
        [:li [:span.futures-app2 "Custom playlists " ]
         "- save interesting phrases and words to playlists for listening and studying."]
        [:li [:span.futures-app "Share"] " playlists to the web and " [:span.futures-app "import"] " them into your mobile app."]
        [:li "Listen to playlists in headphones for a walk or in transport. Playlists can be listened to with a "
         [:span.futures-app  "locked screen"] "."]
        [:li "Download playlists " [:span.futures-app "offline" ] " and listen to them without access to the Internet!"]]
       [spacer 18]
       [:h2 "View key features in action:"]
       [spacer 18]
       [:iframe
        {:allowfullscreen "allowfullscreen",
         :allow
         "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture",
         :frameborder "0",
         :src "https://www.youtube.com/embed/pPwxHrUeIGM",
         :height "315",
         :width "560"}]
       [spacer 18]
       [:h2 "Get mobile app:"]
       [spacer 32]
       [:div.install-buttons
        [:a {:href ""}
         [:img.app-button {:src "./img/apple-store-button@1x.png"}]]
        [ui/spacer 32]
        #_[:a {:href ""}
           [:img.app-button {:src "./img/google-play-button@1x.png"}]]
        [:div.android-coming-soon "(Android app is coming soon)"]]
       [spacer 32]])}))
