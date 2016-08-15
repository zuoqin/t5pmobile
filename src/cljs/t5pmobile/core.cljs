(ns t5pmobile.core (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
           
            
            ;[taoensso.tower :as tower :refer-macros (with-tscope)] ;;internalization technique
            
  )
  (:import goog.History)
)

(enable-console-print!)

(defonce app-state (atom {:view 0 :current "Home"}))



(def my-tconfig
  {:dev-mode? true
   :fallback-locale :en
   :dictionary
   {:en         {:example {:foo         ":en :example/foo text"
                           :foo_comment "Hello translator, please do x"
                           :bar {:baz ":en :example.bar/baz text"}
                           :greeting "Hello %s, how are you?"
                           :inline-markdown "<tag>**strong**</tag>"
                           :block-markdown* "<tag>**strong**</tag>"
                           :with-exclaim!   "<tag>**strong**</tag>"
                           :greeting-alias :example/greeting
                           :baz-alias      :example.bar/baz}
                 :missing  "<Missing translation: [%1$s %2$s %3$s]>"}
    :en-US      {:example {:foo ":en-US :example/foo text"}}
    :en-US-var1 {:example {:foo ":en-US-var1 :example/foo text"}}}})

(t :en-US my-tconfig :example/foo)
(t :en    my-tconfig :example/foo)
(t :en    my-tconfig :example/greeting "Steve")





(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))



(defcomponent navigation-view [_ _]
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
            (dom/span {:id "pageTitle"}  (:current @app-state))
          )
        )
        (dom/div {:className "collapse navbar-collapse navbar-ex1-collapse" :id "menu"}
          (dom/ul {:className "nav navbar-nav"}
            (dom/li
              (dom/a (assoc style :href "#/home")
                (dom/span {:className "glyphicon glyphicon-home"})
                     "Home")
            )

            (dom/li
              (dom/a (assoc style :href "#/messages")
                (dom/span {:className "glyphicon glyphicon-envelope"})
                     "Messages")
            )


            (dom/li
              (dom/a (assoc style :href "#/leave")
                (dom/span {:className "glyphicon glyphicon-list-alt"})
                     "Leave")           
            )

            (dom/li
              (dom/a (assoc style :href "#/payslip") 
                (dom/span {:className "glyphicon glyphicon-usd"})
                     "Payslip")
            )


            (dom/li
              (dom/a (assoc style :href "#/subordinate")
                (dom/span {:className "glyphicon glyphicon-th"})
                     "Subordinate")
            )
          )
         
          (dom/ul {:className "nav navbar-nav navbar-right"}
            (dom/li
              (dom/a (assoc style :href "#/user")
                 (dom/span {:className "glyphicon glyphicon-cog"})
                     "Settings")
            )         
            (dom/li
              (dom/a (assoc style :href "#/login") 
                (dom/span {:className "glyphicon glyphicon-log-out"})
                     "Logout")
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
  (logout-view data owner)
)

(defmethod website-view 1
  [data owner] 
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






