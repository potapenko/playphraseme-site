(ns playphraseme.views.modal.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.ui :as ui :refer [flexer spacer]]
            [playphraseme.common.shared :as shared]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.mobile-app.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))
