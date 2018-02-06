(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage]))


(reg-sub
 :play
 (fn [db _]
   (get db :play false)))

(reg-event-db
 :play
 (fn [db [_ value]]
   (assoc db :play value)))


(comment

 (reg-sub
  :name
  (fn [db _]
    (:name db)))

 (reg-event-db
  :name
  (fn [db [_ value]]
    (assoc db :name value)))

 )
