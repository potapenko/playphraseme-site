(ns playphraseme.views.not-found.view
  (:require [reagent.core :as r]))

(defn page []
  (r/create-class
   {:component-did-mount
    (fn [])
    :reagent-render
    (fn []
      [:div.page-container
       [:h1 "Page not found."]])}))
