(ns playphraseme.api.route-functions.permission.add-user-permission
  (:require [playphraseme.api.queries.user.user-permission :as user-prm]
            [ring.util.http-response :as respond]))

(defn add-user-permission
  "Create user permission"
  [id permission]
  (let [added-permission (try
                           (user-prm/insert-permission-for-user! id permission)
                           (catch Exception e 0))]
    (if (not= 0 added-permission)
      (respond/ok        {:message (format "Permission '%s' for user %s successfully added" permission id)})
      (respond/not-found {:error (format "Permission '%s' does not exist" permission)}))))

(defn add-user-permission-response
  "Generate response for permission creation"
  [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (add-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))

