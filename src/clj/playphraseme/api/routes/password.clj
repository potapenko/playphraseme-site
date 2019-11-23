(ns playphraseme.api.routes.password
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.password.password-reset :refer [password-reset-response]]
            [playphraseme.api.route-functions.password.password-set :refer [password-set-response]]
            [playphraseme.api.route-functions.password.request-password-reset :refer [request-password-reset-response]]
            [compojure.api.sweet :refer :all]))

(def password-routes
  "Specify routes for User Password functions"
  (context "/api/v1/password" []
           :tags       ["Password"]
           :return     {:message String}
           :middleware [cors-mw]

    (POST "/reset-request" []
           :body-params [user-email         :- String]
           :summary     "Request a password reset for the registered user with the matching email"
           :description "The reset key will be valid for 24 hours after creation."
           (request-password-reset-response user-email))

    (POST "/reset-confirm" []
           :body-params [reset-key    :- String
                         new-password :- String]
           :summary     "Replace an existing user password with the newPassword given a valid resetKey"
           (password-reset-response reset-key new-password))

    (POST "/password-set" {:as request}
           :body-params   [new-password :- String]
           :header-params [authorization :- String]
           :middleware    [token-auth-mw cors-mw authenticated-mw]
           :summary       "Set a new password for the current user to newPassword"
           (password-set-response request new-password))))
