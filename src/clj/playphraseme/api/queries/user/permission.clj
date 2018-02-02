(ns playphraseme.api.queries.user.permission
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.app.config :refer [env]]
            [playphraseme.db.core :refer :all]
            [mount.core :as mount]))

(def coll "permissions")

(defn insert-permission!
  "Inserts a single permission into the permission table"
  [permission]
  (when-not (get-doc coll {:permission permission})
    (add-doc coll {:permission permission})))

(comment
  (insert-permission! "admin")
  (insert-permission! "basic"))
