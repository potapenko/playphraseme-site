(ns playphraseme.app.routes
  (:require [playphraseme.app.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [playphraseme.app.config :refer [env]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [playphraseme.common.util :as util]))

(defn home-page [params]
  (layout/render "home.html"
                 (merge
                  {:facebook-app-id (:facebook-client-id env)}
                  params)))

(defn prepare-search-text [q]
  (some-> q
          string/trim
          (string/replace #"[_+]" " ")))

(defroutes home-routes
  (GET "/" [q :as request]
       (home-page {:q (prepare-search-text q)}))
  (GET "/search" [q :as request]
       (home-page {:q (prepare-search-text q)}))
  (GET "/phrase/:phrase" [phrase]
       (clojure.pprint/pprint phrase)
       (home-page {:q (prepare-search-text phrase)})))

