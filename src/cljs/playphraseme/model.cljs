(ns playphraseme.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage]))

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
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(reg-event-db
 :locale
 [localstorage/model-store-md]
 (fn [db [_ docs]]
   (assoc db :locale docs)))

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-sub
 :params
 (fn [db _]
   (:params db)))

(reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(reg-sub
 :locale
 (fn [db _]
   (get db :locale :en)))

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
 :user-roles
 (fn [db _]
   (user-roles db)))

(reg-sub
 :logged?
 (fn [db _]
   (-> db :auth-data nil? not)))

(reg-sub
 :has-user-role?
 (fn [db [_ role]]
   (-> ((user-roles db) role) nil? not)))

(reg-sub
 :page-before-login
 (fn [db _]
   (get db :page-before-login)))

(reg-event-db
 :page-before-login
 (fn [db [_ value]]
   (assoc db :page-before-login value)))

(reg-sub
 :mobile?
 (fn [db _]
   (:mobile? db)))

(reg-event-db
 :mobile?
 (fn [db [_ value]]
   (assoc db :mobile? value)))

(reg-sub
 :desktop?
 (fn [db _]
   (not (:mobile? db))))

(reg-sub
 :responsive-scale
 (fn [db _]
   (get db :responsive-scale 1)))

(reg-event-db
 :responsive-scale
 (fn [db [_ value]]
   (assoc db :responsive-scale value)))

(reg-sub
 :responsive-show-left-column?
 (fn [db _]
   (get db :responsive-show-left-column? true)))

(reg-event-db
 :responsive-show-left-column?
 (fn [db [_ value]]
   (assoc db :responsive-show-left-column? value)))

(reg-sub
 :responsive-show-right-column?
 (fn [db _]
   (get db :responsive-show-right-column? true)))

(reg-event-db
 :responsive-show-right-column?
 (fn [db [_ value]]
   (assoc db :responsive-show-right-column? value)))
