(ns t5pmobile.messages
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [t5pmobile.core :as t5pcore]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
  )
  (:import goog.History)
)

(enable-console-print!)

(defonce app-state (atom  {:page 0 :msgcount 0, :myappcount 0, :pendingapps 0, :showmessages 0, :showapplications 0, :messages []} ))


(defn OnGetMessages [response]
   ;(swap! app-state assoc :page inc)
   (swap! app-state assoc :myapplications  (get response "MyApplications")  )
   (swap! app-state assoc :messages  (get response "Messages")  )
   (swap! app-state assoc :msgcount (get (get response "Data") "msgcount") )
   (swap! app-state assoc :myappcount  (clojure.core/count (get response "MyApplications")) )
   (swap! app-state assoc :pendingapps  (clojure.core/count (get response "PendingApplications")) )
   ;(update-in app-state [:messages :myappcount] assoc (clojure.core/count (get response "MyApplications")) )
   ;(update-in app-state [:messages :pendingapps] assoc (clojure.core/count (get response "PendingApplications")) )
   ;(swap! t5pcore/app-state assoc-in [:messages] app-state )
   (.log js/console (:msgcount @app-state)) 

)



(defn OnGetMessagesPage [response]
  (let [
    messages (:messages @app-state)
    ]
    (swap! app-state assoc :messages 
           (into [] (concat messages (get response "Messages")))
           ) 

  )
)

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)




(defn getMessages [data]
  ;(.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))
  ;(swap! t5pcore/app-state assoc-in [:messages] (conj (:messages data) {:showmessages 0}) )
  ;(swap! t5pcore/app-state  assoc-in [:messages] {:showmessages 0} )
 
  (GET "http://localhost/T5PWebAPI/api/messages" {:handler OnGetMessages
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                            })
)


(defn getMessagesPage [event]
  (let [
      url (str "http://localhost/T5PWebAPI/api/messages/messages?empid=" "10289&page=" (+ 1 (:page @app-state)) )
    ]
   (.stopPropagation event)
   (swap! app-state assoc :page  (+ (:page @app-state) 1)   )
    (.log js/console (str "page: " " " (:page @app-state )))



    (GET url {:handler OnGetMessagesPage
                                              :error-handler error-handler
                                              :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                              })
  )
 
)


(defn displayapplications [event]
  ( let [ 
      ;olddata (:messages app-state)
      isshow (:showapplications @app-state)
      
      newdata ( if (= isshow 1) 0 1 )      
    ]
    ;(.log js/console "call to displaymessages function")
    (swap! app-state assoc :showapplications newdata )
    ;(swap! app-state assoc-in [:messages] newdata )
  )
)

(defn displaymessages [event]
  ( let [ 
      ;olddata (:messages app-state)
      isshow (:showmessages @app-state)
      
      newdata ( if (= isshow 1) 0 1 )      
    ]
    (.log js/console "call to displaymessages function")
    (swap! app-state assoc :showmessages newdata )
    ;(swap! app-state assoc-in [:messages] newdata )
  )
)

(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)


(defcomponent showapplications-view [data owner]
  (render
    [_]
    (dom/div {:className "list-group" :style {:display "block"}}
      (map (fn [item]
        (dom/span
          (dom/a {:className "list-group-item" :href (str  "#/applicationdetail/" (get item "forminstanceid") )}
            (dom/h4 {:className "list-group-item-heading"} 
              (dom/span {:className "label label-success"} (str "#" (get item "forminstanceid")))  
              (str " - " (get item "leavetype") " - "   (get item "applicantname") )
            )
            (dom/p {:className "paddingleft2"} (str "提交 "(get item "submittime")) )
            (dom/p {:className "list-group-item-text paddingleft2"}
              (str "开始日期: " (get item "leavefromdate") " 结束日期:   "   (get item "leavetodate") ", "   (get item "leavedays")  " 天") 
            )
          ) 
        )               
        )(:myapplications data)
      )
    )
  )
)


(defcomponent showmessages-view [data owner]
  (render
    [_]
    (dom/div {:className "list-group" :style {:display "block"}}
      (map (fn [item]
        (dom/span
          (dom/a {:className "list-group-item" :href (str  "#/msgdetail/" (get item "messageid") ) }
            (dom/h4 {:className "list-group-item-heading"} (get item "subject"))
            (dom/h6 {:className "paddingleft2"} (get item "senddate"))
            (dom/p {:className "list-group-item-text paddingleft2"} (get item "body"))
          ) 
        )                  
        )(:messages data)
      )
      (dom/div {:style {:display "block"}}
        (dom/button {:type "button" :className "btn btn-info" :style {:width "100%"}  :onClick (fn [e](getMessagesPage e))} "更多信息")
      )
    )
  )
)


(defmulti myapplications-view (
  fn [data owner] (:showapplications data )
  )
)



(defmethod myapplications-view 0
  [data owner] 
  (empty-view data owner)
)

(defmethod myapplications-view 1
  [data owner] 
  (showapplications-view data owner)
)


(defmulti mymessages-view (
  fn [data owner] (:showmessages data )
  )
)

(defmethod mymessages-view 0
  [data owner] 
  (empty-view data owner)
)

(defmethod mymessages-view 1
  [data owner] 
  (showmessages-view data owner)
)


(defn onMount [data]
  (getMessages data)
)


(defcomponent message-page-view [data owner]
  (will-mount [_]
    (onMount data)
  )
  (render [_]
   (dom/div
     (om/build t5pcore/website-view nil {})
     ;(om/build t5pcore/website-view t5pcore/app-state {})
     (dom/div #js {:className "panel panel-primary" :onClick (fn [e](displaymessages e))}
       (dom/div {:className "panel-heading"}
         (dom/div {:className "row"}
           (dom/div {:className "col-md-10"}
             (dom/span {:style {:float "left"} :className "glyphicon glyphicon-chevron-down"})
             (dom/span {:style {:padding-left "5px"}} "我的消息")
           )
           (dom/div {:className "col-md-2"}
             (dom/span {:className "badge" :style {:float "right" }} (str (:msgcount data))  )
           )
         )
       )
       (om/build mymessages-view  data {})
     )


     (dom/div {:className "panel panel-primary"  :onClick (fn [e](displayapplications e))}
       (dom/div {:className "panel-heading"}
         (dom/div {:className "row"}
           (dom/div {:className "col-md-10"}
             (dom/span {:style {:float "left"} :className "glyphicon glyphicon-chevron-down"})
             (dom/span {:style {:padding-left "5px"}} "我的申请")
           )
           (dom/div {:className "col-md-2"}
             (dom/span {:className "badge" :style {:float "right" }} (str (:myappcount data))  ))

          )
       )
       (om/build myapplications-view  data {})
     )


     (dom/div {:className "panel panel-primary"}
       (dom/div {:className "panel-heading"}
         (dom/div {:className "row"}
           (dom/div {:className "col-md-10"}
             (dom/span {:style {:float "left"} :className "glyphicon glyphicon-chevron-down"})
             (dom/span {:style {:padding-left "5px"}} "我的审批")
           )
           (dom/div {:className "col-md-2"}
             (dom/span {:className "badge" :style {:float "right" }} (str (:pendingapps data))  ))

          )
       )
      )


    )
  )
)




(sec/defroute messages-page "/messages" []
  (om/root message-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))


