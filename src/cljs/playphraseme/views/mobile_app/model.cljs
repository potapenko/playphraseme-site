(ns playphraseme.views.mobile-app.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))

(mcr/reg-sub-event ::modal-img-src nil)
(mcr/reg-sub-event ::modal-img-horzontal? false)
