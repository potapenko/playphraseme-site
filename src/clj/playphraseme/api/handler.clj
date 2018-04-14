(ns playphraseme.api.handler
  (:require [compojure.api.sweet :refer :all]
            [playphraseme.api.routes.user :refer :all]
            [playphraseme.api.routes.preflight :refer :all]
            [playphraseme.api.routes.permission :refer :all]
            [playphraseme.api.routes.refresh-token :refer :all]
            [playphraseme.api.routes.auth :refer :all]
            [playphraseme.api.routes.session :refer :all]
            [playphraseme.api.routes.password :refer :all]
            [playphraseme.api.routes.phrases :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defapi api-routes
  {:swagger
   {:ui   "/api-docs"
    :spec "/swagger.json"
    :data {:info {:title   "playphraseme.api"
                  :version "0.0.1"}
           :tags [{:name "Preflight" :description "Return successful response for all preflight requests"}
                  {:name "User" :description "Create, delete and update user details"}
                  {:name "Permission" :description "Add and remove permissions tied to specific users"}
                  {:name "Refresh-Token" :description "Get and delete refresh-tokens"}
                  {:name "Auth" :description "Get auth information for a user"}
                  {:name "Session" :description "Get session information for a user"}
                  {:name "Phrases" :description "Search phrases"}
                  {:name "Password" :description "Request and confirm password resets"}]}}}
  preflight-route
  user-routes
  permission-routes
  refresh-token-routes
  auth-routes
  session-routes
  password-routes
  phrases-routes)
