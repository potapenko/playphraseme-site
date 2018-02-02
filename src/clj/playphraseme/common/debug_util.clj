(ns playphraseme.common.debug-util
  (:require [clojure.string :as string]
            [playphraseme.db.core :as db]))

(defmacro ... [& vars]
  (let [vars (if (-> vars first vector?) (first vars) vars)]
    (let [m (apply merge
                   (for [v vars]
                     {(keyword (str v)) v}))]
      m)))

(defn remove-fn-text [s]
  (string/replace s #"\(fn\* \[\] \((.+)\)\)" "$1"))

(defmacro ->time [f]
  `(let [start# (. System (nanoTime))
         ret#   (~f)]
     (prn (-> '~f str remove-fn-text)
          (str "Elapsed time: "
               (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro ->time-> [arg f]
  `(let [start# (. System (nanoTime))
         ret#   (~f ~arg)]
     (prn '~f (str "Elapsed time: "
                   (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro ->time->> [f arg]
  `(let [start# (. System (nanoTime))
         ret# (~f ~arg)]
     (prn '~f (str "Elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro catch-error
  [name data & body]
  {:pre [(vector? data)]}
  `(try
     ~@body
     (catch Throwable e#
       (db/add-doc "logs" {:name      ~name
                           :data      (... ~data)
                           :exception (with-out-str
                                        (clojure.stacktrace/print-stack-trace e#))})
       nil)))
