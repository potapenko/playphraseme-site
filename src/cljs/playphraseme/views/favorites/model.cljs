(ns playphraseme.views.favorites.model
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [playphraseme.common.localstorage :as localstorage])
  (:require-macros [re-frame-macros.core :as mcr]))

(mcr/reg-sub-event ::favorites-list [])

(defn add-indexes [favorites]
  (->> favorites
       (map-indexed (fn [index e]
                      (assoc e :index index)))
       vec))

(reg-event-db
 ::favorites-append
 (fn [db [_ {:keys [count favorites]}]]
   (-> db
       (update ::favorites-list concat favorites)
       (update ::favorites-list add-indexes)
       (assoc ::count count))))
