(ns playphraseme.common.docs
  (:require [markdown.core :refer [md->html]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! put! chan] :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn html->hiccup [doc]
  [:div {:dangerouslySetInnerHTML {:__html doc}}])

(defn load-md-doc [doc-name]
  (go
    (-> (<! (http/get (str "/docs/" doc-name ".md")))
        :body
        md->html
        html->hiccup)))

(defn load-html-doc [doc-name]
  (go
    (-> (<! (http/get (str "/docs/" doc-name ".html")))
        :body
        html->hiccup)))

(comment
  (go (println "doc:" (<! (load-doc "guesttour"))))



  )
