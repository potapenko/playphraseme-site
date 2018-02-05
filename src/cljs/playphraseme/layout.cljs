(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.common.ui :as ui]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.common.localization :refer [ls]]
            [playphraseme.model]))

(defn header-button [label href]
  [:a.header-button {:href href} label])

(defn header []
  [:div.header
   [:div.top
    [header-button (ls :navigation.login.register) "/#/register"]
    [header-button (ls :navigation.guest.tour) "/#/guest-tour"]
    [header-button (ls :navigation.support) "/#/support"]
    [header-button "Public API" "/#/api"]
    [ui/grow]
    [header-button "Facebook" ""]
    [header-button "Like!" ""]]
   [:div.bottom
    [:div.logo
     [:span.red "Play"]
     [:span.black "Phrase"]
     [:span.gray ".me"]]
    [:div.statistic
     [:span.count "254,000"]
     [:span.info (ls :statistic.phrases)]]
    [:div.translate-direction
     [:span.select-button "En"] [:span.arrow ">"] [:span.select-button "En"]]]])

(defn left-column []
  [:div.left-column ""])

(defn right-column []
  [:div.right-column ""])

(defn root [current-page]
  [:div.layout-container
   {:style {:zoom @(rf/subscribe [:responsive-scale])}}
   [left-column]
   [:div.layout-main
    [header]
    [:div.current-page-container current-page]]
   [right-column]])
