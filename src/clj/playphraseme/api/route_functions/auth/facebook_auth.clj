(ns playphraseme.api.route-functions.auth.facebook-auth
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parser]
            [playphraseme.common.debug-util :as debug-util :refer [...]]
            [playphraseme.app.config :refer [env]]))

(defn facebook-auth-response []
  (let [{:keys [facebook-callback-uri facebook-client-id facebook-client-secret]} env]
    (resp/redirect
     (:uri (oauth2/make-auth-request
            {:authorization-uri  "https://graph.facebook.com/oauth/authorize"
             :access-token-uri   "https://graph.facebook.com/oauth/access_token"
             :redirect-uri       facebook-callback-uri
             :client-id          facebook-client-id
             :client-secret      facebook-client-secret
             :access-query-param :access_token
             :scope              ["email"]
             :grant-type         "authorization_code"})))))

(defn facebook-auth-callback-response [code]
  (let [{:keys [facebook-callback-uri
                facebook-client-id
                facebook-client-secret]} env
        access-token-response            (:body (client/get (str "https://graph.facebook.com/oauth/access_token?"
                                                                 "client_id=" facebook-client-id
                                                                 "&redirect_uri=" facebook-callback-uri
                                                                 "&client_secret=" facebook-client-secret
                                                                 "&code=" code)))
        access-token                     (get (re-find #"access_token=(.*?)&expires=" access-token-response) 1)
        user-details-str                 (client/get (str "https://graph.facebook.com/me?access_token=" access-token))
        user-details                     (-> user-details-str :body (parser/parse-string keyword))]

    (let [{:keys [id first_name last_name email]} user-details]
      (println ">> facebook login complete:" (... id first_name email))
      (resp/redirect "/#/auth/?auth-token=any-token"))))

