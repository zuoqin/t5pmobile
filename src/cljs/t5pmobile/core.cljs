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
          :eportal "Home"
          :messages "Messages"
          :msgdetail "Message Details"
          :applicationdetail "My application"
          :leave "Leave"
          :payslip "Payslip"
          :subordinate "Subordinate"
          :settings "Settings"
          :hrms "HRMS"
        }
        :missing  "<Missing translation: [%1$s %2$s %3$s]>"
      }
      :cn{
        :mainmenu{
          :eportal "首页"
          :messages "我的消息"
          :msgdetail "消息详细"
          :applicationdetail "我的申请"
          :leave "休假"
          :payslip "薪资单"
          :subordinate "下属"
          :settings "设置"
          :hrms ""
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


(defn displaySystemMenuBlock []
  (dom/li
    (dom/a {:href "#"}
      (dom/i {:className "fa fa-sitemap fa-fw"})
      "System Menu"
    )          
  )
)

(defn displaySideBarBlock []
  (dom/div {:className "navbar-default sidebar" :role "navigation"}
    (dom/div {:className "sidebar-nav navbar-collapse"}
      (dom/ul {:className "nav" :id "side-menu"}
        (dom/li {:className "sidebar-search"}
          (dom/div {:className "input-group custom-search-form"}
            (dom/input {:className "form-control" :type "text" :placeholder "Search..."})
            (dom/span {:className "input-group-btn"}
              (dom/button {:className "btn btn-default" :type "button"}
                (dom/i {:className "fa fa-search"})
              )
            )
          )
        )

        (dom/li
          (dom/a {:href "#/hrms"}
            (dom/i {:className "fa fa-dashboard fa-fw"})
            "Dashboard"
          )
          
        )
        (dom/li
          (dom/a {:href "#"}
            (dom/i {:className "fa fa-bar-chart-o fa-fw"})
            (dom/span {:className "fa arrow"})
            "Charts"
          )
          (dom/ul {:className "nav nav-second-level"}
            (dom/li
              (dom/a {:href "flot.html"} "Flot Charts")
            )
            (dom/li
              (dom/a {:href "morris.html"} "Morris.js Charts")
            )
          )  ;; /.nav-second-level
        )
        (dom/li
          (dom/a {:href "#/hrms"}
            (dom/i {:className "fa fa-table fa-fw"})
            "Journals"
          )          
        )
        (dom/li
          (dom/a {:href "#/hrms"}
            (dom/i {:className "fa fa-edit fa-fw"})
            "Forms"
          )          
        )
        (displaySystemMenuBlock)
      )
    )
  )
)


(defn displayUserSettingsBlock []
  (dom/li {:className "dropdown"}
    (dom/a {:className "dropdown-toggle" :data-toggle "dropdown" :href "#" }
      (dom/i {:className "fa fa-user fa-fw"})
      (dom/i {:className "fa fa-caret-down"})
    )
    (dom/ul {:className "dropdown-menu dropdown-user"}
      (dom/li
        (dom/a {:href "#"} 
          (dom/i {:className "fa fa-user fa-fw"})
          "User Profile"
        )
      )
      (dom/li
        (dom/a {:href "#"} 
          (dom/i {:className "fa fa-gear fa-fw"})
          "Settings"
        )
      )
      (dom/li {:className "divider"})
      (dom/li
        (dom/a {:href "#/login"} 
          (dom/i {:className "fa fa-sign-out fa-fw"})
          "Logout"
        )
      )
    )

  )
)

(defn displayMessagesBlock []
  (dom/li {:className "dropdown"}
    (dom/a {:className "dropdown-toggle" :data-toggle "dropdown" :href "#" }
      (dom/i {:className "fa fa-envelope fa-fw"})
      (dom/i {:className "fa fa-caret-down"})
    )
    (dom/ul {:className "dropdown-menu dropdown-messages"}
      (dom/li
        (dom/a {:href "#"} 
          (dom/div
            (dom/strong "John Smith")
            (dom/span {:className "pull-right text-muted"}
              (dom/em "Yesterday")
            )
          )
          (dom/div "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eleifend...")
        )
      )
      (dom/li {:className "divider"})
      (dom/li
        (dom/a {:href "#"} 
          (dom/div
            (dom/strong "John Smith")
            (dom/span {:className "pull-right text-muted"}
              (dom/em "Yesterday")
            )
          )
          (dom/div "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eleifend...")
        )
      )
      (dom/li {:className "divider"})
      (dom/li
        (dom/a {:href "#"} 
          (dom/div
            (dom/strong "John Smith")
            (dom/span {:className "pull-right text-muted"}
              (dom/em "Yesterday")
            )
          )
          (dom/div "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eleifend...")
        )
      )
      (dom/li {:className "divider"})
      (dom/li
        (dom/a {:className "text-center" :href "#"}
          (dom/strong "Read All Messages ")
          (dom/i {:className "fa fa-angle-right"})
        )
      )
    )
  )
)

(defcomponent hrms-navigation-view [data owner]
  (render [_]
    (let [style {:style {:margin "10px" :padding-bottom "0px"}}
      stylehome {:style {:margin-top "0px"} }
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
        (dom/ul {:className "nav navbar-top-links navbar-right"}
          (displayMessagesBlock)
          (displayUserSettingsBlock)
        )
        (displaySideBarBlock)
      )
    )
  )
)


(defcomponent eportal-navigation-view [data owner]
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
              (dom/a (assoc style :href "#/eportal")
                (dom/span {:className "glyphicon glyphicon-home"})
                  (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/eportal)
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
              (dom/a (assoc style :href "#/hrms") 
                (dom/span {:className "glyphicon glyphicon-log-out"})
                (t (numtolang  (:language (:User @app-state))) my-tconfig :mainmenu/hrms)
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
  (.log js/console "One is found in view")
  (eportal-navigation-view data owner)
)



(defmethod website-view 2
  [data owner] 
  (.log js/console "Two is found in view")
  (hrms-navigation-view data owner)
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






