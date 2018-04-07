(ns playphraseme.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [playphraseme.common.route :as route]
            [playphraseme.common.util :as util]
            [playphraseme.views.search.view :as search-page]
            [playphraseme.views.login.view :as login-page]
            [playphraseme.views.not-found.view :as not-found-page]
            [playphraseme.views.register.view :as register-page]
            [playphraseme.views.reset-password.view :as reset-password-page]
            [playphraseme.views.phrase.view :as phrase-page]
            [playphraseme.views.article.view :as articles]
            [playphraseme.views.support.view :as support-page]
            [playphraseme.views.history.view :as history-page]
            [playphraseme.layout :as layout]
            [playphraseme.model]
            [playphraseme.common.responsive :as responsive]
            [playphraseme.common.phrases :as phrases])
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
   :history        #'history-page/page})

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
  (route/goto-page! :search (merge
                             {:q (phrases/random-phrase)}
                             query-params)))

(secretary/defroute "/phrase" []
  (route/goto-page! :phrase))

(secretary/defroute "/register" []
  (route/goto-page! :register))

(secretary/defroute "/reset-password" []
  (route/goto-page! :reset-password))

(secretary/defroute "/login" []
  (route/goto-page! :login))

(secretary/defroute "/auth" [query-params]
  ;; TODO store auth token
  (route/goto-page! :search))

(secretary/defroute "/article" []
  (route/goto-page! :article))

(secretary/defroute "/support" []
  (route/goto-page! :support))

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
  (mount-components))
