(ns playphraseme.api.auth-resources.token-auth-backend
  (:require [playphraseme.app.config :refer [env]]
            [buddy.auth.backends :refer [jws]]
            [clojure.pprint :refer [pprint]]
            [mount.core :as mount :refer [defstate]]))

(defstate token-backend :start
  (jws {:secret (:auth-key env) :options {:alg :hs512}}))
