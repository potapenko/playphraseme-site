(ns playphraseme.api.queries.user.permission
  (:require [playphraseme.db.users-db :refer :all]))

(def coll "permissions")

(defn insert-permission!
  "Inserts a single permission into the permission table"
  [permission]
  (when-not (get-doc coll {:permission permission})
    (add-doc coll {:permission permission})))

(comment
  (insert-permission! "admin")
  (insert-permission! "basic"))
