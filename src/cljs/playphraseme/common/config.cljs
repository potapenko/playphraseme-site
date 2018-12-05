(ns playphraseme.common.config
  (:require [playphraseme.common.util :as util]))

(def mobile-layout? (or util/ios? util/android?))
