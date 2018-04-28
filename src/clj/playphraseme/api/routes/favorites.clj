(ns playphraseme.api.routes.favorites
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.search.phrases-search :refer :all]
            [playphraseme.api.queries.favorites :refer :all]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

(def favorites-routes
  "Specify routes for Favorites Phrases"
  (context "/api/v1/favorites" []
           :tags ["Favorites"]

           (GET "/"            request
                :return        s/Any
                :middleware    [token-auth-mw cors-mw authenticated-mw]
                :header-params [authorization :- String]
                :query-params  [{skip :- s/Num 0} {limit :- s/Num 10}]
                :summary       "Return user favorites"
                (let [user-id (-> request :identity :id)]
                  (ok {:count     (get-favorites-count user-id)
                       :favorites (get-favorites-by-user user-id skip limit)})))

           (GET "/:phrase-id"  [phrase-id :as request]
                :return        s/Any
                :middleware    [token-auth-mw cors-mw authenticated-mw]
                :header-params [authorization :- String]
                :summary       "Get phrase favorite"
                (let [user-id (-> request :identity :id)]
                  (ok (get-favorite-by-phrase-id phrase-id user-id))))

           (POST "/:phrase-id"            [phrase-id :as request]
                 :return        s/Str
                 :middleware    [token-auth-mw cors-mw authenticated-mw]
                 :summary       "Add favorite"
                 (do (insert-favorite! phrase-id (-> request :identity :id))
                     (ok "OK")))

           (DELETE "/:phrase-id"  [phrase-id :as request]
                   :return        s/Str
                   :middleware    [token-auth-mw cors-mw authenticated-mw]
                   :summary       "Remove favorite"
                   (do (delete-favorite-by-phrase-id! phrase-id (-> request :identity :id))
                       (ok "OK")))))

