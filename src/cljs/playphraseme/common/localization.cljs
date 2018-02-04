(ns playphraseme.common.localization
  (:require [re-frame.core :as rf]
            [playphraseme.locale.de :as de]
            [playphraseme.locale.en :as en]
            [playphraseme.locale.es :as es]
            [playphraseme.locale.fr :as fr]
            [playphraseme.locale.ru :as ru]
            [playphraseme.locale.ua :as ua]))

(defn ls [code]
  (let [locale (case @(rf/subscribe [:locale])
                 :de de/locale
                 :en en/locale
                 :es es/locale
                 :fr fr/locale
                 :ru ru/locale
                 :ua ua/locale)]
    (-> locale code)))

