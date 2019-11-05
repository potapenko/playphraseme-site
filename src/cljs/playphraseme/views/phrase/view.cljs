(ns playphraseme.views.phrase.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.shared :as shared]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.reset-password.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn page []
  (r/create-class
   {:component-did-mount
    (fn [])
    :reagent-render
    (fn []
      [:div.page-container
       [:h1 "Phrase page"]
       [shared/under-construction]])}))
