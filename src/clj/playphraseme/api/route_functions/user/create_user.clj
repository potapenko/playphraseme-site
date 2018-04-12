(ns playphraseme.api.route-functions.user.create-user
  (:require [buddy.hashers :as hashers]
            [clojure.string :as string]
            [playphraseme.api.queries.user.registered-user :as users]
            [playphraseme.api.queries.user.user-permission :as user-prm]
            [ring.util.http-response :as respond]))

(defn create-new-user
  "Create user with `email`, `name`, `password`"
  [email name password]
  (let [hashed-password (hashers/encrypt password)
        new-user        (users/insert-registered-user! {:email    email
                                                        :name name
                                                        :password hashed-password})
        permission      (user-prm/insert-permission-for-user! (:id new-user) "basic")]
    (respond/created "" {:name (str (:name new-user))})))

(defn create-user-response
  "Generate response for user creation"
  [email name password]
  (let [name-query   (users/get-registered-user-by-name name)
        email-query      (users/get-registered-user-by-email email)
        email-exists?    (not-empty email-query)
        name-exists? (not-empty name-query)]
    (cond
      (and name-exists? email-exists?) (respond/conflict {:error "name and Email already exist"})
      name-exists?                     (respond/conflict {:error "name already exists"})
      (-> password count (< 5))            (respond/conflict {:error "Password - 5 symbols minimum"})
      (-> password string/blank?)          (respond/conflict {:error "Password is empty"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user email name password))))

