(ns playphraseme.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[playphraseme started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[playphraseme has shut down successfully]=-"))
   :middleware identity})
