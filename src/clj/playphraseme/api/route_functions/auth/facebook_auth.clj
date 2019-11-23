(ns playphraseme.api.route-functions.auth.facebook-auth
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parser]
            [clojure.pprint :refer [pprint]]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [playphraseme.app.config :refer [env]]
            [playphraseme.api.route-functions.user.create-user :refer [create-new-user]]
            [playphraseme.api.general-functions.user.create-token :refer [create-token]]
            [playphraseme.api.queries.user.registered-user :as users]
            [clojure.tools.logging :as log]))

(defn facebook-auth-response []
  (resp/redirect
   (:uri (let [{:keys [facebook-callback-uri facebook-client-id facebook-client-secret]} env]
           (oauth2/make-auth-request
            {:authorization-uri  "https://graph.facebook.com/oauth/authorize"
             :access-token-uri   "https://graph.facebook.com/oauth/access_token"
             :redirect-uri       facebook-callback-uri
             :client-id          facebook-client-id
             :client-secret      facebook-client-secret
             :access-query-param :access_token
             :scope              ["email"]
             :profileFields      ["emails" "name"]
             :grant-type         "authorization_code"})))))

(defn facebook-auth-callback-response [code]
  (let [{:keys [facebook-callback-uri
                facebook-client-id
                facebook-client-secret]} env

        access-token (-> (client/get "https://graph.facebook.com/oauth/access_token"
                                     {:query-params {:client_id     facebook-client-id
                                                     :redirect_uri  facebook-callback-uri
                                                     :client_secret facebook-client-secret
                                                     :code          code}})
                         :body (parser/parse-string keyword) :access_token)
        user-details (-> (client/get "https://graph.facebook.com/me"
                                     {:query-params {:access_token access-token
                                                     :fields       "id,name,email"}})
                         :body (parser/parse-string keyword))]

    (let [{:keys [id email name]} user-details]
      (when-not (users/get-registered-user-by-email email)
        (create-new-user email name (str (java.util.UUID/randomUUID))))

      (let [user  (users/get-registered-user-by-email email)
            token (create-token user)]
        (resp/redirect (str "/#/auth?auth-token=" token))))))

