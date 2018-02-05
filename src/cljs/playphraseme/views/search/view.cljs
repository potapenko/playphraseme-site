(ns playphraseme.views.search.view)

(defn page []
  [:div.search-container
   [:div.search-content
    [:div.video-player-container ""]
    [:div.search-ui-container ""]
    [:div.search-results-container
     [:div.results
      (doall
       (for [x (range 100)]
         ^{:key (str "elem-" x)}
         [:div.one-result (str "One result: " x)]))]]]])

