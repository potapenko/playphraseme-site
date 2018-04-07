(ns playphraseme.views.learn.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.reset-password.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn page []
  [:div "Learn page"])
