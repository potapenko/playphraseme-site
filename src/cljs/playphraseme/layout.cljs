(ns playphraseme.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.model]))

(defn page []
  [:div "site layout"])
