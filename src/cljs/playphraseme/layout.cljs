(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.common.ui :as ui]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.common.localization :refer [ls]]
            [playphraseme.model]))

(defn header-button [label]
  [:a.header-button {:href ""} label])

(defn header []
  [:div.header
   [:div.top
    [header-button (ls :navigation.login.register)]
    [header-button (ls :navigation.guest.tour)]
    [ui/grow]
    [header-button (ls :navigation.support)]
    [ui/grow]
    [header-button "Facebook"]
    [header-button "Like!"]]
   [:div.bottom
    [:div.logo
     [:span.red "Play"]
     [:span.black "Phrase"]
     [:span.gray ".me"]]
    [:div.phrases-statistic
     [:span.count "254,000"]
     [:span.info (ls :statistic.phrases)]]
    [:div.translate-direction "EN>EN"]]])

(defn left-column []
  [:div.left-column "left-column"])

(defn right-column []
  [:div.right-column "right column"])

(defn root [current-page]
  [:div.layout-container
   [left-column]
   [:div.layout-main
    [header]
    [:div.current-page-container current-page]]
   [right-column]])
