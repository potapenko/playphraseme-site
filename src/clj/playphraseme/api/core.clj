(ns playphraseme.api.core
  (:require [mount.core :as mount]
            [playphraseme.api.queries.user.permission :refer :all]
            [playphraseme.api.handler]))

(defn start []
  (insert-permission! "admin")
  (insert-permission! "basic"))

(mount/defstate api
  :start (start))
