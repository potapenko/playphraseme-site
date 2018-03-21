(ns playphraseme.common.macros
  (:require [clojure.string :as string]))

(defmacro ...
  "A macro that turns a list of variables into a map"
  [& vars]
  (let [vars (if (-> vars first vector?) (first vars) vars)]
    (let [m (apply merge
                   (for [v vars]
                     {(keyword (str v)) v}))]
      m)))

(defmacro ->time
  "Own time implementation - output information about the function and the time of its execution"
  [f]
  `(let [start# (. System (nanoTime))
         ret#   (~f)]
     (prn (-> '~f str remove-fn-text)
          (str "Elapsed time: "
               (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro ->time->
  "->time with arguments (for -> macro)"
  [arg f]
  `(let [start# (. System (nanoTime))
         ret#   (~f ~arg)]
     (prn '~f (str "Elapsed time: "
                   (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro ->time->>
  "->time with arguments (for ->> macro)"
  [f arg]
  `(let [start# (. System (nanoTime))
         ret# (~f ~arg)]
     (prn '~f (str "Elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))

(defmacro reg-sub-event [name default-value]
  `(do
     (re-frame.core/reg-event-db
      ~name
      (fn [db# [_ value#]]
        (assoc db# ~name value#)))
     (re-frame.core/reg-sub
      ~name
      (fn [db# [_]]
        (get db# ~name ~default-value)))))

(defmacro reg-sub [name default-value]
  `(re-frame.core/reg-sub
    ~name
    (fn [db# [_]]
      (get db# ~name ~default-value))))

(defmacro reg-event [name]
  `(re-frame.core/reg-event-db
    ~name
    (fn [db# [_ value#]]
      (assoc db# ~name value#))))
