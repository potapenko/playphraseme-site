(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.common.ui :as ui]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.common.localization :refer [ls]]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.model]))

(defn header-button
  ([label href icon-class]
   [:a.header-button {:href href} [:i {:class icon-class}] " " label])
  ([label href]
   [:a.header-button {:href href} label]))

(defn header []
  [:div.header
   {:class (util/class->str (when-not (= @(rf/subscribe [:page]) :search) "invert"))}
   [:div.top
    [header-button (ls :navigation.login.register) "/#/login" "fas fa-user-circle"]
    #_[header-button (ls :navigation.guest.tour) "/#/guest-tour" "far fa-question-circle"]
    [header-button (ls :navigation.support) "/#/support" "far fa-envelope"]
    #_[header-button "Favorites" "/#/favorites" "fas fa-heart"]
    [ui/flexer]
    [header-button "Github" "" "fab fa-github-square"]
    [header-button  "Facebook" "" "fab fa-facebook"]
    [header-button "Like!" "" "far fa-thumbs-up"]]
   [:div.bottom
    [:div.logo {:on-click (fn [e]
                            (if (-> e .-altKey)
                              (phrases/search-random-bad-phrase)
                              (phrases/search-random-phrase)))}
     [:span.red "Play"]
     [:span.black "Phrase"]
     [:span.gray ".me"]]
    [ui/flexer]
    [:div.statistic
     [:span.count "322"]
     [:span.info (ls :statistic.movies)]]
    [ui/spacer 10]
    [:div.statistic
     [:span.count "254,000"]
     [:span.info (ls :statistic.phrases)]]
    #_[:div.translate-direction
     [:span.select-button "En"] [:span.arrow ">"] [:span.select-button "En"]]]])

(defn left-column []
  [:div.left-column ""])

(defn right-column []
  [:div.right-column ""])

(defn root [current-page]
  [:div.layout-container
   {:style {:zoom @(rf/subscribe [:responsive-scale])}}
   (when @(rf/subscribe [:responsive-show-left-column?])
    [left-column])
   [:div.layout-main
    [header]
    [:div.current-page-container current-page]]
   (when @(rf/subscribe [:responsive-show-right-column?])
     [right-column])])
