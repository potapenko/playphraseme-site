(ns playphraseme.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [playphraseme.common.route :as route]
            [playphraseme.common.util :as util :refer [or-str]]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.views.login.view :as login-page]
            [playphraseme.views.not-found.view :as not-found-page]
            [playphraseme.views.register.view :as register-page]
            [playphraseme.views.reset-password.view :as reset-password-page]
            [playphraseme.views.phrase.view :as phrase-page]
            [playphraseme.views.article.view :as articles]
            [playphraseme.views.support.view :as support-page]
            [playphraseme.views.history.view :as history-page]
            [playphraseme.views.favorites.view :as favorites-page]
            [playphraseme.views.settings.view :as settings-page]
            [playphraseme.views.learn.view :as learn-page]
            [playphraseme.layout :as layout]
            [playphraseme.model]
            [playphraseme.common.responsive :as responsive]
            [playphraseme.common.phrases :as phrases]
            [playphraseme.common.rest-api :as rest-api])
  (:import goog.History))

(def pages
  {:search         #'search-page/page
   :login          #'login-page/page
   :register       #'register-page/page
   :reset-password #'reset-password-page/page
   :not-found      #'not-found-page/page
   :guest-tour     #'articles/guest-tour
   :phrase         #'phrase-page/page
   :support        #'support-page/page
   :history        #'history-page/page
   :favorites      #'favorites-page/page
   :settings       #'settings-page/page
   :learn          #'learn-page/page})

(defn page []
  (let [page-id @(rf/subscribe [:page])
        params  @(rf/subscribe [:params])]
    [layout/root
     ^{:key [page-id params]}
     [(-> pages page-id) params]]))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (util/go-url! "/#/search"))

(secretary/defroute "/search" [query-params]
  (let [{:keys [q p]} query-params]
    (println ">>>>" q p)
   (route/goto-page! :search (merge
                              query-params
                              (when-not p
                                {:q (or-str
                                     q
                                     @(rf/subscribe [:search-text])
                                     (phrases/random-phrase))})))))


(secretary/defroute "/phrase" []
  (route/goto-page! :phrase))

(secretary/defroute "/register" []
  (route/goto-page! :register))

(secretary/defroute "/reset-password" []
  (route/goto-page! :reset-password))

(secretary/defroute "/login" []
  (route/goto-page! :login))

(secretary/defroute "/logout" []
  (rest-api/logout)
  (util/go-url! "/#/"))

(secretary/defroute "/auth" [query-params]
  (let [{:keys [auth-token]} query-params]
   (rest-api/auth auth-token)
   (util/go-url! "/#/search")))

(secretary/defroute "/article" []
  (route/goto-page! :article))

(secretary/defroute "/support" []
  (route/goto-page! :support))

(secretary/defroute "/history" []
  (route/goto-page! :history))

(secretary/defroute "/favorites" []
  (route/goto-page! :favorites))

(secretary/defroute "/learn" []
  (route/goto-page! :learn))

(secretary/defroute "/settings" []
  (route/goto-page! :settings))

(secretary/defroute "/guest-tour" []
  (route/goto-page! :guest-tour))

(secretary/defroute "*" []
  (route/goto-page! :not-found))

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
  (responsive/start)
  (rest-api/authorize!)
  (mount-components))
