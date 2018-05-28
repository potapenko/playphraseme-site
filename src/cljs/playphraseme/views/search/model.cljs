(ns playphraseme.views.search.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :refer [model-store-md]]
            [playphraseme.common.util :as util]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(mcr/reg-sub ::search-count 0)
(mcr/reg-sub ::phrases nil)
(mcr/reg-sub ::suggestions nil)
(mcr/reg-sub-event ::current-word-index 0)
(mcr/reg-sub ::current-suggestion-index nil)

(mcr/reg-sub-event :playing false)
(mcr/reg-sub-event :stopped true)
(mcr/reg-sub-event :search-text "")
(mcr/reg-sub-event :current-phrase-index nil)
(mcr/reg-sub-event :autoplay-enabled nil)
(mcr/reg-sub-event :first-search true)
(mcr/reg-event-update :search-count 0 inc)
(mcr/reg-sub :search-count 0)
(mcr/md-reg-sub-event [model-store-md] ::audio-muted true)
(mcr/md-reg-sub-event [model-store-md] ::audio-volume 0.3)
(mcr/reg-sub-event ::input-focused? false)
(mcr/reg-sub-event ::next-word-suggestion nil)

(defn- add-indexes [coll]
  (->> coll (map-indexed (fn [i e] (assoc e :index i))) vec))

(defn- add-phrases-indexes [phrases]
  (->> phrases
       add-indexes
       (map #(update % :words add-indexes))
       vec))

(defn- get-phrase-by-id [db id]
  (->> db ::phrases (drop-while #(not= (:id %) id)) first))

(defn- remove-first-phrase [phrases first-phrase]
  (let [{:keys [id index]} first-phrase]
    (->>
     phrases
     (remove (fn [e]
               (and
                (-> e :id (= id))
                (-> e :index (not= 0)))))
     vec)))

(reg-event-db
 ::search-result
 (fn [db [_ {:keys [phrases count suggestions next-word-suggestion]}]]
   (-> db
       (assoc
        ::phrases phrases
        ::search-count count
        :current-phrase-index 0
        ::current-suggestion-index nil
        ::next-word-suggestion next-word-suggestion
        ::suggestions suggestions)
       (update ::suggestions add-indexes)
       (update ::phrases add-phrases-indexes)
       (update ::phrases remove-first-phrase (first phrases)))))

(reg-event-db
 ::search-result-append
 (fn [db [_ value]]
   (-> db
       (update ::phrases concat (-> value
                                    :phrases
                                    (remove-first-phrase (-> db :phrases first))))
       (update ::phrases add-phrases-indexes))))

(reg-event-db
 ::next-phrase
 (fn [db [_]]
   (let [current       (:current-phrase-index db)
         count-phrases (-> db ::phrases count)
         next-current  (inc current)]
     (assoc db
            :current-phrase-index (if (> next-current (dec count-phrases))
                                    0 next-current)))))

(reg-event-db
 ::prev-phrase
 (fn [db [_]]
   (let [current       (:current-phrase-index db)
         count-phrases (-> db ::phrases count)]
     (assoc db :current-phrase-index (max 0 (dec current))))))

(reg-event-db
 ::favorite-phrase
 (fn [db [_ id value]]
   (let [index (:index (get-phrase-by-id db id))]
     (if-not (nil? index)
         (assoc-in db
                   [::phrases index :favorited]
                   value)
         db))))

(reg-event-db
 ::next-suggestion
 (fn [db [_]]
   (let [current           (::current-suggestion-index db)
         count-suggestions (-> db ::suggestions count)]
     (if (nil? current)
       (assoc db ::current-suggestion-index 0)
       (assoc db ::current-suggestion-index (min
                                             (dec count-suggestions)
                                             (inc current)))))))

(reg-event-db
 ::prev-suggestion
 (fn [db [_]]
   (let [current           (::current-suggestion-index db)
         count-suggestions (-> db ::suggestions count)]
     (if (nil? current)
       (assoc db ::current-suggestion-index 0)
       (assoc db ::current-suggestion-index (max 0 (dec current)))))))
