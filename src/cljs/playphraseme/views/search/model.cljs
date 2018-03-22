(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.macros :as mcr]))

(mcr/reg-sub-event ::stopped false)
(mcr/reg-sub-event ::show-ios-play false)
(mcr/reg-sub-event ::search-text "")
(mcr/reg-sub-event ::search-count 0)
(mcr/reg-sub-event ::phrases nil)
(mcr/reg-sub-event ::suggestions nil)
(mcr/reg-sub-event ::current-phrase-index nil)
(mcr/reg-sub-event ::current-word-index 0)
(mcr/reg-sub ::current-suggestion-index nil)

(defn- add-indexes [coll]
  (->> coll (map-indexed (fn [i e] (assoc e :index i))) vec))

(defn- add-phrases-indexes [phrases]
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
        ::current-suggestion-index nil
        ::suggestions (:suggestions value))
       (update ::suggestions add-indexes)
       (update ::phrases add-phrases-indexes))))

(reg-event-db
 ::search-result-append
 (fn [db [_ value]]
   (-> db
       (update ::phrases concat (:phrases value))
       (update ::phrases add-phrases-indexes))))

(reg-event-db
 ::next-phrase
 (fn [db [_]]
   (let [current       (::current-phrase-index db)
         count-phrases (-> db ::phrases count)]
     (assoc db ::current-phrase-index (min (dec count-phrases) (inc current))))))

(reg-event-db
 ::prev-phrase
 (fn [db [_]]
   (let [current       (::current-phrase-index db)
         count-phrases (-> db ::phrases count)]
     (assoc db ::current-phrase-index (max 0 (dec current))))))

(reg-event-db
 ::next-suggestion
 (fn [db [_]]
   (let [current       (::current-suggestion-index db)
         count-suggestions (-> db ::suggestions count)]
     (if (nil? current)
       (assoc db ::current-suggestion-index 0)
       (assoc db ::current-suggestion-index (min (dec count-suggestions) (inc current)))))))

(reg-event-db
 ::prev-suggestion
 (fn [db [_]]
   (let [current       (::current-suggestion-index db)
         count-suggestions (-> db ::suggestions count)]
     (if (nil? current)
       (assoc db ::current-suggestion-index 0)
       (assoc db ::current-suggestion-index (max 0 (dec current)))))))
