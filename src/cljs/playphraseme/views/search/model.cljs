(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage]))


(reg-sub
 ::stopped
 (fn [db _]
   (get db ::stopped false)))

(reg-event-db
 ::stopped
 (fn [db [_ value]]
   (assoc db ::stopped value)))


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

(defn- add-indexes [coll]
  (->> coll (map-indexed (fn [i e] (assoc e :index i))) vec))

(defn add-phrases-indexes [phrases]
  (->> phrases
       add-indexes
       (map #(update % :words add-indexes))))

(reg-event-db
 ::search-result
 (fn [db [_ value]]
   (-> db
    (assoc
     ::phrases (:phrases value)
     ::search-count (:count value)
     ::current-phrase-index 0
     ::suggestions (:suggestions value))
    (update ::phrases add-phrases-indexes))))

(reg-event-db
 ::search-result-append
 (fn [db [_ value]]
   (-> db
       (update ::phrases concat (:phrases value))
       (update ::phrases add-phrases-indexes))))

(reg-sub
 ::current-phrase-index
 (fn [db [_]]
   (get db ::current-phrase-index)))

(reg-event-db
 ::current-phrase-index
 (fn [db [_ value]]
   (assoc db ::current-phrase-index value)))

(reg-event-db
 ::next-phrase
 (fn [db [_]]
   (let [current       (::current-phrase-index db)
         count-phrases (::search-count db)]
     (assoc db ::current-phrase-index (min (dec count-phrases) (inc current))))))

(reg-sub
 ::current-word-index
 (fn [db [_]]
   (get db ::current-word-index 0)))

(reg-event-db
 ::current-word-index
 (fn [db [_ value]]
   (assoc db ::current-word-index value)))

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
