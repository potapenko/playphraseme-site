(ns playphraseme.app.facebook-auth
  (:require [clj-oauth2.client :as oauth2]
            [noir.response :as resp]
            [clj-http.client :as client]
            [cheshire.core :as parse]))

(def APP_ID "your app id")
(def APP_SECRET "your app secret key")
(def REDIRECT_URI "http://localhost:3000/auth_facebook")

(def facebook-oauth2
  {:authorization-uri "https://graph.facebook.com/oauth/authorize"
   :access-token-uri "https://graph.facebook.com/oauth/access_token"
   :redirect-uri REDIRECT_URI
   :client-id APP_ID
   :client-secret APP_SECRET
   :access-query-param :access_token
   :scope ["email"]
   :grant-type "authorization_code"})
