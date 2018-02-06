(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage]))




(comment
 (reg-sub
  :name
  (fn [db _]
    (::name db)))

 (reg-event-db
  :name
  (fn [db [_ value]]
    (assoc db ::name value))))
