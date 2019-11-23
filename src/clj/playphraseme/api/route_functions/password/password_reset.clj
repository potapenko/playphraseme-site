(ns playphraseme.api.route-functions.password.password-reset
  (:require [buddy.hashers :as hashers]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [ring.util.http-response :as respond]
            [playphraseme.api.queries.user.password-reset-key :as prk]
            [playphraseme.api.queries.user.registered-user :as users]))

(defn update-password
  "Update user's password"
  [reset-key key-row new-password]
  (let [user-id         (:user-id key-row)
        hashed-password (hashers/encrypt new-password)]
    (prk/remove-reset-key! reset-key)
    (users/update-registered-user-password! user-id hashed-password)
    (respond/ok {:message "Password successfully reset"})))

(defn password-reset-response
  "Generate response for password update"
  [reset-key new-password]
  (let [key-data        (prk/get-reset-data-by-reset-key reset-key)
        key-exists?     (nil? key-data)
        already-used?   (:already-used key-data)
        key-valid-until (c/from-string (:valid-until key-data))
        key-valid?      (t/before? (t/now) key-valid-until)]
    (cond
      key-exists?   (respond/not-found {:error "Reset key does not exist"})
      key-valid?    (update-password reset-key key-data new-password)
      :else         (respond/not-found {:error "Reset key has expired"}))))
