(ns playphraseme.api.route-functions.auth.facebook-auth
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parser]
            [playphraseme.app.config :refer [env]]))

(def app-id (:facebook-client-id env))
(def app-secret (:facebook-client-secret env))
(def redirect-uri (:facebook-callback-url env))

(def facebook-oauth2
  {:authorization-uri  "https://graph.facebook.com/oauth/authorize"
   :access-token-uri   "https://graph.facebook.com/oauth/access_token"
   :redirect-uri       redirect-uri
   :client-id          app-id
   :client-secret      app-secret
   :access-query-param :access_token
   :scope              ["email"]
   :grant-type         "authorization_code"})

(defn facebook-auth-response []
  (resp/redirect
   (:uri (oauth2/make-auth-request facebook-oauth2))))

(defn facebook-auth-callback-response [code]
  (let [access-token-response (:body (client/get (str "https://graph.facebook.com/oauth/access_token?"
                                                      "client_id=" app-id
                                                      "&redirect_uri=" redirect-uri
                                                      "&client_secret=" app-secret
                                                      "&code=" code)))
        access-token          (get (re-find #"access_token=(.*?)&expires=" access-token-response) 1)
        user-details          (-> (client/get (str "https://graph.facebook.com/me?access_token=" access-token))
                                  :body
                                  (parser/parse-string keyword))]

    (let [{:keys [id first_name email]} user-details]
      (resp/redirect "/#/auth/?auth-token=any-token"))))

