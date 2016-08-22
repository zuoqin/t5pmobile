(ns t5pmobile.newemplist (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            
  )
  (:import goog.History)
)

(enable-console-print!)

(defonce app-state (atom  {:view 2  :current "New Employee List"} ))
(def jquery (js* "$"))

(defn buildMainWrapper [data]
  (dom/div {:id "page-wrapper"}
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/h1 {:className "page-header"} "New Employee List")
      )
    )

    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"} "New Employee List")


          (dom/div {:className "panel-body"}
            (dom/div {:className "dataTable_wrapper"}
              (dom/table {:className "table table-striped table-bordered table-hover" :id "dataTables-example" :style {:width "100%"}}
                (dom/thead
                  (dom/tr
                    (dom/th "Rendering engine")
                    (dom/th "Browser")
                    (dom/th "Platform(s)")
                    (dom/th "Engine version")
                  )
                )
                (dom/tbody
                  (dom/tr {:className "odd gradeX"}
                    (dom/td "Trident")
                    (dom/td "Internet Explorer 4.0")
                    (dom/td "Win 95+")
                    (dom/td {:className "center"} "4")
                  )
                  (dom/tr {:className "odd gradeX"}
                    (dom/td "Trident2")
                    (dom/td "Internet Explorer 7.0")
                    (dom/td "Mac")
                    (dom/td {:className "center"} "10")
                  )

                  (dom/tr {:className "odd gradeX"}
                    (dom/td "Trident3")
                    (dom/td "Internet Explorer 8.0")
                    (dom/td "Linux")
                    (dom/td {:className "center"} "70")
                  )

                )
              )
            )
          )
        )
      )
    )
  
  )
)


(defn onMount [data]
 
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/newemplist)
  )
  (swap! app-state assoc-in [:sysmenus] (:sysmenus @t5pcore/app-state))
)



(defcomponent newemplist-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "Update happened") 
    (jquery
      (fn []
        (-> (jquery "#side-menu")
          (.metisMenu)
        )
      )
    )

    (jquery
      (fn []
        (-> (jquery "#dataTables-example")
          (.DataTable "responsive" "true")
        )
      )
    )
  )
  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      ;(dom/h1 "About Page")
      (buildMainWrapper data)
    )
  )
)




(sec/defroute newemplist-page "/newemplist" []
  (om/root newemplist-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))




