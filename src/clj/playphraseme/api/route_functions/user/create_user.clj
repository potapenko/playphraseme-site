(ns playphraseme.api.route-functions.user.create-user
  (:require [buddy.hashers :as hashers]
            [clojure.string :as string]
            [playphraseme.api.queries.user.registered-user :as users]
            [playphraseme.api.queries.user.user-permission :as user-prm]
            [ring.util.http-response :as respond]))

(defn create-new-user
  "Create user with `email`, `username`, `password`"
  [email username password]
  (let [hashed-password (hashers/encrypt password)
        new-user        (users/insert-registered-user! {:email    email
                                                        :username username
                                                        :password hashed-password})
        permission      (user-prm/insert-permission-for-user! (:id new-user) "basic")]
    (respond/created "" {:username (str (:username new-user))})))

(defn create-user-response
  "Generate response for user creation"
  [email username password]
  (let [username-query   (users/get-registered-user-by-username username)
        email-query      (users/get-registered-user-by-email email)
        email-exists?    (not-empty email-query)
        username-exists? (not-empty username-query)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      username-exists?                     (respond/conflict {:error "Username already exists"})
      (-> password count (< 5))            (respond/conflict {:error "Password - 5 symbols minimum"})
      (-> password string/blank?)          (respond/conflict {:error "Password is empty"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user email username password))))

