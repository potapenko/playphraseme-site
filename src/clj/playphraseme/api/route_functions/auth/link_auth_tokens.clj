(ns playphraseme.api.route-functions.auth.link-auth-tokens
  (:require [playphraseme.api.general-functions.user.create-token :refer [create-token]]
            [playphraseme.api.route-functions.user.create-user :refer [create-user-response]]
            [playphraseme.api.queries.user.link-auth-tokens :as link-tokens]
            [playphraseme.api.queries.user.registered-user :as users]
            [playphraseme.app.config :refer [env]]
            [clj-time.coerce :refer [from-date]]
            [clj-time.core :as time]
            [clj-http.client :refer [success?]]
            [postal.core :refer [send-message]]
            [ring.util.http-response :as respond]))

(defn- token-expired? [^java.util.Date token-date]
  (let [token-ttl-days (-> env :auth-by-link :email-link-token-ttl-days)]
    (assert (not (nil? token-ttl-days)) "Email link token TTL not set - update config!")
    (time/after?
     (time/now)
     (time/plus (from-date token-date) (time/days token-ttl-days)))))

(defn- send-email
  "Send password reset email (uses Postal + https://account.sendinblue.com/)"
  [{:keys [to-email from-email subject html-body plain-body]}]
  (send-message (:stmp env) {:from    from-email
                             :to      to-email
                             :subject subject
                             :body    [:alternative
                                       {:type "text/plain" :content plain-body}
                                       {:type "text/html"  :content html-body}]}))

(defn- verify-email-plain-body [base-url token]
  (let [verify-url (str base-url "/" token)]
    (str "Verify email\n\n" verify-url)))

(defn- verify-email-html-body [base-url token]
  (let [verify-url (str base-url "/" token)]
    (str "<html><body><h1>Verify email</h1><br><p><a href=\"" verify-url "\">" verify-url "</a></p></body></html>")))

(defn- send-verify-email [email link-token]
  (let [base-url (-> env :auth-by-link :auth-base-link)]
    (send-email {:to-email   email
                 :from-email (-> env :reset-password-email-from)
                 :subject    "Verify email"
                 :html-body  (verify-email-html-body base-url link-token)
                 :plain-body (verify-email-plain-body base-url link-token)})))

(defn verify-email-response
  "Generate response for get requests to /api/v1/auth/verify-email. A successful request to this route will generate a new
   refresh-token, and return {:id :name :permissions :token :refresh-token}"
  [link-token]
  (let [email-jwt-expiration-period        (-> env :auth-by-link :jwt-by-email-ttl)
        {:keys [email date] :as token-doc} (link-tokens/get-token-doc-by-token link-token)]
    (assert (not (nil? email-jwt-expiration-period)) "JWT generated upon verify email expiration period not set - update config!")
    (cond
      (nil? email)          (respond/not-found {:error "The token does not exist"})
      (nil? date)           (respond/internal-server-error {:error "The token's date is empty"})
      (token-expired? date) (respond/gone {:error "The token has expired"})
      :else (let [user          (users/get-registered-user-by-email email)
                  refresh-token (str (java.util.UUID/randomUUID))
                  _             (users/update-registered-user-refresh-token! (:id user) refresh-token)]
              (link-tokens/delete-token link-token)
              (users/update-registered-user-verified-email! (:id user) true)
              (respond/ok {:id            (:id user)
                           :name      (:name user)
                           :permissions   (:permissions user)
                           :token         (create-token user email-jwt-expiration-period)
                           :refresh-token refresh-token})))))

(defn- insert-and-send-token-response [email link-token]
  (do
    (link-tokens/insert-token! email link-token)
    (send-verify-email email link-token)
    (respond/ok {:link-token link-token})))

(defn link-token-generate-response
  "Generate response for get request to /api/v1/auth/link. Tries to create a user with the email given if it doesn't
  exist using 'user-create-response' and if it fails, returns its response. Otherwise, generates link token for the
  email given."
  [email]
  (let [user-by-email (users/get-registered-user-by-email email)
        link-token    (str (java.util.UUID/randomUUID))]
    (if (nil? user-by-email)
      (let [generated-password   (str (java.util.UUID/randomUUID))
            user-create-response (create-user-response email email generated-password)]
        (if (success? user-create-response)
          (insert-and-send-token-response email link-token)
          user-create-response))
      (insert-and-send-token-response email link-token))))
