(ns playphraseme.views.login.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))

(mcr/reg-sub-event ::error-message nil)
(mcr/reg-sub-event ::username nil)
(mcr/reg-sub-event ::password nil)
