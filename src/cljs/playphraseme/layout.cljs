(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.common.ui :as ui]
            [playphraseme.common.config :as config]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.common.localization :refer [ls]]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.rest-api :as rest-api]
            [playphraseme.model]
            [playphraseme.common.responsive :as resp])
  (:require-macros
   [re-frame-macros.core :as mcr :refer [let-sub]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn header-button [label href & [icon-class]]
  [:div.d-flex.flex-column.grow.px-4.text-center
   [:div.grow]
   [:a.d-inline-block.header-button
    {:href href}
    (when icon-class
      [:i {:class icon-class}])
    " " label]
   [:div.grow]])

(defn facebook-like-button []
  #_(let-sub [scale :responsive-scale]
           [:div.fb-like
            {:style (resp/fb-button-css @scale)
             :data-share      "false"
             :data-show-faces "false"
             :data-size       "small"
             :data-width      "100"
             :data-action     "like"
             :data-layout     "button"
             :data-href       "https://www.facebook.com/playphrase/"}]))

(defn header []
  (let-sub [:page
            :all-movies-count
            :all-phrases-count
            :mobile?]
    (go
      (rf/dispatch [:all-phrases-count (<! (rest-api/count-all-phrases))])
      #_(rf/dispatch [:all-movies-count (<! (rest-api/count-all-movies))]))
    (fn []
      [:div.header.d-flex.invert.px-2
       ;; {:class (util/class->str (when-not (= @page :search) "invert"))}
       [:div.logo (when-not config/disable-search?
                    {:on-click (fn [e]
                                 (if (-> e .-altKey)
                                   (phrases/search-random-bad-phrase)
                                   (phrases/search-random-phrase)))})
        [:span.red "Play"]
        [:span.black "Phrase"]
        [:span.gray ".me"]]
       [:div.d-flex.header-buttons-line.grow.mt-2
        (when-not (= @page :search)
          [header-button "Home" "/#/" "fas fa-home"])
        #_(when-not (= @page :login)
            (if (rest-api/authorized?)
              [header-button
               (str (ls :navigation.logout) " (" (:name @(rf/subscribe [:auth-data])) ")")
               "/#/logout" "fas fa-user-circle"]
              [header-button (ls :navigation.login.register) "/#/login" "fas fa-user-circle"]))
        (when-not @mobile?
          [header-button (ls :navigation.support) "/#/support" "far fa-envelope"])
        (when-not @mobile?
          [header-button "Github"
           "https://github.com/potapenko/playphraseme-site" "fab fa-github-square"])
        (when-not @mobile?
          [header-button "Youtube"
           "https://www.youtube.com/channel/UCD_uvkY4IcFDEkZ3fgYhFWA" "fab fa-youtube"])
        (when-not @mobile?
          [header-button  "Facebook"
           "https://www.facebook.com/playphrase/" "fab fa-facebook"])]
       ^{:key "fixed-key"}
       [facebook-like-button]
       (when @mobile?
         [ui/spacer 6])
       [:div.mobile-apps.ml-3
        [:a.pr-2
         (if (-> @page (= :mobile-app))
           {:href "https://itunes.apple.com/app/playphraseme/id1441967668"
            :target "_blank"}
           {:href "/#/mobile-app"})
         [:img.app-button {:src    "/img/apple-store-button@1x.png"}]]
        [ui/spacer 12]
        [:a
         (if (-> @page (= :mobile-app))
           {:href "https://play.google.com/store/apps/details?id=com.playphrasemewalk"
            :target "_blank"}
           {:href "/#/mobile-app"})
         [:img.app-button {:src    "/img/google-play-button@1x.png"}]]]
       (when-not @mobile?
         [:div.statistic.ml-3
          [:span.count @all-phrases-count]
          [:span.info (ls :statistic.phrases)]])])))

(defn left-column []
  [:div.left-column ""])

(defn right-column []
  [:div.right-column ""])

(defn root []
  (let-sub [scale :responsive-scale
            :page]
    (r/create-class
     {:component-did-mount
      (fn []
        (when resp/ios?
         (util/add-class (util/selector "body") "ios")))
      :reagent-render
      (fn [current-page]
        [:div
         {:style (merge
                  #_(resp/zoom-css @scale))
          :class (util/class->str
                  (when resp/ios? :ios)
                  (when resp/android? :android)
                  (when resp/safari? :safari)
                  (when resp/chrome? :chrome))}
         [:div.mobile-query]
         [:div
          ;; {:style (resp/container-height-css @scale)}
          [header]
          [:div.d-flex.grow.position-relative
           current-page]]])})))

