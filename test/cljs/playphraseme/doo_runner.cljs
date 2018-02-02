(ns playphraseme.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [playphraseme.core-test]))

(doo-tests 'playphraseme.core-test)

