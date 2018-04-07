(ns playphraseme.views.support.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))


(mcr/reg-sub-event ::error-message nil)
(mcr/reg-sub-event ::message nil)
