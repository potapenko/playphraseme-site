(ns playphraseme.api.routes.user
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.user.create-user :refer [create-user-response]]
            [playphraseme.api.route-functions.user.delete-user :refer [delete-user-response]]
            [playphraseme.api.route-functions.user.modify-user :refer [modify-user-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))


(def user-routes
  "Specify routes for User functions"
  (context "/api/v1/user" []
           :tags ["User"]

    (POST "/"           {:as request}
           :return      {:name String}
           :middleware  [cors-mw]
           :body-params [email :- String name :- String password :- String]
           :summary     "Create a new user with provided name, email and password."
           (create-user-response email name password))

    (DELETE "/:id"          {:as request}
             :path-params   [id :- String]
             :return        {:message String}
             :header-params [authorization :- String]
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Deletes the specified user. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (delete-user-response request id))

    (PATCH  "/:id"          {:as request}
             :path-params   [id :- String]
             :body-params   [{name :- String ""} {password :- String ""} {email :- String ""}]
             :header-params [authorization :- String]
             :return        {:id String :email String :name String}
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (modify-user-response request id name password email))))
