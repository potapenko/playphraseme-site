(ns playphraseme.views.article.view
  (:require [playphraseme.common.docs :as docs]
            [re-frame.core :as rf]
            [cljs.core.async :refer [<! put! chan] :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn article [doc-name]
  (fn[]
    (let [res (atom nil)]
      (go (rf/dispatch [:set-docs (<! (docs/load-md-doc doc-name))]))
      (fn []
        [:div
         @(rf/subscribe [:docs])]))))

(def guest-tour (article  "guest-tour-ru"))
