(ns playphraseme.app.facebook-auth
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parse]
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

(resp/redirect
 (:uri (oauth2/make-auth-request facebook-oauth2)))

(def facebook-user (atom nil))

(defn facebook [params]
  (let [access-token-response (:body (client/get (str "https://graph.facebook.com/oauth/access_token?"
                                                      "client_id=" app-id
                                                      "&redirect_uri=" redirect-uri
                                                      "&client_secret=" app-secret
                                                      "&code=" (get params "code"))))
        access-token          (get (re-find #"access_token=(.*?)&expires=" access-token-response) 1)
        user-details          (-> (client/get (str "https://graph.facebook.com/me?access_token=" access-token))
                                  :body
                                  (parse/parse-string))]
    (swap! facebook-user
           #(assoc % :facebook-id %2 :facebook-name %3 :facebook-email %4)
           (get user-details "id")
           (get user-details "first_name")
           (get user-details "email"))))

(defroutes facebook-routes
  (GET "/auth_facebook" {params :query-params} (facebook params)))
