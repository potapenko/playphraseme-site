(ns playphraseme.api.routes.favorites
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.search.phrases-search :refer :all]
            [playfavoriteme.api.queries.favorites :refer :all]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

(def phrases-routes
  "Specify routes for Favorites Phrases"
  (context "/api/v1/favorites" []
           :tags ["Favorites"]

           (GET "/"            request
                :return        s/Any
                :middleware    [token-auth-mw cors-mw authenticated-mw]
                :header-params [authorization :- String]
                :query-params  [{skip :- s/Num 0} {limit :- s/Num 10}]
                :summary       "Return user favorites"
                (ok (get-favorites-by-user (-> request :identity :id) skip limit)))

           (GET "/count"       request
                :return        s/Any
                :middleware    [token-auth-mw cors-mw authenticated-mw]
                :header-params [authorization :- String]
                :query-params  [{skip :- s/Num 0} {limit :- s/Num 10}]
                :summary       "Return user favorites count"
                (ok (get-favorites-count (-> request :identity :id))))

           (POST "/"            request
                 :tags          ["Phrases"]
                 :return        s/Str
                 :middleware    [token-auth-mw cors-mw authenticated-mw]
                 :query-params  [phrase-id :- s/Str]
                 :summary       "Add favorite"
                 (do (insert-favorite! phrase-id (-> request :identity :id))
                     (ok "OK")))

           (DELETE "/"            request
                   :tags          ["Phrases"]
                   :return        s/Str
                   :middleware    [token-auth-mw cors-mw authenticated-mw]
                   :query-params  [phrase-id :- s/Str]
                   :summary       "Add favorite"
                   (do (delete-favorite-by-phrase-id! phrase-id (-> request :identity :id))
                       (ok "OK")))))

