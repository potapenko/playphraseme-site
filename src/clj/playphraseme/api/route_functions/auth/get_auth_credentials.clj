(ns playphraseme.api.route-functions.auth.get-auth-credentials
  (:require [playphraseme.api.general-functions.user.create-token :refer [create-token]]
            [playphraseme.api.queries.user.registered-user :as users]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response
  "Generate response for get requests to /api/v1/auth. This route requires basic
   authentication. A successful request to this route will generate a new
   refresh-token, and return {:id :username :permissions :token :refresh-token}"
  [request]
  (let [user          (:identity request)
        refresh-token (str (java.util.UUID/randomUUID))
        _             (users/update-registered-user-refresh-token! (:id user) refresh-token)]
    (respond/ok {:id            (:id user)
                 :username      (:username user)
                 :permissions   (:permissions user)
                 :token         (create-token user)
                 :refresh-token refresh-token})))


(defn credentials-response
  "Generate response for get requests to /api/v1/session."
  [request]
  (let [user (:identity request)]
    (respond/ok (assoc (select-keys user [:id :username :permissions :refresh-token])
                       :token (create-token user)))))

