(ns playphraseme.views.support.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.common.shared :as shared]
            [playphraseme.common.rest-api :as rest-api :refer [success? error?]]
            [playphraseme.views.support.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn form-data []
  [@(rf/subscribe [::model/message])])

(defn form-completed? []
  (->> (form-data) (remove string/blank?) count (= 1)))

(defn clear-error! []
  (rf/dispatch [::model/error-message nil]))

(defn on-send [e]
  (-> e .preventDefault)
  (clear-error!)
  (when (form-completed?)
    (go
      #_(let [res (<! (rest-api/support-request (first (form-data))))]
        (if (success? res)
          (rf/dispatch [::model/message (-> res :body :message)])
          (rf/dispatch [::model/error-message (:error res)])))))
  false)


#_(defn page []
  [:form {:on-submit on-send}
   [:div.page-title "Send message to us"]
   [:div
    (when-let [error-message @(rf/subscribe [::model/error-message])]
      [:div.alert.alert-danger {:role "alert"} error-message])
    [:div.d-flex
     [:textarea.input {:type       "textarea"
                       :id         "input-message"
                       :style      {:height "150px"}
                       :rows       6
                       :value      (-> (form-data) first)
                       :on-change  (fn [e]
                                     (clear-error!)
                                     (rf/dispatch [::model/message (-> e .-target .-value)]))
                       :auto-focus true}]]]
   [:div.d-flex
    [:div.grow]
    [:button.form-button {:type "submit" :disabled (not (form-completed?))} "Send message"]
    [:div.grow]]])

(defn page []
  (r/create-class
   {:component-did-mount
    (fn [])
    :reagent-render
    (fn []
      [:div.page-container
       [:h1 "Support page"]
       [shared/under-construction]])}))
