(ns t5pmobile.core (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
  )
  (:import goog.History)
)

(enable-console-print!)

(defonce app-state (atom {:view 0 :current "Home"}))



(def my-tconfig
  {:dev-mode? true
    :fallback-locale :en
    :dictionary{
      :en{
        :mainmenu{
          :home "Home"
          :messages "Messages"
          :msgdetail "Message Details"
          :applicationdetail "My application"
          :leave "Leave"
          :payslip "Payslip"
          :subordinate "Subordinate"
          :settings "Settings"
          :exit "Logout"
        }
        :missing  "<Missing translation: [%1$s %2$s %3$s]>"
      }
      :cn{
        :mainmenu{
          :home "首页"
          :messages "我的消息"
          :msgdetail "消息详细"
          :applicationdetail "我的申请"
          :leave "休假"
          :payslip "薪资单"
          :subordinate "下属"
          :settings "设置"
          :exit "退出"
        }
      }
    }
  }
)


(defn numtolang [num]
  (let [res (case num
      0  (keyword "en")
      1  (keyword "cn")
      2  (keyword "zh")
      3  (keyword "jp")
      (keyword "en")
    )]
    
    res
  )
)



(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))



(defcomponent navigation-view [data owner]
  (render [_]
    (let [style {:style {:margin "10px" :padding-bottom "0px"}}
      stylehome {:style {:margin-top "10px"} }
      ]
      (dom/nav {:className "navbar navbar-default navbar-fixed-top" :role "navigation"}
        (dom/div {:className "navbar-header"}
          (dom/button {:type "button" :className "navbar-toggle"
            :data-toggle "collapse" :data-target ".navbar-ex1-collapse"}
            (dom/span {:className "sr-only"} "Toggle navigation")
            (dom/span {:className "icon-bar"})
            (dom/span {:className "icon-bar"})
            (dom/span {:className "icon-bar"})
          )
          (dom/a  (assoc stylehome :className "navbar-brand")
            (dom/span {:id "pageTitle"}  (:current @data))
          )
        )
        (dom/div {:className "collapse navbar-collapse navbar-ex1-collapse" :id "menu"}
          (dom/ul {:className "nav navbar-nav"}
            (dom/li
              (dom/a (assoc style :href "#/home")
                (dom/span {:className "glyphicon glyphicon-home"})
                  (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/home)
                )
            )

            (dom/li
              (dom/a (assoc style :href "#/messages")
                (dom/span {:className "glyphicon glyphicon-envelope"})
                  (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/messages)
              )
            )


            (dom/li
              (dom/a (assoc style :href "#/leave")
                (dom/span {:className "glyphicon glyphicon-list-alt"})
                (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/leave)
              )           
            )

            (dom/li
              (dom/a (assoc style :href "#/payslip") 
                (dom/span {:className "glyphicon glyphicon-usd"})
                (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/payslip)
              )
            )


            (dom/li
              (dom/a (assoc style :href "#/subordinate")
                (dom/span {:className "glyphicon glyphicon-th"})
                (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/subordinate)
              )
            )
          )
         
          (dom/ul {:className "nav navbar-nav navbar-right"}
            (dom/li
              (dom/a (assoc style :href "#/user")
                 (dom/span {:className "glyphicon glyphicon-cog"})
                 (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/settings)
              )
            )         
            (dom/li
              (dom/a (assoc style :href "#/login") 
                (dom/span {:className "glyphicon glyphicon-log-out"})
                (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/exit)
              )
            )
          )
        )

              
      )
    )
  )
)


(defcomponent logout-view [_ _]
  (render
   [_]
   (let [style {:style {:margin "10px;"}}]
     (dom/div style
       (dom/a (assoc style :href "#/login") 
              "Login"
              )
      )
    )
  )
)



(defmulti website-view
  (
    fn [data _]
      (:view (if (= data nil) @app-state @data ))
  )
)

(defmethod website-view 0
  [data owner] 
  ;(.log js/console "zero found in view")
  (logout-view data owner)
)

(defmethod website-view 1
  [data owner] 
  ;(.log js/console "One is found in view")
  (navigation-view data owner)
)


(defn index-page-view [app owner]
 (reify
   om/IRender
   (render
     [_]
      (dom/div
        (om/build website-view app {})
        ;(dom/h1 "Index Page")
      )
    )
  )
)


;; (sec/defroute index-page "/" []
;;   (om/root index-page-view
;;            app-state
;;            {:target (. js/document (getElementById "app"))}))




(defn main []
  (-> js/document
      .-location
      (set! "#/"))

  (aset js/window "location" "#/login")
)
  
;(main)






