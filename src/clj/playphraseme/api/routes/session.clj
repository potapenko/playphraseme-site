(ns playphraseme.api.routes.session
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.auth.get-auth-credentials :refer [credentials-response]]
            [schema.core :as s]
            [ring.util.http-response :as respond]
            [compojure.api.sweet :refer :all]))


(def session-routes
  "Specify routes for User functions"
  (context "/api/v1/session" []
           :tags ["Session"]

  (GET "/"           request
      :tags          ["Session"]
      :return        {:id String :name String :permissions [String] :token String}
      :header-params [authorization :- String]
      :middleware    [token-auth-mw cors-mw authenticated-mw]
      :summary       "Get Session info"
      :description   "Authorization header expects the following format 'Token {token}'"
      (credentials-response request))))
