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
            [malcontent.middleware :refer [add-content-security-policy]]
            [ring.middleware.gzip :refer :all]
            [clojure.java.io :as io]
            [playphraseme.common.util :as util]
            [clojure.tools.logging :as log]
            [ring.middleware.cors :refer [wrap-cors]]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
   (-> #'api-routes
       wrap-gzip
       (wrap-cors :access-control-allow-origin [#".*"]
                  :access-control-allow-methods [:get :put :post :delete]))
   (-> #'home-routes
       (wrap-routes middleware/wrap-csrf)
       (wrap-routes middleware/wrap-formats)
       middleware/wrap-base
       #_(add-content-security-policy :config-path (io/resource "policy.edn")))
   (route/not-found
    (:body
     (error-page {:status 404
                  :title  "page not found"})))))

(defn app [] #'app-routes)



