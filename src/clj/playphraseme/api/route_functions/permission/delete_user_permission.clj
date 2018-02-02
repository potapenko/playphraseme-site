(ns playphraseme.api.route-functions.permission.delete-user-permission
  (:require [playphraseme.api.queries.user.user-permission :as user-prm]
            [ring.util.http-response :as respond]))

(defn delete-user-permission
  "Remove user permission"
  [id permission]
  (let [deleted-permission (user-prm/delete-user-permission! {:userid id :permission permission})]
    (if (not= 0 deleted-permission)
      (respond/ok        {:message (format "Permission '%s' for user %s successfully removed" permission id)})
      (respond/not-found {:error (format "User %s does not have %s permission" id)}))))

(defn delete-user-permission-response
  "Generate response for user permission deletion"
  [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (delete-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))

