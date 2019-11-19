(ns playphraseme.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [playphraseme.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[playphraseme started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[playphraseme has shut down successfully]=-"))
   :middleware wrap-dev})
