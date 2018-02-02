(ns playphraseme.api.queries.user.link-auth-tokens
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [mount.core :as mount]
            [playphraseme.app.config :refer [env]]
            [playphraseme.db.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.common.dates-util :as date-util]))

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