(ns playphraseme.api.queries.user.link-auth-tokens
  (:require [monger.collection :as mc]
            [mount.core :as mount]
            [playphraseme.db.core :refer :all]))

(declare migrations)

(def collection "link_auth_tokens")

(defn insert-token!
  "Inserts a token -> email reference row"
  [email token]
  (add-doc collection {:token token
                       :email email
                       :date  (java.util.Date.)
                       }))

(defn get-token-doc-by-token
  [token]
  "Returns an email by an auth token"
  (some-> (find-doc collection {:pred {:token token} :sort :reverse})))

(defn delete-by-token!
  "Deletes a token -> email reference row by a token"
  [token]
  (delete-docs collection {:token token}))

(defn delete-token [^String token]
  (delete-docs collection {:token token}))

(defn start []
  (mc/ensure-index db collection {:token 1}))

(mount/defstate migrations
  :start (start))

(comment
  (insert-token! "a.a.eliseyev@gmail.com" "sometoken")
  (get-token-doc-by-token "27efd066-c0f6-44d0-87bd-40c103824ef3")
  (delete-by-token! "sometoken"))
