(ns playphraseme.api.middleware.authenticated
  (:require [buddy.auth :refer [authenticated?]]
            [ring.util.http-response :refer [unauthorized]]
            [clojure.pprint :refer :all]))

(defn authenticated-mw
  "Middleware used in routes that require authentication. If request is not
   authenticated a 401 not authorized response will be returned"
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (unauthorized {:error "Not authorized"}))))

