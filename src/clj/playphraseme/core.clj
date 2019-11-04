(ns playphraseme.core
  (:gen-class)
  (:require [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [luminus.http-server :as http]
            [playphraseme-site.nrepl :as nrepl]
            [mount.core :as mount]
            playphraseme.api.core
            [playphraseme.app.config :refer [env]]
            [playphraseme.app.handler :as handler]
            playphraseme.db.users-db))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
  http-server
  :start
  (http/start
   (-> env
       (assoc  :handler (handler/app))
       (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
       (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (log/info "Listening on port:" (-> env :port))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (start-app args))
