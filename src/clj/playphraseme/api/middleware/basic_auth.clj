(ns playphraseme.api.middleware.basic-auth
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [playphraseme.api.auth-resources.basic-auth-backend :refer [basic-backend]]))

(defn basic-auth-mw
  "Middleware used on routes requiring basic authentication"
  [handler]
  (wrap-authentication handler basic-backend))
