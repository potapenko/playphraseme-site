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

(reg-sub
 ::phrases
 (fn [db [_]]
   (get db ::phrases)))

(reg-event-db
 ::phrases
 (fn [db [_ value]]
   (assoc db ::phrases value)))

(reg-sub
 ::suggestions
 (fn [db [_]]
   (get db ::suggestions)))

(reg-event-db
 ::suggestions
 (fn [db [_ value]]
   (assoc db ::suggestions value)))

(reg-event-db
 ::search-result
 (fn [db [_ value]]
   (assoc db
          ::phrases (->> value :phrases (map-indexed (fn [i e] (assoc e :index i))))
          ::search-count (:count value)
          ::suggestions (:suggestions value))))

(reg-event-db
 ::search-result-append
 (fn [db [_ value]]
   (update db ::phrases concat (:phrases value))))

(reg-sub
 ::current-phrase-index
 (fn [db [_]]
   (get db ::current-phrase-index)))

(reg-event-db
 ::current-phrase-index
 (fn [db [_ value]]
   (assoc db ::current-phrase-index value)))

(comment
 (reg-sub
  ::name
  (fn [db [_]]
    (get db ::name)))

 (reg-event-db
  ::name
  (fn [db [_ value]]
    (assoc db ::name value)))


 )
