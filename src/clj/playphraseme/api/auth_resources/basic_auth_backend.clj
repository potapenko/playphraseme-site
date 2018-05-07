(ns playphraseme.api.auth-resources.basic-auth-backend
  (:require [playphraseme.api.queries.user.registered-user :as users]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
            [buddy.hashers :as hashers]))

(defn get-user-info
  "The name and email values are stored in citext fields in Postgres thus
   the need to convert them to strings for future use. Since we want to accept
   eiter name or email as an identifier we will query for both and check
   for a match."
  [identifier]
  (let [registered-user (users/get-registered-user-by-email identifier)]
    (when-not (nil? registered-user)
      {:user-data (-> registered-user
                      (assoc-in [:name] (str (:name registered-user)))
                      (assoc-in [:email]    (str (:email registered-user)))
                      (dissoc   :created_on)
                      (dissoc   :password))
       :password  (:password registered-user)})))

(defn basic-auth
  "This function will delegate determining if we have the correct name and
   password to authorize a user. The return value will be added to the request
   with the keyword of :identity. We will accept either a valid name or
   valid user email in the name field. It is a little strange but to adhere
   to legacy basic auth api of using name:password we have to make the
   field do double duty."
  [request {:keys [username password]}]
  (let [user-info   (get-user-info username)]
    (if (and user-info (hashers/check password (:password user-info)))
      (:user-data user-info)
      false)))

(def basic-backend
  "Use the basic-auth function defined in this file as the authentication
   function for the http-basic-backend"
  (http-basic-backend {:authfn basic-auth}))
