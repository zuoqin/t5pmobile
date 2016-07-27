(ns t5pmobile.leave
  (:require-macros [cljs.core.async.macros :refer [go]])
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
            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            [om-bootstrap.input :as i]
            [cljs-time.core :as tm]
            [cljs-time.format :as tf]
            ;;[domina :as dominalib]
            ;;[domina.events :as dominaevents]
            
  )
  (:import goog.History)
)


(def ch (chan (dropping-buffer 2)))
(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:view 0 :leavecode "" :leavetypes [] :leaveapp {:leavecode "请选择"} } ))



;(swap! app-state assoc-in [:leavetypes] {})
;(swap! app-state assoc-in [:leavecodes] ())
(defonce fieldnum (atom 0))

(defn fields-to-map [fielddef]
  (let [
      newdata {(keyword (get fielddef "fieldcode")){
        :name (get fielddef "name") :fieldtype (get fielddef "fieldtype")
        :required (get fielddef "required") :num @fieldnum :timeformat (get fielddef "timeformat")
        :hide (get fielddef "hide") :values (get fielddef "values")} }
    ]
    (swap! fieldnum inc)
    newdata
  )
)

(defn leaves-to-map [leave]
  (let [     
      newdata {
       (keyword (get leave "leavecode")) {
         :fields (into {} (doall  (map fields-to-map (get leave "fields"))  ) )  
         :name (get leave "name")} 
       }
    ]
    (reset! fieldnum 0)
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

(defn handle-chkb-change [e]
  ;(.log js/console (.. e -target -id) )  
  (.log js/console "The change ....")
  (.stopPropagation e)
  (.stopImmediatePropagation (.. e -nativeEvent) )
  (swap! app-state assoc-in [:leaveapp (keyword  (.. e -currentTarget -id) )] 
    (if (= true (.. e -currentTarget -checked)  ) 1 0)
  ) 
  ;(set! (.-checked (.. e -currentTarget)) false)
  ;(dominalib/remove-attr!  (.. e -currentTarget) :checked)
  ;;(dominalib/set-attr!  (.. e -currentTarget) :checked true)
)

(defn handle-chkb-click [e]
  ;(.log js/console (.. e -target -id) )  
  (.log js/console "The click ....")
  (.stopPropagation e)
  (.stopImmediatePropagation (.. e -nativeEvent) )
  ;(set! (.-checked (.. e -currentTarget)) false) 
  ;(dominalib/remove-attr!  (.. e -currentTarget) :checked)
  ;;(dominalib/set-attr!  (.. e -currentTarget) :checked true)
  ;;(dominaevents/stop-propagation e)
  ;;(dominaevents/prevent-default e)
)


(defn handle-change [e]
  ;(.log js/console (.. e -target -id) )  
  (.log js/console "The run ....")
)

(def custom-formatter1 (tf/formatter "MMM dd yyyy hh:mm:ss"))
(def custom-formatter2  (tf/formatter (:datemask (:User @t5pcore/app-state)) ))

(def custom-formatter3 (tf/formatter "yyyyMMdd"))

(defn setdatepicker [field]
  (if (and
      (= (:fieldtype (nth field 1) ) 1 )
      (= (:timeformat (nth field 1) ) 0 )
    ) 
     
    (jquery
     (fn []
       (-> (jquery (str "#" (name (nth field 0) )) )
         (.datepicker #js{:format "dd/mm/yyyy" })
         (.on "show"
           (fn [e] (
              let [
                ;;dt (js/Date (.. e -date))
                ;dtstring (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                dtstring (if
                  (= (count (.. e -dates) ) 0)
                    (tf/parse custom-formatter1 "May 26 2016 08:00:00"  )
                    (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                )


              ]
              ;;(swap! app-state assoc-in [:leavetypes :ivyt03 :fields :leavefromdate :value] (str (subs dt 8 10)  "/05/"    (subs dt 11 16)  ) )
             ;;(.log js/console dt) 
             ;;(.log js/console (str (.. e -date)  ) )
             ;(.log js/console (count (.. e -dates)))
             ;(.log js/console (subs (str (.. e -date)  ) 4 24))
             (.log js/console (tf/unparse custom-formatter3 (tm/date-time 2010 10 3)))  ;;dtstring
             )
           )
         )
       )      
     )
    )  


   


  )

  (if (and
      (= (:fieldtype (nth field 1) ) 1 )
      (= (:timeformat (nth field 1) ) 2 )
    ) 
     
    (jquery
     (fn []
       (-> (jquery (str "#" (name (nth field 0) )) )
         (.timepicker #js{:timeFormat "H:i:s" } )
       )      
     )
    )  
  )



  ;(.log js/console (get field  "fieldcode"    )   )  
  
)

(defn setdatepickers []
  (let [fields  (:fields ((keyword (:leavecode (:leaveapp @app-state) )) (:leavetypes @app-state) ) ) ]
    ;(.log js/console "Inside SetDate Pickers" )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )
    (dorun (map setdatepicker fields   ))
  )
)



(defn setdatepicker2 [field]
  (.log js/console (:name (nth field 1)  ) )
)


(defn setdatepickers2 []
  (let [fields (:fields ((keyword (:leavecode @app-state)) (:leavetypes @app-state) ) )  ]
    ;(.log js/console "Inside SetDate Pickers" )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )
    (dorun (map setdatepicker2 fields   ))
  )
)


(defn alertselected1 [event param]
  ;(js/alert (str event  "ClojureScript says 'Boo!'" ))
 
  ;(swap! app-state assoc :leavecode  event)

  (jquery
   (fn []
     (-> (jquery (str "#" param ))
       (.trigger  "click")
     )
   )
  )
  (swap! app-state assoc-in [:leaveapp (keyword param)] event) 
   (.log js/console (str "#" param )) 
  ;(setdatepickers2)
  1
)


(defn alertselected [event]
  ;(js/alert (str event  "ClojureScript says 'Boo!'" ))
 
  ;(swap! app-state assoc :leavecode  event)

  (jquery
   (fn []
     (-> (jquery "#leavebtngroup")
       (.trigger  "click")
     )
   )
  )
  (swap! app-state assoc-in [:leaveapp :leavecode] event) 
   ;(.log js/console (str "#" "leavefromdate" )) 
  ;(setdatepickers2)
  1
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch (fn [v] (
                         setdatepickers
                         ;.log js/console "Core.ASYNVC working!!!" 
                         )       )  )

    )
  )
)

(initqueue)

(defn OnCalcLeave [response]  
  (.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))
)

(defn calcleave []
  (POST "http://localhost/T5PWebAPI/api/leavecalc" {:handler OnCalcLeave
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                            :body (str 
"{"
  "\"leavecode\":\"" "LV04A"  "\"," 
  "\"leavefromtime\":\"" "2016/05/01 08:00"  "\","
  "\"leavetotime\":\"" "2016/05/01 18:00"  "\","
  "\"notes\":\"" "from clojure mobile"  "\""
"}") 
                                            })
)



(defcomponent leave-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    
    (put! ch 42)
  )
  (render [_]
    (dom/div
     (om/build t5pcore/website-view nil {})
      (p/panel (merge {:header (dom/h3 "休假申请" )} {:bs-style "primary" :text-align "center"}  {:footer  ( b/button {:bs-style "primary" :onClick (fn [e](calcleave))} "Submit")}  )

        (dom/div {:className "panel-body"}
          (dom/form {:className "form-horizontal"}
            (dom/div {:className "form-group"}
              (dom/label {:className "col-sm-2 control-label"} "类型"
                (dom/span {:style {:color "Red"}} "*")
              )
              (dom/div {:className "col-sm-10"}
                (b/button-group
                  {:id "leavebtngroup" }
                  (b/dropdown {:title (:leavecode (:leaveapp @app-state))  }
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
                  (dom/span {} (:name  (nth text 1)))
                  (if ( = (:required (nth text 1)  ) true ) 
                    (dom/span {:style {:color "Red"}} "*")
                  )
                )
                (dom/div {:className "col-sm-10"}
                  (cond 
                    (= (:fieldtype (nth text 1)  )  0)
                      ;;(dom/input {:type "text" :id (name (first text) ) :onChange #(handle-change %)})
                      (dom/div {:className "col-sm-10"}
                        (b/button-group
                          {:id  (name (first text) ) }
                          (b/dropdown {:title
                          (if
                            (= ( (keyword (nth text 0)) (:leaveapp @app-state)) nil )
                              (:name  (nth text 1))  
                              ( (keyword (nth text 0)) (:leaveapp @app-state))  

                          ) 

                          }
                            (map (fn [item]
                              (b/menu-item {:key (get item "code")
                                :on-select  (fn [e](alertselected1 e (name (first text) )))
                              } (get item "value"))
                              )(:values (nth text 1))
                            )                  
                          )
                        )
                      )
                    
                    (= (:fieldtype (nth text 1)  )  1)
                      (dom/input {:type "text" :id (name (first text) ) :oninput #(handle-change %)})
                    (= (:fieldtype (nth text 1)  )  2)
                      (dom/input {:type "text" :id (name (first text) ) :onChange #(handle-change %)})
                    (= (:fieldtype (nth text 1)  )  3)
                      (dom/input {:type "checkbox"   :id (name (first text)) :label (:name  (nth text 1))
                      ;;:onClick #(handle-chkb-click %)
                      :onChange ( fn [e]( handle-chkb-change  e )   ) 
                      }) 
                  )


                )
              )            
              )
              (sort 
                #(compare ( :num ( nth %1 1)) ( :num( nth %2 1))) 
                (filter (fn [x] ( not= (keyword (nth x 0)) :leavecode  )         )
                  (into[] (:fields ((keyword (:leavecode (:leaveapp @app-state))) (:leavetypes @app-state)) )  )   
                )
                
              )




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



