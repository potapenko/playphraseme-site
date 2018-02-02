(ns playphraseme.common.dates-util
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clj-time.predicates :as pr]
            [clojure.core.match :refer [match]]))

(defn date-time? [tm]
  (or
   (instance? org.joda.time.LocalDateTime tm)
   (instance? org.joda.time.LocalDate tm)))

(def formater (f/formatters :mysql))

(defn format-date [t]
  (f/unparse formater t))

(defn ->parse [s]
  (some-> (f/parse formater s)))

(defn now []
  (t/now))

(defn now-str []
  (-> (t/now) format-date))

(defn ->java-date [d]
  (c/to-date d))

(defn str->java-date [s]
  (-> s f/parse ->java-date))

(defn format-for-js [dt]
  (f/unparse (f/formatters :date-hour-minute-second-ms) dt))

(defn to-utc [dt]
  (t/to-time-zone dt (t/time-zone-for-offset 0)))

(comment
  (->java-date (->java-date (now-str)))

  (f/show-formatters))
