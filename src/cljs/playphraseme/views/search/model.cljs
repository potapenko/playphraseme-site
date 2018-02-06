(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage]))


(reg-sub
 ::stoped
 (fn [db _]
   (get db ::stoped false)))

(reg-event-db
 ::stoped
 (fn [db [_ value]]
   (assoc db ::stoped value)))


(reg-sub
 ::search-text
 (fn [db _]
   (get db ::search-text "")))

(reg-event-db
 ::search-text
 (fn [db [_ value]]
   (assoc db ::search-text value)))

(reg-sub
 ::search-count
 (fn [db _]
   (get db ::search-count 0)))

(reg-event-db
 ::search-count
 (fn [db [_ value]]
   (assoc db ::search-count value)))

(comment

 (reg-sub
  ::name
  (fn [db _]
    (::name db)))

 (reg-event-db
  ::name
  (fn [db [_ value]]
    (assoc db ::name value)))

 )
