(ns playphraseme.common.ga)

(def tracking-code "UA-56061701-1")

(defn track [page]
  (js/ga "set" "page" page)
  (js/ga "send" "pageview"))

(defn start []
  (js/ga "create" tracking-code "auto"))
