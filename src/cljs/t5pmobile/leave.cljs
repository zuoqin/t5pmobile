(ns t5pmobile.leave  (:use [net.unit8.tower :only [t]])
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
            [t5pmobile.settings :as settings]
  )
  (:import goog.History)
)


(def ch (chan (dropping-buffer 2)))
(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:modalText "THis the MOdal text" :modalTitle "This is the Modal Title"  :view 1 :leavecode "" :leavetypes [] :leaveapp {:leavecode "请选择"} } )) ;; :leavedays 0 :leavehours 0



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
     
     
     (swap! app-state assoc-in [:leavetypes]   (into {} newdata) )
     (swap! app-state assoc-in [:leavecodes]  leavecodes) 
  )
   

   ;(.log js/console (:leavetypes @app-state)) 

)


(defn OnCalcLeaveDays [response]
  ( let [ 
    newdata (map leaves-to-map response)
    leavecodes (map leaves-to-leavecodes response)
  ]
     
     
     (swap! app-state assoc-in [:leavetypes]   (into {} newdata) )
     (swap! app-state assoc-in [:leavecodes]  leavecodes) 
  )
   

   ;(.log js/console (:leavetypes @app-state)) 

)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn setNewLeaveApp [newLeaveApp]
  (swap! app-state assoc-in [:leaveapp :forminstanceid] 
    (:forminstanceid newLeaveApp)
  ) 

  (swap! app-state assoc-in [:modalTitle] 
    (str "New Leave Application Succeeded")
  ) 

  (swap! app-state assoc-in [:modalText] 
    (str "New Leave Application " (:forminstanceid newLeaveApp))
  ) 
  ;(.log js/console (str  (:forminstanceid newLeaveApp) ))
  (jquery
     (fn []
       (-> (jquery "#leaveModal")
           (.modal)
           )))
)


(defn setLeaveAppError [newLeaveApp]
  (swap! app-state assoc-in [:modalTitle] 
    (str "New Leave Application Failed")
  ) 

  (swap! app-state assoc-in [:modalText] 
    (str  (:error newLeaveApp))
  ) 
  ;(.log js/console (str  (:error newLeaveApp) ))
  (jquery
     (fn []
       (-> (jquery "#leaveModal")
           (.modal)
           )))
)


(defn OnApplyLeaveSuccess [response]
  (let [     
      newdata {:forminstanceid (get response "forminstanceid") :msg (get response "error") }
    ]


    (if (= (:forminstanceid newdata) nil)
      (jquery
         (fn []
           (-> (jquery "#leaveModal")
               (.modal)
               )))
      (setNewLeaveApp newdata)
    )
  )
  
  ;;(.log js/console (str  (get (first response)  "Title") ))
)




(defn OnApplyLeaveError [response]
  (let [     
      newdata { :error (get (:response response)  "error") }
    ]
   
    (setLeaveAppError newdata)
  )
  
  ;(.log js/console (str  response ))
)


(defn applyLeave []
  ;; (let [res {"forminstanceid"  888 "msg"  "Succeeded"}] 
  ;;   (OnApplyLeave res)

  ;; )
  (POST (str settings/apipath  "api/empleave") {
    :handler OnApplyLeaveSuccess
    :error-handler OnApplyLeaveError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state))))}
    :format :json
    :params (:leaveapp @app-state)})
)



(defn getLeaveTypes [data]
 (.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))

 
  (GET (str settings/apipath "api/leavetype/leavetype2?type=0") {
    :handler OnGetLeaveTypes
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)



(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)




(defn onMount [data]
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/leave)
  )
  (getLeaveTypes data)
  (jquery
   (fn []
     (-> (jquery "#datepicker")
       (.datepicker {})
     )
   )
  )
)


(defn OnCalcLeave [response]
  (let [     
      newdata {:leavedays (get response "leavedays") :leavehours (get response "leavehours") }
    ]

    (swap! app-state assoc-in [:leaveapp :leavedays] 
      (:leavedays newdata)
    ) 
    (.log js/console (str  (:leavedays newdata) ))
  )
  
  ;;(.log js/console (str  (get (first response)  "Title") ))
)



(defn calcleave []
  (.log js/console "Starting post leave calculation" ) 
  (POST (str settings/apipath "api/leavecalc") {
    :handler OnCalcLeave
    :error-handler error-handler
    :headers {
              :content-type "application/json"
              :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
    :format :json
    :params (:leaveapp @app-state)
    }
   
  )
)

(defn IsCheckLeave? []
  (and ( = (nil? (:leavefromdate (:leaveapp @app-state)))  false)
       ( = (nil? (:leavetodate (:leaveapp @app-state)))  false)
       ( = (nil? (:leavecode (:leaveapp @app-state)))  false)
  )
)



(defn CheckCalcLeave []
  (if (= (IsCheckLeave?)  true)
    (calcleave) 
    (.log js/console (str  (:leavefromdate (:leaveapp @app-state))) )
  )  
)

(defn handle-chkb-change [e]
  ;(.log js/console (.. e -target -id) )  
  ;(.log js/console "The change ....")
  (.stopPropagation e)
  (.stopImmediatePropagation (.. e -nativeEvent) )
  (swap! app-state assoc-in [:leaveapp (keyword  (.. e -currentTarget -id) )] 
    (if (= true (.. e -currentTarget -checked)  ) 1 0)
  )
  (CheckCalcLeave)
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
;(def custom-formatter2  (tf/formatter (:datemask (:User @t5pcore/app-state)) ))

(def custom-formatter3 (tf/formatter "yyyy/MM/dd"))




(defn setNewLeaveAppValue [key val]
  (swap! app-state assoc-in [:leaveapp (keyword key)] val)
  (CheckCalcLeave)
)

(defn setdatepicker [field]
  (let [custom-formatter2  (tf/formatter (:datemask (:User @t5pcore/app-state)) )] 


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
                  dt (js/Date (.. e -date))
                  ;dtstring (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                  dtstring (if
                    (= (count (.. e -dates) ) 0)
                      nil ;(tf/parse custom-formatter1 "May 26 2016 08:00:00"  )
                      (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                  )


                ]
                ;;(swap! app-state assoc-in [:leavetypes :ivyt03 :fields :leavefromdate :value] (str (subs dt 8 10)  "/05/"    (subs dt 11 16)  ) )
              
               ;;(.log js/console (str (.. e -date)  ) )
               ;(.log js/console (count (.. e -dates)))
               ;(.log js/console (subs (str (.. e -date)  ) 4 24))
                 (
                   if (= dtstring nil) "nil"
                   (
                     if (not= 
                                         ;( (keyword (.. e -target -id)) (:leaveapp @app-state)  )  
                         ( (keyword (.. e -target -id)) (:leaveapp @app-state))
                         (tf/unparse custom-formatter2 dtstring)

                         )
                     (
                       setNewLeaveAppValue (.. e -target -id) (tf/unparse custom-formatter2 dtstring)
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


(defn setcalculatedfield [field] 
    ;(.log js/console (keyword  (nth field 0) )  )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )


  ;; leavedays, leavehours - these fields are calculated fields, set initial values to 0
  (if (= (:fieldtype (nth field 1)) 5)
    (if (=   ( (keyword  (nth field 0) ) (:leaveapp @app-state)) nil)
      (swap! app-state assoc-in [:leaveapp (keyword  (nth field 0) )] 
        0
      )    
    )
  )


  ;; boolean fields initialize to TRUE
  (if (= (:fieldtype (nth field 1)) 3)
    (if (=   ( (keyword  (nth field 0) ) (:leaveapp @app-state)) nil)
      (swap! app-state assoc-in [:leaveapp (keyword  (nth field 0) )] 
        1
      ) 
    )
  )
)


(defn setcalculatedfields []
  (let [fields  (:fields ((keyword (:leavecode (:leaveapp @app-state) )) (:leavetypes @app-state) ) ) ]
    ;(.log js/console "Inside  setcalculatedfields" )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )
    (dorun (map setcalculatedfield fields   ))
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

(defn setcontrols []
  (setdatepickers)
  (setcalculatedfields)
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
      (take! ch (
        fn [v] (
                                        ;(setcalculatedfields) 
           setcontrols 
                                        ;.log js/console "Core.ASYNVC working!!!" 
        )
      ))
    )
  )
)

(initqueue)

(defn leave-to-state [leave]
  (let [     
      newdata {:leavecode (get leave "leavecode") :name (get leave "name") }
    ]
    newdata
  )
  
)


(defn desplayComboboxField [text]
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

)

(defn addModal []
   (dom/div

   ;(b/button {:type "button" :className "btn btn-info btn-lg" :data-toggle "modal" :data-target "#myModal"} "Open Modal")
           (dom/div {:id "leaveModal" :className "modal fade" :role "dialog"}
                    (dom/div {:className "modal-dialog"} 
                             ;;Modal content
                             (dom/div {:className "modal-content"} 
                                      (dom/div {:className "modal-header"} 
                                               (b/button {:type "button" :className "close" :data-dismiss "modal"})
                                               (dom/h4 {:className "modal-title"} (:modalTitle @app-state) )
                                               )
                                      (dom/div {:className "modal-body"}
                                               (dom/p (:modalText @app-state))
                                               )
                                      (dom/div {:className "modal-footer"}
                                               (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
                                               )
                                      )
                             )
                    )
            
           (dom/div

            ( b/button {:bs-style "primary"
                        :onClick (fn [e](applyLeave))
                        :disabled? (not= (IsCheckLeave?)  true)  } "Submit")
            )   
   )

)



(defcomponent leave-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "Here hould be put!!!!") 
    (put! ch 42)
  )
  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      (p/panel (merge {:header (dom/h3 "休假申请" )} {:bs-style "primary" :text-align "center"}  
        {:footer (addModal)}  )

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
                      (desplayComboboxField text)
                    (= (:fieldtype (nth text 1)  )  1)
                      (dom/input {:type "text" :id (name (first text) ) :oninput #(handle-change %)})
                    (= (:fieldtype (nth text 1)  )  2)
                      (dom/input {:type "text" :id (name (first text) ) :onChange #(handle-change %)})
                    (= (:fieldtype (nth text 1)  )  3)
                      (dom/input {:type "checkbox" :defaultChecked true :id (name (first text)) :label (:name  (nth text 1))
                      :onChange ( fn [e]( handle-chkb-change  e )   ) 
                      }) 
                    (= (:fieldtype (nth text 1)  )  5)
                      (dom/input {:type "text" :disabled true :value ((keyword (name (first text) )) (:leaveapp @app-state))  :ref (name (first text) ) :id (name (first text) )})

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



