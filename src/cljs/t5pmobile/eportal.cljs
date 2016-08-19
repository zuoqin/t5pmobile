(ns t5pmobile.eportal (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            [t5pmobile.settings :as settings]
  )
  (:import goog.History)
)

(enable-console-print!)


(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))



(defn onMount [data]
  ;(getLeaveTypes data)
  (swap! t5pcore/app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/eportal)
  ) 
)


(defcomponent eportal-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (render
    [_]
    (let [style {:style {:margin "10px" :padding-bottom "0px"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        (om/build t5pcore/website-view data {})
        (dom/div (assoc styleprimary  :className "panel panel-primary")
          (dom/div {:className "panel-heading"}
                     "基本信息" 
          )
          (dom/table {:className "table table-bordered"}
            (dom/tbody
              (dom/tr
                (dom/td #js {:rowSpan "3" :className "portrait"}
                  (dom/img {:src (str settings/apipath  "Content/Portrait/charles.jpg") :className "img-rounded portrait"})
                )
                (dom/td {:className "tdtable"} (:EmpName (:Employee data) ) 
                )
              )
              (dom/tr {:className "table_tr_background"}
                (dom/td {:className "tdtable"} "总经理" )
              )
            )
          )
        )


        (dom/div {:className "panel panel-primary"}
          (dom/div {:className "panel-heading"}
                     "年假" 
          )
          (dom/table {:className "table table-bordered"}
            (dom/tbody
              (dom/tr
                (dom/td 
                  (dom/div {:style {:float "left" }} "全年共享受年假") 
                  (dom/div {:style {:float "right"}} "0 天")
                )
              )
              (dom/tr {:className "table_tr_background"}
                (dom/td 
                  (dom/div {:style {:float "left"}} "全年已用" )
                  (dom/div {:style {:float "right"}} "0 天" )
                )
              )
              (dom/tr
                (dom/td 
                  (dom/div {:style {:float "left"}} "余额" )
                  (dom/div {:style {:float "right"}} "0 天" )
                )
              )
            )
          )
        )
        (dom/div {:className "panel panel-primary"}
            (dom/div {:className "panel-heading"}
                       "排班" 
            )
            (dom/ul {:className "list-group"}
              (map (fn [text]
                (dom/div
                  (dom/li {:className "list-group-item"}
                    (dom/span {:className "glyphicon glyphicon-time grey"})
                    (get text "workdate")
                  )
                  (dom/li {:className "list-group-item paddingleft3 table_tr_background gray"} "没有排班")
                )
                ) (:Roster (:Employee data))
              )
            )
        )      
      )
    ) 
  )
)


(sec/defroute eportal-home-page "/eportal" []
  (om/root eportal-page-view
           t5pcore/app-state
           {:target (. js/document (getElementById "app"))}))






