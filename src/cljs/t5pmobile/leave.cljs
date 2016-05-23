(ns t5pmobile.leave
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [t5pmobile.core :as t5pcore]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
            [clojure.string :as str]
            [om-bootstrap.button :as b]
  )
  (:import goog.History)
)

(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:leavetypes [] :leavecode "请选择"} ))


(defn OnGetLeaveTypes [response]

   (swap! app-state assoc :leavetypes  response)

   (.log js/console (:leavetypes @app-state)) 

)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn getLeaveTypes [data]
 (.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))

 
  (GET "http://localhost/T5PWebAPI/api/leavetype" {:handler OnGetLeaveTypes
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                            })
)



(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)




(defn onMount [data]
  (getLeaveTypes data)
  (jquery
   (fn []
     (-> (jquery "#datepicker")
       (.datepicker {})
     )
   )
  )
)


(defn alertselected [event]
   ;(js/alert (str event  "ClojureScript says 'Boo!'" ))
  (swap! app-state assoc :leavecode  event)

  (jquery
   (fn []
     (-> (jquery "#leavebtngroup")
       (.trigger  "click")
     )
   )
  )

)

(defcomponent leave-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (render [_]
    (dom/div {:className "panel panel-primary"}
      (dom/div {:className "panel-heading"}
        (dom/h3 {:className "panel-title"} "休假申请"
        )
      )

      (dom/div {:className "panel-body"}
        (dom/form {:className "form-horizontal"}
          (dom/div {:className "form-group"}
            (dom/label {:className "col-sm-2 control-label"} "类型"
              (dom/span {:style {:color "Red"}} "*")
            )
            (dom/div {:className "col-sm-10"}
              (b/button-group
                {:id "leavebtngroup" }
                (b/dropdown {:title (:leavecode @app-state) }
                  (map (fn [item]
                    (b/menu-item {:key (get item "leavecode")  :on-select (fn [e](alertselected e))   } (get item "chinese"))
                    )(:leavetypes data)
                  )                  
                )
              )
            )
          )


          (dom/div {:className "form-group"}
            (dom/label {:className "col-sm-2 control-label"} 
              (dom/span {:className "glyphicon glyphicon-time green"} "开始日期")
              (dom/span {:style {:color "Red"}} "*")
            )
            (dom/div {:className "col-sm-10"}
              (dom/input {:type "text" :id "datepicker"})
            )
          )
        )
      )
    )
  )
)




(sec/defroute leave-page "/leave" []
  (om/root leave-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))
