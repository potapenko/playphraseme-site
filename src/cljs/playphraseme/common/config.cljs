(ns playphraseme.common.config
  (:require [playphraseme.common.util :as util]))

(def disable-search? (and
                                (or util/ios? util/android?)
                                js/window.searchOnMobile))


