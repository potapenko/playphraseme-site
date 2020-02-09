(defproject playphraseme "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.8.1"]
                 [cljs-ajax "0.8.0"]
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [com.google.guava/guava "27.0.1-jre"]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 [cprop "0.1.14"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [expound "0.7.2"]
                 [funcool/struct "1.4.0"]
                 [luminus-jetty "0.1.7"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.0"]
                 [metosin/compojure-api "1.1.13"]
                 [metosin/muuntaja "0.6.4"]
                 [metosin/reitit "0.3.9"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.webjars.npm/bulma "0.7.5"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [re-frame "0.10.8"]
                 [reagent "0.8.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.14"]

                 ;; custom clj

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
                 [diehard "0.6.0"]
                 [bk/ring-gzip "0.2.1"]
                 [ring-basic-authentication "1.0.5"]
                 [lib-noir "0.9.9"]
                 [clj-oauth2 "0.2.0"]
                 [malcontent "0.1.0-SNAPSHOT"]
                 [sitemap "0.4.0"]
                 [etaoin "0.3.6"]

                 ;; custom cljs

                 [soda-ash "0.4.0"]
                 [cljs-http "0.1.44"]
                 [camel-snake-kebab "0.4.0"]
                 [cljsjs/c3 "0.4.14-0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [bux "0.3.0"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 [re-frame-macros "0.1.14-SNAPSHOT"]
                 [cljs-await "1.0.1-SNAPSHOT"]
                 [secretary "1.2.3"]

                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot playphraseme.core

  :plugins [[lein-cljsbuild "1.1.7"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :server-logfile "log/figwheel-logfile.log"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild{:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-dir "target/cljsbuild/public/js"
                 :output-to "target/cljsbuild/public/js/app.js"
                 :source-map "target/cljsbuild/public/js/app.js.map"
                 :optimizations :advanced
                 :pretty-print false
                 :infer-externs true
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}
                 :externs ["react/externs/react.js"]}}}}

             :aot :all
             :uberjar-name "playphraseme.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.10"]
                                 [cider/piggieback "0.4.1"]
                                 [doo "0.1.11"]
                                 [figwheel-sidecar "0.5.19"]
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "2019-07-08"]
                                 [re-frisk "0.5.4.1"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [lein-doo "0.1.11"]
                                 [lein-figwheel "0.5.19"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :figwheel {:on-jsload "playphraseme.core/mount-components"
                                :open-urls ["http://localhost:3000/"]}
                     :compiler
                     {:output-dir "target/cljsbuild/public/js/out"
                      :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                      :optimizations :none
                      :preloads [re-frisk.preload]
                      :output-to "target/cljsbuild/public/js/app.js"
                      :asset-path "/js/out"
                      :source-map true
                      :main "playphraseme.app"
                      :pretty-print true}}}}


                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "playphraseme.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}

                  }
   :profiles/dev {}
   :profiles/test {}})
