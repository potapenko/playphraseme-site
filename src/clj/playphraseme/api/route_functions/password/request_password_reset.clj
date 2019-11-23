(ns playphraseme.api.route-functions.password.request-password-reset
  (:require [playphraseme.api.queries.user.password-reset-key :as prk]
            [playphraseme.api.queries.user.registered-user :as users]
            [playphraseme.app.config :refer [env]]
            [postal.core :refer [send-message]]
            [ring.util.http-response :as respond]))

(defn add-response-link-to-plain-body
  "Insert link into plaintext email body"
  [body response-link]
  (str body "\n\n" response-link))

(defn add-response-link-to-html-body
  "Insert link into HTML email body"
  [body response-link]
  (let [body-less-closing-tags (clojure.string/replace body #"</body></html>" "")]
    (str body-less-closing-tags "<br><p>" response-link "</p></body></html>")))

(defn send-reset-email
  "Send password reset email (uses Postal + https://account.sendinblue.com/)"
  [to-email from-email subject html-body plain-body]
  (send-message (:stmp env)
                {:from    from-email
                 :to      to-email
                 :subject subject
                 :body    [:alternative
                           {:type "text/plain" :content plain-body}
                           {:type "text/html" :content html-body}]}))

(defn process-password-reset-request
  "Safely process password reset request

  The `response-base-link` will get a reset key appended to it and then the
  link itself will be appended to the email body. The reset key will be valid
  for 24 hours after creation. *NOTE* do not use a fromEmail address ending
  with @gmail.com because of the DMARC policy. It is recommended to use a custom
  domain you own instead"
  [user from-email subject email-body-plain email-body-html response-base-link]
  (let [reset-key     (str (java.util.UUID/randomUUID))
        the-insert    (prk/insert-password-reset-key! (:id user) reset-key)
        response-link (str response-base-link "/" reset-key)
        body-plain    (add-response-link-to-plain-body email-body-plain response-link)
        body-html     (add-response-link-to-html-body email-body-html response-link)]
    (send-reset-email (str (:email user)) from-email subject body-html body-plain)
    (respond/ok {:message (str "Reset email successfully sent to " (str (:email user)))})))

(defn request-password-reset-response
  "Generate response for password reset request"
  [user-email]
  (let [from-email         (env :reset-password-email-from)
        subject            "Reset password"
        email-body-plain   "Reset password"
        email-body-html    "<html><body><h1>Reset password</h1></body></html>"
        response-base-link (env :reset-password-base-link)
        user               (users/get-registered-user-by-email user-email)]
    (if (empty? user)
      (respond/not-found {:error (str "No user exists with the email " user-email)})
      (process-password-reset-request user from-email subject email-body-plain email-body-html response-base-link))))
