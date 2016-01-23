(ns uxbox.ui.workspace.recent-colors
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [cats.labs.lens :as l]
            [uxbox.locales :refer (tr)]
            [uxbox.rstore :as rs]
            [uxbox.state :as st]
            [uxbox.data.workspace :as dw]
            [uxbox.ui.icons :as i]
            [uxbox.ui.mixins :as mx]
            [uxbox.ui.dom :as dom]
            [uxbox.ui.workspace.base :as wb]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lenses
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static ^:private shapes-by-id
  (as-> (l/key :shapes-by-id) $
    (l/focus-atom $ st/state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private ^:static toggle-colorpalette
  #(rs/emit! (dw/toggle-tool :workspace/colorpalette)))

(defn- count-color
  [state shape prop]
  (let [color (prop shape)]
    (if (contains? state color)
      (update state color inc)
      (assoc state color 1))))

(defn- calculate-colors
  [shapes]
  (as-> (comp
         #(count-color %1 %2 :fill)
         #(count-color %1 %2 :stroke)) $
    (reduce $ {} shapes)
    (sort-by second (into [] $))
    (take 5 (map first $))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- recent-colors-render
  [own {:keys [page id] :as shape} callback]
  (let [shapes-by-id (rum/react shapes-by-id)
        shapes (->> (vals shapes-by-id)
                    (filter #(= (:page %) page)))
        colors (calculate-colors shapes)]
    (html
     [:div
      [:span (tr "ds.recent-colors")]
      [:div.row-flex
       (for [color colors]
         [:span.color-th {:style {:background color}
                          :on-click (partial callback color)}])
       (for [i (range (- 5 (count colors)))]
         [:span.color-th])

       [:span.color-th.palette-th {:on-click toggle-colorpalette}
        i/palette]]])))

(def ^:static recent-colors
  (mx/component
   {:render recent-colors-render
    :name "recent-colors"
    :mixins [mx/static rum/reactive]}))
