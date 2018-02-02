(ns playphraseme.api.queries.user.user-permission
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [playphraseme.app.config :refer [env]]
            [playphraseme.api.queries.user.registered-user :as db-user]))

(defn get-permissions-for-userid
  "get all the permissions for a given userid"
  [^String user-id]
  (or (some-> (db-user/get-registered-user-by-id user-id) :permissions) []))

(defn insert-permission-for-user!
  "Inserts the specified permission for the designated user"
  [^String user-id permission]
  (let [permissions (-> (get-permissions-for-userid user-id) set
                        (conj permission) vec)]
    (db-user/update-registered-user! user-id {:permissions permissions})))

(defn delete-user-permission!
  "Delete a single user permission matching provided id and permission name"
  [^String user-id permission]
  (let [permissions (-> (get-permissions-for-userid user-id) set
                        (disj permission) vec)]
    (db-user/update-registered-user! user-id {:permissions permissions})))

