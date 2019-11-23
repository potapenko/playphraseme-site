(ns playphraseme.api.routes.permission
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.permission.add-user-permission :refer [add-user-permission-response]]
            [playphraseme.api.route-functions.permission.delete-user-permission :refer [delete-user-permission-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))


(def permission-routes
  "Specify routes for User Permission-related functions"
  (context "/api/v1/permission/user/:id" []
    :tags          ["Permission"]
    :path-params   [id :- String]
    :body-params   [permission :- String]
    :header-params [authorization :- String]
    :return        {:message String}
    :middleware    [token-auth-mw cors-mw authenticated-mw]
    :description   "Authorization header expects the following format 'Token {token}'"

    (POST "/" request
      :summary "Adds the specified permission for the specified user. Requires token to have `admin` auth."
      (add-user-permission-response request id permission))

    (DELETE "/" request
      :summary "Deletes the specified permission for the specified user. Requires token to have `admin` auth."
      (delete-user-permission-response request id permission))))
