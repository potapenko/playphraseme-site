(ns playphraseme.common.spec
  (:require [playphraseme.common.util :as util]))

(defn- normalize-pred
  "Retains symbols in pred. Strips things like numbers.
  Example: (<= (count %) 10) gets (<= (count %))"
  [pred]
  (cond
    (seq? pred) (filter some? (map normalize-pred pred))
    (symbol? pred) pred))

(defn- dispatch [id {:keys [pred via]}]
  (cond-> [(normalize-pred pred)]
    id (conj id)
    (and id (seq via)) (conj via)))

(defmulti error-formatter
  "Dispatches on normalized pred and optional id and via of problem.
  Dispatches in this order:
  * [normalized-pred id via]
  * [normalized-pred id]
  * [normalized-pred]"
  {:arglists '([id problem])}
  dispatch)

(defmethod error-formatter :default
  [id problem]
  (let [dispatch-val (dispatch id problem)]
    (case (count dispatch-val)
      3 (error-formatter id (dissoc problem :via))
      2 (error-formatter nil problem)
      (pr-str (:pred problem)))))

(defmethod error-formatter '[(<= (count %))]
  [_ {:keys [pred val]}]
  (let [max-length (last pred)]
    (util/format
     "Expected maximal length id %s, but actual is %s."
     max-length (count val))))
