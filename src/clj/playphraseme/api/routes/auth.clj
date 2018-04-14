(ns playphraseme.api.routes.auth
  (:require [playphraseme.api.middleware.basic-auth :refer [basic-auth-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.route-functions.auth.get-auth-credentials :refer [auth-credentials-response credentials-response]]
            [playphraseme.api.route-functions.auth.link-auth-tokens :refer :all]
            [playphraseme.api.route-functions.auth.facebook-auth :as facebook]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))

(def auth-routes
  "Specify routes for Authentication functions"
  (context "/api/v1/auth" []

           (GET "/"             {:as request}
           :tags          ["Auth"]
           :return        {:id String :name String :permissions [String] :token String :refresh-token String}
           :header-params [authorization :- String]
           :middleware    [basic-auth-mw cors-mw authenticated-mw]
           :summary       "Returns auth info given a name and password in the '`Authorization`' header."
           :description   "Authorization header expects '`Basic name:password`' where `name:password`
                           is base64 encoded. To adhere to basic auth standards we have to use a field called
                           `name` however we will accept a valid name or email as a value for this key."
           (auth-credentials-response request))

     (GET "/link"        []
           :tags         ["Auth"]
           :query-params [email :- s/Str]
           :middleware   [cors-mw]
           :return       {:link-token String}
           :summary      "Returns a token to be used for authorization via an emailed link, associated with the given email"
           (link-token-generate-response email))

     (GET "/verify-email/:link-token" []
           :tags        ["Auth"]
           :path-params [link-token :- String]
           :middleware  [cors-mw]
           :return      {:id String :name String :permissions [String] :token String :refresh-token String}
           :summary     "Authorizes and returns auth info given a temporary link token."
           (verify-email-response link-token))

     (GET "/session"     request
          :tags          ["Auth"]
          :return        {:id String :name String :permissions [String] :token String}
          :header-params [authorization :- String]
          :middleware    [token-auth-mw cors-mw authenticated-mw]
          :summary       "Get Session info"
          :description   "Authorization header expects the following format 'Token {token}'"
          (credentials-response request))

     (GET "/facebook-callback" []
          :tags          ["Auth"]
          :query-params  [code :- s/Str]
          :summary       "Facebook auth API callback"
          (facebook/facebook-auth-callback-response code))

     (GET "/facebook" []
          :tags          ["Auth"]
          :return        {:id String :name String :permissions [String] :token String :refresh-token String}
          :summary       "Facebook auth"
          (facebook/facebook-auth-response))))
