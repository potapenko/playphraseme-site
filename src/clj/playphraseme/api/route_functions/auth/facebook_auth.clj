(ns playphraseme.api.route-functions.auth.facebook-auth
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parser]
            [clojure.pprint :refer [pprint]]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [playphraseme.app.config :refer [env]]))

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
             :scope              ["email" "public_profile" "user_friends"]
             :profileFields      ["emails" "name" "gender" "displayName" "age_range"]
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

    (let [{:keys [id first_name last_name email]} user-details]
      (println "-----------------")
      (pprint user-details)
      (println "-----------------")
      (resp/redirect "/#/auth?auth-token=any-token"))))

