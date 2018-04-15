(ns playphraseme.api.queries.user.password-reset-key
  (:require [clj-time.coerce :as c]
            [clj-time.core :as t]
            [playphraseme.common.dates-util :as date-util]
            [playphraseme.db.core :refer :all]))

(def coll "password_reset_keys")

(defn get-password-reset-keys-for-userid
  "get the password reset key(s) for a given userid"
  [^String user-id]
  (get-doc coll {:user-id user-id}))

(defn get-reset-data-by-reset-key
  "get the row containing the specified reset_key"
  [reset-key]
  (get-doc coll {:reset-key reset-key}))

(defn remove-reset-key!
  "delete a specified reset_key ture"
  [reset-key]
  (delete-docs coll {:reset-key reset-key}))

(defn insert-password-reset-key!
  "inserts a row in the password_reset_key table using the default valid until timestamp"
  [user-id reset-key]
  (delete-docs coll {:user-id user-id})
  (add-doc coll {:user-id      user-id
                 :reset-key    reset-key
                 :valid-until  (-> (t/now) (t/plus (t/hours 12)) date-util/format-date)}))

(comment

  (insert-password-reset-key! "any key" "user id")
  (get-reset-data-by-reset-key "any key")
  (-> (get-reset-data-by-reset-key "any key") :valid-until c/from-string)

  )
