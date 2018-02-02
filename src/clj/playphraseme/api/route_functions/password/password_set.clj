(ns playphraseme.api.route-functions.password.password-set
  (:require [buddy.hashers :as hashers]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [ring.util.http-response :as respond]
            [playphraseme.api.queries.user.registered-user :as users]))

(defn password-set-response
  "Generate response to setting a new password for current user"
  [request new-password]
  (let [user            (:identity request)
        user-id         (:id user)
        hashed-password (hashers/encrypt new-password)]
    (users/update-registered-user-password! user-id hashed-password)
    (respond/ok {:message "Password successfully set"})))