(ns ^:figwheel-no-load playphraseme.app
  (:require [playphraseme.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
