(ns playphraseme.app
  (:require [playphraseme.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
