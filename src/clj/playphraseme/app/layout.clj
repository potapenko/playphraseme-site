(ns playphraseme.app.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :refer [content-type ok]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [playphraseme.api.queries.config :as config]
            [playphraseme.app.config :refer [env]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [playphraseme.common.google-search :as google-search])
  (:import [java.net URLDecoder]))

(declare ^:dynamic *app-context*)
(parser/set-resource-path!  (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (let [{:keys [q]} params]
   (content-type
    (ok
     (parser/render-file
      template
      (assoc params
             :page template
             :csrf-token *anti-forgery-token*
             :search-on-mobile (if (or (config/get-config :search-on-mobile) (not (nil? q))) "true" "false")
             :servlet-context *app-context*
             :page-title (google-search/generate-page-title q)
             :page-description (google-search/generate-page-description q)
             :video-url (google-search/get-video-url q)
             :page-static-content (google-search/generate-page-static-content q)
             :searched-phrase (some-> q URLDecoder/decode)
             :rel-canonical (google-search/generate-rel-canonical q))))
    "text/html; charset=utf-8")))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})
