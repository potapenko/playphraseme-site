(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [playphraseme-site.config :refer [env]]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [playphraseme-site.figwheel :refer [start-fw stop-fw cljs]]
   [playphraseme-site.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'playphraseme.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'playphraseme.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))
