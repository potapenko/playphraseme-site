(ns playphraseme.api.routes.config
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [compojure.api.sweet :refer :all]
            [playphraseme.api.queries.config :as config]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn- get-config-by-id [id]
  (config/get-config id))

(def configs-routes
  "Specify routes for Mobile Configs"
  (context "/api/v1/configs" []
    :tags ["Configs"]

    (GET "/"            [request]
         :tags          ["Configs"]
         :query-params  [id :- s/Str]
         :return        s/Any
         :middleware    [cors-mw]
         :summary       "Return config value by id"
         (ok
          (get-config-by-id id)))))


