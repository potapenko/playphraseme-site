(ns playphraseme.model
  (:require [re-frame.core :refer [dispatch subscribe reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))

(mcr/reg-sub-event :layout nil)
(mcr/reg-sub-event :responsive-scale 1)
(mcr/reg-sub-event :responsive-show-left-column? true)
(mcr/reg-sub-event :responsive-show-right-column? true)
(mcr/reg-sub-event :fullscreen false)
(mcr/reg-sub-event :mobile? false)
(mcr/reg-sub-event :docs [])
(mcr/reg-sub :page nil)
(mcr/reg-sub :params nil)
(mcr/reg-sub-event :user-roles [:basic])
(mcr/reg-sub-event :page-before-login nil)
(mcr/reg-sub-event :all-phrases-count nil)
(mcr/reg-sub-event :all-movies-count nil)
(mcr/reg-sub :auth-data {})
(mcr/reg-sub-event :autoplay-enabled true)

(reg-event-db
 :initialize-db
 (fn [_ _]
   {:page :search}))

(reg-event-db
 :set-active-page
 (fn [db [_ page params]]
   (println "params:" page params)
   (assoc db
          :page page
          :params params)))

(reg-event-db
 :locale
 [localstorage/model-store-md]
 (fn [db [_ docs]]
   (assoc db :locale docs)))

(mcr/reg-sub :locale :en)

(reg-event-db
 :set-auth-data
 (fn [db [_ value]]
   (if value
     (assoc db :auth-data
            (update value :permissions #(set (map keyword %))))
     (dissoc db :auth-data))))

(defn- user-roles [db]
  (get-in db [:auth-data :permissions] #{}))

(reg-sub
 :logged?
 (fn [db _]
   (-> db :auth-data nil? not)))

(reg-sub
 :has-user-role?
 (fn [db [_ role]]
   (-> ((user-roles db) role) nil? not)))

(reg-sub
 :desktop?
 (fn [db _]
   (not (:mobile? db))))

