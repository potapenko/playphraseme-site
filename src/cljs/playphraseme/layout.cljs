(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.model]))

(defn header []
  [:div.header
   [:div.top "top"]
   [:div.bottom "bottom"]
   ])

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
