(ns playphraseme.api.routes.phrases
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.csrf :refer [csrf-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.auth.get-auth-credentials :refer [credentials-response]]
            [playphraseme.api.route-functions.search.phrases-search :refer :all]
            [schema.core :as s]
            [ring.util.http-response :as respond]
            [compojure.api.sweet :refer :all]))

(def session-routes
  "Specify routes for User functions"
  (context "/api/v1/phrases" []
           :tags ["Phrases"]

  (GET "/search"     request
      :tags          ["Session"]
      :return        s/Any
      :middleware    [cors-mw authenticated-mw]
      :query-params  [q :- s/Str {skip :- s/Num 0} {limit :- s/Num 10}]
      :summary       "Return phrase search result"
      (search-response q skip limit))))
