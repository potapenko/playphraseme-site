(ns playphraseme.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    {:page :search}))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

;;subscriptions

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))
