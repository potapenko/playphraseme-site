(ns playphraseme.views.register.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))

(mcr/reg-sub-event ::error-message nil)
(mcr/reg-sub-event ::email nil)
(mcr/reg-sub-event ::confirm-password nil)
(mcr/reg-sub-event ::password nil)

