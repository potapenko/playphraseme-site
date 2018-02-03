(ns playphraseme.common.route
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [playphraseme.common.util :as util]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as string]
            [playphraseme.common.rest-api :as rest-api]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn goto-page!
  ([page]
   (goto-page! page nil))
  ([page params]
   (rf/dispatch [:set-active-page page params])))

(defn has-role? [role]
  @(rf/subscribe [:has-user-role? role]))

(defn page-params []
  @(rf/subscribe [:page-params]))

(defn goto-page-or-login!
  ([page checked-role] (goto-page-or-login! page nil checked-role))
  ([page params checked-role]
   (go
     (if (and (<! (rest-api/authorize!))
              (has-role? checked-role))
       (do
         (println "Logged")
         (goto-page! page params)
         true)
       (do
         (println "Not logged")
         (dispatch [:page-before-login {:page page :params params}])
         (goto-page! :login)
         false)))))

(defn goto-page-after-login! []
  (if-let [before @(subscribe [:page-before-login])]
    (let [{:keys [page params]} before]
      (dispatch [:page-before-login nil])
      (goto-page! page params))
    (util/go-url! "/#/")))
