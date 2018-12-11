(ns playphraseme.api.queries.user.registered-user
  (:require [playphraseme.api.general-functions.doc-id :refer :all]
            [playphraseme.db.users-db :refer :all]
            [clojure.string :as string]))

(def coll "users")

(defn get-registered-user-by-id
  "Selects the (id, email, name, password, refresh_token) for registered user matching the id"
  [^String user-id]
  (stringify-id
   (get-doc-by-id coll (str->id user-id))))

(defn get-registered-user-by-refresh-token
  [refresh-token]
  (stringify-id
   (get-doc coll {:refresh-token refresh-token})))

(defn get-registered-user-by-name
  "Selects the (id, email, name) for registered user matching the name"
  [name]
  (stringify-id
   (get-doc coll {:name name})))

(defn get-registered-user-by-email
  "Selects the (id, email, name) for registered user matching the email"
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
  [^String user-id {:keys [email name password refresh-token] :as user-data}]
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


(comment

  (let [names (->> "names.txt" slurp string/split-lines (remove string/blank?) set)]
    (->> (find-docs coll {:pred  {:email {"$nin" [nil 0 "0"]}
                                  :name  {"$nin" [nil 0 "0"]}}
                          :skip  0
                          :limit 0})
         (map (fn [{:keys [name email]}]
                (let [[name surname] (string/split name #" +" )]
                  {:name    name
                   :surname surname
                   :email   email})))
         (remove #(-> % :name nil?))
         (remove #(-> % :surname nil?))
         (filter #(->> % :email (re-find #"^.+@.+\..+$")))
         (filter (fn [{:keys [name surname email]}]
                   (or (-> email (string/ends-with? "ru"))
                       (->> name string/lower-case (re-find #"^[а-я]+$"))
                       (some->> surname string/lower-case (re-find #"(ov|ko|va|ev|in|na|ik|ak|ih|ki)$"))
                       (names name))))
         (map (fn [{:keys [name surname email]}] (string/join "," [email name surname])))
         (shuffle)
         (partition-all 20000)
         (map-indexed (fn [index part]
                        (->> part
                             (map #(spit (format "emails-ru-%s.csv" index) (str % "\n") :append true))
                             (doall))))))


  )
