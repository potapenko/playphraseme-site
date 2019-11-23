(ns playphraseme.api.routes.refresh-token
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.route-functions.refresh-token.delete-refresh-token :refer [remove-refresh-token-response]]
            [playphraseme.api.route-functions.refresh-token.gen-new-token :refer [gen-new-token-response]]
            [compojure.api.sweet :refer :all]))

(def refresh-token-routes
  "Specify routes for Refresh-Token functions"
  (context "/api/v1/refresh-token/:refresh-token" []
           :tags        ["Refresh-Token"]
           :middleware  [cors-mw]
           :path-params [refresh-token :- String]

    (GET "/" request
          :return         {:token String :refresh-token String}
          :summary        "Get a fresh token and new refresh-token with a valid refresh-token."
          (gen-new-token-response refresh-token))

    (DELETE "/" request
            :return         {:message String}
            :summary        "Delete the specific refresh-token"
            (remove-refresh-token-response refresh-token))))
