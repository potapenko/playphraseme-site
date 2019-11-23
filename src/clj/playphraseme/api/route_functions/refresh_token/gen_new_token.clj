(ns playphraseme.api.route-functions.refresh-token.gen-new-token
  (:require [playphraseme.api.general-functions.user.create-token :refer [create-token]]
            [playphraseme.api.queries.user.registered-user :as users]
            [ring.util.http-response :as respond]))

(defn create-new-tokens
  "Create a new user token"
  [user]
  (let [new-refresh-token (str (java.util.UUID/randomUUID))
        _ (users/update-registered-user-refresh-token! (:id user) new-refresh-token)]
    {:token (create-token user) :refresh-token new-refresh-token}))

(defn gen-new-token-response
  "Generate response for user token creation"
  [refresh-token]
  (let [user (users/get-registered-user-by-refresh-token refresh-token)]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          (create-new-tokens user)))))
