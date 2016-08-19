(ns t5pmobile.msgdetail  (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
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

(enable-console-print!)


(defonce app-state (atom  {:view 1 :msgid 0 :current "Message Details"} ))


(defn array-to-string [element]
  (let [
      newdata {:empname (get element "empname") } 
    ]
    (:empname newdata)
  )
)

(defn OnGetMessage [response]
  (let [     
      newdata {
        :sendtime (get response "senddate") :to (get response "To")
        :body (get response "body") :subject (get response "subject") }
    ]

    (swap! app-state assoc-in [:sendtime ] (:sendtime newdata) ) 
    (swap! app-state assoc-in [:to ]  (clojure.string/join ", "  (map array-to-string (:to newdata))) ) 
    (swap! app-state assoc-in [:body] (:body newdata) ) 
    (swap! app-state assoc-in [:subject ] (:subject newdata) ) 
  )
)




(defn OnError [response]
  (let [     
      newdata { :error (get (:response response)  "error") }
    ]
    (.log js/console (str  response )) 
    
  )
  
  
)


(defn getMessageDetail []
  ;(.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))
  (if
    (and 
      (not= (:msgid @app-state) nil)
      (not= (:msgid @app-state) 0)
    )
    (GET (str settings/apipath "api/messages?messageid=" (:msgid @app-state) "&empid=" (:empid @app-state))
      {
        :handler OnGetMessage
        :error-handler OnError
        :headers {
          :content-type "application/json"
          :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state))))
        }
      }
    )
  
  )
)




(defn onMount [data]
  (swap! app-state assoc-in [:current] 
    (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/msgdetail)
  )
  (getMessageDetail)
)

(defcomponent msgdetail-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (render
    [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        (om/build t5pcore/website-view data {})
        (dom/div {:id "message-detail-container"}
          (dom/span
            (dom/div  (assoc styleprimary  :className "panel panel-default"  :id "divMsgInfo")
              (dom/div {:className "panel-heading"}
                (dom/h3 {:className "panel-title"} (:subject @app-state))
                (dom/h5 "发件人: Jane")
                (dom/h5 "收件人:: "
                  (dom/span (str (:to @app-state)))
                )
                (dom/h5 "抄送: ")
                (dom/h5 (str "时间: "  (:sendtime @app-state)) )
              )
              (dom/div  #js {:className "panel-body" :dangerouslySetInnerHTML #js {:__html (:body @app-state)}} nil)
            )
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (b/button {:className "btn btn-danger"} "删除")
          )
        )
      )
    )

  )
)



(sec/defroute msgdetail-page "/msgdetail/:id" {id :id}
  (let [
    messageid id
    ]
    (swap! app-state assoc :msgid messageid :empid (:empid (:Employee @t5pcore/app-state))) 

    (om/root msgdetail-page-view
             app-state
             {:target (. js/document (getElementById "app"))})

  )
)







