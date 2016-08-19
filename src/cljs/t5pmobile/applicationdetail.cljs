(ns t5pmobile.applicationdetail  (:use [net.unit8.tower :only [t]])
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
  )
  (:import goog.History)
)

(enable-console-print!)


(defonce app-state (atom  {:forminstanceid 0  :current "My Application"} ))


(defn array-to-string [element]
  (let [
      newdata {:empname (get element "empname") } 
    ]
    (:empname newdata)
  )
)

(defn OnGetApplication [response]
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


(defn getApplicationDetail []
  ;(.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))
  (if
    (and 
      (not= (:forminstanceid @app-state) nil)
      (not= (:forminstanceid @app-state) 0)
    )
    (GET (str "http://localhost/T5PWebAPI/api/empleave?forminstanceid=" (:forminstanceid @app-state) "&empid=" (:empid @app-state))
      {
        :handler OnGetApplication
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
    (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/applicationdetail)
  )
  (getApplicationDetail)
)

(defcomponent applicationdetail-page-view [data owner]
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
        (dom/div {:id "leave-detail-container"}
          (dom/div (assoc styleprimary  :className "panel panel-default"  :id "divMsgInfo")
            (dom/div {:className "panel-heading"}
              (dom/h3 {:className "panel-title"}
                (dom/span {:className "label label-success"} (str "#" (:forminstanceid @app-state)))
                " - Sunny - 无薪假"
              )
              (dom/h5 "提交时间:2016/08/01 13:42:29")
              (dom/dl {:className "dl-horizontal leaveRecipients"}
                (dom/dt {:text-align "left"}
                  (dom/small "开始日期: ")
                )
                (dom/dd "2016/08/06")

                (dom/dt {:text-align "left"}
                  (dom/small "结束日期: : ")
                )
                (dom/dd "2016/08/08")

                (dom/dt {:text-align "left"}
                  (dom/small "天数 : ")
                )
                (dom/dd "1")
              )
            )
            (dom/div {:className "panel-body"})
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (b/button {:className "btn btn-success"} "取消")
            (b/button {:className "btn btn-danger"} "删除")
          )
        )
      )
    )

  )
)



(sec/defroute applicationdetail-page "/applicationdetail/:id" {id :id}
  (let [
    forminstanceid id
    ]
    (swap! app-state assoc :forminstanceid forminstanceid :empid (:empid (:Employee @t5pcore/app-state))) 

    (om/root applicationdetail-page-view
             app-state
             {:target (. js/document (getElementById "app"))})

  )
)







