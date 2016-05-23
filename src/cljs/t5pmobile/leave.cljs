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
            [om-bootstrap.panel :as p]
            
  )
  (:import goog.History)
)

(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:leavetypes [] :leavecode "请选择"} ))

(defn leaves-to-map [leave]
  (let [     
      newdata {(keyword (get leave "leavecode")) leave }
    ]
    newdata
  )
)

(defn leaves-to-leavecodes [leave]
  (let [     
      newdata {:leavecode (get leave "leavecode") :name (get leave "name") }
    ]
    newdata
  )
  
)

(defn OnGetLeaveTypes [response]
  ( let [ 
    newdata (map leaves-to-map response)
    leavecodes (map leaves-to-leavecodes response)
  ]
     
     
     (swap! app-state assoc :leavetypes  (into {} newdata) )
     (swap! app-state assoc :leavecodes leavecodes) 
  )
   

   ;(.log js/console (:leavetypes @app-state)) 

)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)







(defn getLeaveTypes [data]
 (.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))

 
  (GET "http://localhost/T5PWebAPI/api/leavetype/leavetype2?type=0" {:handler OnGetLeaveTypes
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

(defn setdatepickers []
  (map (fn [text]
    (jquery
     (fn []
       (-> (jquery (str "#" (get text "fieldcode")) )
         (.datepicker {})
       )      
     )
    )
    (.log js/console (str "#" (get text "fieldcode") ))        
    )  (get ((keyword (:leavecode @app-state)) (:leavetypes @app-state)) "fields" ) 
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
  
   ;(.log js/console (str "#" "leavefromdate" )) 

    (jquery
     (fn []
       (-> (jquery (str "#" "leavefromdate") )
         (.datepicker {})
       )      
     )
    )
)

(defcomponent leave-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "did updated!!!!!!!!!!!!!" )  
    (setdatepickers)
  )
  (render [_]
    (p/panel (merge {:header (dom/h3 "休假申请" )} {:bs-style "primary"}
      
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
                    (b/menu-item {:key (:leavecode item)  :on-select (fn [e](alertselected e))   } (:name item))
                    )(:leavecodes data)
                  )                  
                )
              )
            )
          )

          (map (fn [text]
            (dom/div {:className "form-group"}
              (dom/label {:className "col-sm-2 control-label"} 
                (dom/span {} (get text "name"))
                (if ( = (get text "required") true ) 
                  (dom/span {:style {:color "Red"}} "*")
                )
              )
              (dom/div {:className "col-sm-10"}
                (cond 
                  (= (get text "fieldtype") 1)
                    (dom/input {:type "text" :id (get text "fieldcode")})
                  (= (get text "fieldtype") 0)
                    (dom/input {:type "text" :id (get text "fieldcode")})
                )
                
                
              )
            )            
            )  (get ((keyword (:leavecode data)) (:leavetypes @app-state)) "fields" ) 
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
