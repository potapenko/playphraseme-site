(ns playphraseme.api.routes.google-cloud
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.middleware.token-auth :refer [token-auth-mw]]
            [playphraseme.api.middleware.authenticated :refer [authenticated-mw]]
            [playphraseme.api.route-functions.search.phrases-search :refer :all]
            [playphraseme.api.middleware.csrf :refer [csrf-mw]]
            [playphraseme.api.queries.favorites :refer :all]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [playphraseme.common.google-translate :refer [translate]]))

(defn google-translate [text to-lang]
  (translate text "en" to-lang))

(def google-cloud-routes
  "Specify routes for Google Cloud API"
  (context "/api/v1/favorites" []
           :tags ["Google Cloud"]

           (GET "/translate"            request
                :return        s/Any
                :middleware    [cors-mw csrf-mw]
                :query-params  [text :- s/Str to-lang :- s/Str]
                :summary       "Translate phrase"
                (ok (google-translate text to-lang)))))


(comment



  )
