(ns playphraseme.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [playphraseme.common.route :as route]
            [markdown.core :refer [md->html]]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.model])
  (:import goog.History))

(def pages
  {:search  #'search-page/page})

(defn page []
  [:div
   [@(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (route/goto-page! :search))

(secretary/defroute "/search" []
  (route/goto-page! :search))

(secretary/defroute "/phrase" []
  (route/goto-page! :phrase))

(secretary/defroute "/register" []
  (route/goto-page! :register))

(secretary/defroute "/login" []
  (route/goto-page! :login))

(secretary/defroute "*" []
  (route/goto-page! :not-found))

(secretary/defroute "/article" []
  (route/goto-page! :article))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (hook-browser-navigation!)
  (mount-components))
