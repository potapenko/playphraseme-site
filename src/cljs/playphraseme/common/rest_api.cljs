(ns playphraseme.common.rest-api
  (:require [cljs-http.client :as http]
            [re-frame.core :as rf]
            [cljs.core.async :refer [<! put! chan] :as async]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as string]
            [playphraseme.common.util :as util]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn not-found-md [res]
  (when (string? res) (println res))
  (let [err-res {:code 404 :success false :body "not found"}]
    (cond
      (= (:code res) 404) err-res
      :else res)))

(def default-middlewares [not-found-md])

(defn call-api [http-fn router-uri opts & middlewares]
  (go
    (let [res         (<! (http-fn (str "/api/v1" router-uri) opts))
          middlewares (concat default-middlewares middlewares)]
      (loop [res     res
             [v & t] middlewares]
        (if (and res v)
          (recur (v res) t)
          res)))))

(defn success? [res]
  (-> res :success true?))

(defn error? [res]
  (-> res :success false?))

(defn auth-error? [res]
  (= (:code res) 401))

(defn get-auth-token []
  (localstorage/get-item "auth-token"))

(defn authorization-header []
  {"authorization" (str "Token " (get-auth-token))})

(defn api-headers []
  {:headers (authorization-header) :content-type :json :accept :json})

(defn logout []
  (localstorage/remove-item! "auth-token")
  (dispatch [:set-auth-data nil]))

(defn- auth* [auth-ch]
  (go
    (let [res (<! auth-ch)]
      (if (success? res)
        (do
          (localstorage/set-item! "auth-token" (-> res :body :token))
          (dispatch-sync [:set-auth-data (->> res :body)])))
      res)))

(defn auth
  ([token] (auth* (http/get "/api/v1/session" (api-headers))))
  ([username password]
   (logout)
   (auth* (http/get "/api/v1/auth" {:basic-auth {:username username :password password}}))))

(defn authorized? []
  @(rf/subscribe [:logged?]))

(defn authorize! []
  (go
    (if-let [token (get-auth-token)]
      (let [res (<! (auth token))]
        (success? res))
      false)))

(defn register-user [email password]
  (go
    (let [res (<! (http/post "/api/v1/user" {:json-params {:email email :username email :password password}}))]
      (when (success? res)
        (<! (auth email password)))
      res)))

(defn reset-password-request [email]
  (call-api http/post "/password/reset-request" {:json-params {:user-email email}}))

(defn reset-password-confirm [password reset-key]
  (call-api http/post "/password/reset-confirm" {:json-params {:new-password password :reset-key reset-key}}))

(defn goto-login-page! []
  (util/go-url! "/#/login"))

(defn login-md [res]
  (if (auth-error? res)
    (do (goto-login-page!) nil)
    res))

(defn search-phrase [text]
  (call-api http/get "/phrases/search"
            (merge (api-headers) {:query-params {:q text}})
            login-md :body))

(defn count-phrase [text]
  (call-api http/get "/phrases/count"
            (merge (api-headers) {:query-params {:q text}})
            login-md :body))

(comment

  (go (println (<! (search-phrase "hello"))))
  (go (println (<! (count-phrase "hello"))))






  )
