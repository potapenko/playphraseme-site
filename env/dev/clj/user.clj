(ns user
  (:require 
            [mount.core :as mount]
            [playphraseme.figwheel :refer [start-fw stop-fw cljs]]
            [playphraseme.core :refer [start-app]]))

(defn start []
  (mount/start-without #'playphraseme.core/repl-server))

(defn stop []
  (mount/stop-except #'playphraseme.core/repl-server))

(defn restart []
  (stop)
  (start))


