(ns playphraseme.app.routes
  (:require [playphraseme.app.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [playphraseme.app.config :refer [env]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home.html"
                {:facebook-app-id (:facebook-client-id env)}))

(defroutes home-routes
  (GET "/" []
       (home-page)))

