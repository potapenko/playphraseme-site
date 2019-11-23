(ns playphraseme.api.route-functions.refresh-token.delete-refresh-token
  (:require [playphraseme.api.queries.user.registered-user :as users]
            [ring.util.http-response :as respond]))

(defn remove-refresh-token-response
  "Remove refresh token (error if doesn't exist)"
  [refresh-token]
  (let [null-refresh-token (users/null-refresh-token! refresh-token)]
    (if (zero? null-refresh-token)
      (respond/not-found  {:error "The refresh token does not exist"})
      (respond/ok         {:message "Refresh token successfully deleted"}))))
