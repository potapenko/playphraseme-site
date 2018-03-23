(ns playphraseme.common.ui)

(defn spacer [param]
  (let [{:keys [w h]} (if (map? param) param {:w param :h param})]
    [:div {:style (merge {}
                         (when w {:width w})
                         (when h {:height h}))}]))

(defn flexer []
  [:div.grow ""])
