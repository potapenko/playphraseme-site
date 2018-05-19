(defproject playphraseme-site "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[cider/cider-nrepl "0.15.1"]
                 [clj-time "0.14.0"]
                 [cljs-ajax "0.7.2"]
                 [com.google.guava/guava "20.0"]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 [compojure "1.6.0"]
                 [cprop "0.1.11"]
                 [funcool/struct "1.1.0"]
                 [luminus-immutant "0.2.3"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.1"]
                 [metosin/compojure-api "1.1.11"]
                 [metosin/muuntaja "0.3.2"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/tools.reader "1.1.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/bootstrap "4.0.0"]
                 [org.webjars/font-awesome "5.0.6"]
                 [re-frame "0.10.2"]
                 [reagent "0.7.0"]
                 [cljsjs/react "15.6.2-0"]
                 [cljsjs/react-dom "15.6.2-0"]
                 [cljsjs/react-dom-server "15.6.2-0"]
                 [reagent-utils "0.2.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [secretary "1.2.3"]
                 [selmer "1.11.1"]
                 [clj-oauth "1.5.5"]

                 ;; custom clj

                 [cheshire "5.7.1"]
                 [base64-clj "0.1.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.async "0.3.443"]
                 [clj-http "3.7.0"]
                 [com.draines/postal "2.0.2"]
                 [ring-cors "0.1.11"]
                 [clj-time "0.14.0"]
                 [buddy "2.0.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [clojurewerkz/money "1.10.0"]
                 [net.polyc0l0r/bote "0.1.0"]
                 [diehard "0.6.0"]
                 [metrics-clojure "2.10.0"]
                 [metrics-clojure-graphite "2.10.0"]
                 [bk/ring-gzip "0.2.1"]
                 [ring-basic-authentication "1.0.5"]
                 [lib-noir "0.9.9"]
                 [clj-oauth2 "0.2.0"]
                 [malcontent "0.1.0-SNAPSHOT"]

                 ;; custom cljs

                 [soda-ash "0.4.0"]
                 [cljs-http "0.1.44"]
                 [camel-snake-kebab "0.4.0"]
                 [cljsjs/c3 "0.4.14-0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [bux "0.3.0"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 [re-frame-macros "0.1.14-SNAPSHOT"]
                 [cljs-await "1.0.1-SNAPSHOT"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot playphraseme.core
  :plugins [[lein-cprop "1.0.3"]
            [lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.14"]
            [lein-immutant "2.1.0"]
            [lein-dotenv "RELEASE"]
            [lein-midje "3.1.3"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port       7002
   :server-ip        "192.168.0.200"
   :css-dirs         ["resources/public/css"]
   :nrepl-middleware
   [cemerick.piggieback/wrap-cljs-repl cider.nrepl/cider-middleware]}
  :profiles
  {:uberjar {:omit-source    true
             :prep-tasks     ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
             {:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-to     "target/cljsbuild/public/js/app.js"
                 :optimizations :advanced
                 :pretty-print  false
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}
                 :externs       ["react/externs/react.js" "resources/public/js/externs.js"]}}}}
             :aot            :all
             :uberjar-name   "playphraseme.jar"
             :source-paths   ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev  [:project/dev :profiles/dev]
   :test [:project/dev :project/test :profiles/test]

   :project/dev {:dependencies [[prone "1.1.4"]
                                [ring/ring-mock "0.3.1"]
                                [ring/ring-devel "1.6.2"]
                                [pjstadig/humane-test-output "0.8.3"]
                                [binaryage/devtools "0.9.7"]
                                [com.cemerick/piggieback "0.2.2"]
                                [midje "1.8.3"]
                                [doo "0.1.8"]
                                [figwheel-sidecar "0.5.14"]]
                 :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                                [lein-doo "0.1.8"]
                                [lein-figwheel "0.5.14"]
                                [org.clojure/clojurescript "1.9.946"]]
                 :cljsbuild
                 {:builds
                  {:app
                   {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                    :figwheel     {:on-jsload      "playphraseme.core/mount-components"
                                   :websocket-host "192.168.0.200"}
                    :compiler
                    {:main          "playphraseme.app"
                     :asset-path    "/js/out"
                     :output-to     "target/cljsbuild/public/js/app.js"
                     :output-dir    "target/cljsbuild/public/js/out"
                     :source-map    true
                     :optimizations :none
                     :pretty-print  true}}}}

                 :doo            {:build "test"}
                 :source-paths   ["env/dev/clj"]
                 :resource-paths ["env/dev/resources"]
                 :repl-options   {:init-ns user :init (start-app [])}
                 :injections     [(require 'pjstadig.humane-test-output)
                                  (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to     "target/test.js"
                      :main          "playphraseme.doo-runner"
                      :optimizations :whitespace
                      :pretty-print  true}}}}

                  }
   :profiles/dev  {}
   :profiles/test {}})
