(ns playphraseme.app.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [compojure.route :as route]
            [mount.core :as mount]
            [playphraseme.api.handler :refer [api-routes]]
            [playphraseme.app.config :refer [env]]
            [playphraseme.app.layout :refer [error-page]]
            [playphraseme.app.middleware :as middleware]
            [playphraseme.app.routes :refer [home-routes]]
            [playphraseme.env :refer [defaults]]
            [ring.middleware.gzip :refer :all]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(defn authenticated? [name pass]
  (and (= name (-> env :home-page-basic-login :name))
       (= pass (-> env :home-page-basic-login :pass))))

(def app-routes
  (routes
   (-> #'api-routes wrap-gzip)
   (-> #'home-routes
       (wrap-routes middleware/wrap-csrf)
       (wrap-routes middleware/wrap-formats)
       middleware/wrap-base)
   (route/not-found
    (:body
     (error-page {:status 404
                  :title  "page not found"})))))

(defn app [] #'app-routes)
