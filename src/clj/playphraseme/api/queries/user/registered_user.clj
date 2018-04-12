(ns playphraseme.api.queries.user.registered-user
  (:require [monger.core :as mg]
            [mount.core :as mount]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [playphraseme.app.config :refer [env]]
            [playphraseme.db.core :refer :all]
            [playphraseme.api.general-functions.doc-id :refer :all]))

(def coll "users")

(defn get-registered-user-by-id
  "Selects the (id, email, username, password, refresh_token) for registered user matching the id"
  [^String user-id]
  (stringify-id
   (get-doc-by-id coll (str->id user-id))))

(defn get-registered-user-by-refresh-token
  [refresh-token]
  (stringify-id
   (get-doc coll {:refresh-token refresh-token})))

(defn get-registered-user-by-username
  "Selects the (id, email, username) for registered user matching the username"
  [username]
  (stringify-id
   (get-doc coll {:username username})))

(defn get-registered-user-by-email
  "Selects the (id, email, username) for registered user matching the email"
  [email]
  (stringify-id
   (get-doc coll {:email email})))

(defn insert-registered-user!
  "Inserts a single user"
  [user-data]
  (stringify-id
   (add-doc coll user-data)))

(defn update-registered-user!
  "Update a single user matching provided id"
  [^String user-id {:keys [email username password refresh-token] :as user-data}]
  (update-doc-by-id coll (str->id user-id) user-data))

(defn update-registered-user-password!
  "Update the password for the user matching the given userid"
  [^String user-id password]
  (update-doc-by-id coll (str->id user-id) {:password password}))

(defn update-registered-user-verified-email!
  "Update the 'email verified' flag for the user matching the given userid"
  [^String user-id verified?]
  (update-doc-by-id coll (str->id user-id) {:email-verified verified?}))

(defn update-registered-user-refresh-token!
  "Update the refresh token for the user matching the given userid"
  [^String user-id refresh-token]
  (update-doc-by-id coll (str->id user-id) {:refresh-token refresh-token}))

(defn null-refresh-token!
  "Set refresh token to null for row matching the given refresh token"
  [refresh-token]
  (let [id (:id (get-registered-user-by-refresh-token refresh-token))]
    (update-doc-by-id coll (str->id id) {:refresh-token nil})))

(defn delete-registered-user!
  "Delete a single user matching provided id"
  [^String user-id]
  (delete-doc-by-id coll (str->id user-id)))
