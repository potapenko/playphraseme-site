(ns playphraseme.views.article.view
  (:require [playphraseme.common.docs :as docs]
            [playphraseme.common.util :as util]
            [re-frame.core :as rf]
            [cljs.core.async :refer [<! put! chan] :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn article [doc-name]
  (fn[]
    (let [res (atom nil)]
      (go (rf/dispatch
           [:set-docs (<! (docs/load-md-doc (str  doc-name "-" (util/locale-name))))]))
      (fn []
        [:div.article-container
         @(rf/subscribe [:docs])]))))

(defn guest-tour []
  ^{:key (str "ques-tour-" (util/locale-name))}
  [(article  "guest-tour")])
