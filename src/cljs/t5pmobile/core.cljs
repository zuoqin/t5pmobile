(ns t5pmobile.core
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

(defonce app-state (atom {:view 0}))
;(sec/set-conig! :prefix "#")


(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn OnGetEmployee [response]
  (
    let [     
      newdata {:EmpName (get (get response "Emphr") "EmpName") :Roster (get response "RosterList")  }
    ]
    (swap! app-state assoc-in [:Employee] newdata )
  )
)


(defn reqemployee []
  (GET "http://localhost/T5PWebAPI/api/employee" {:handler OnGetEmployee
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @app-state)))) }
                                            })
)

(defn OnLogin [response]
  (
    let [     
      ;;newdata (js->clj response)
      newdata (vector {:token (get response "access_token")  :expires (get response "expires_in") }  
     )


;;[{:Title (get (first response) "Title") :Introduction  (get (first response) "Introduction") :Reference  (get (first response) "Reference") :Updated  (get (first response) "Updated") :Published (get (first response) "Pub;ished")}]
    ]

    (.log js/console (str newdata))
    ;;(.log js/console (str (select-keys (js->clj response) [:Title :Reference :Introduction])  ))    
    ;(swap! app-state assoc-in pageid newdata )
    (swap! app-state assoc-in [:token] newdata )
    (swap! app-state assoc-in [:view] 1 )
    (reqemployee)
    (aset js/window "location" "http://localhost:3449/#/home")

  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)

(defn dologin [username password]
  (POST "http://localhost/T5PWebAPI/Token" {:handler OnLogin
                                            :error-handler error-handler
                                            :headers {:content-type "application/x-www-form-urlencoded"}
                                            :body "grant_type=password&username=nacho&password=1"
                                            })
)


(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))



(defcomponent navigation-view [_ _]
  (render
   [_]
   (let [style {:style {:margin "10px;"}}]
     (dom/div style
              (dom/a (assoc style :href "#/") 
                     "Home")
              (dom/a (assoc style :href "#/something") 
                     "Something")
              (dom/a (assoc style :href "#/messages") 
                     "Messages")
              (dom/a (assoc style :href "#/login") 
                     "Logout")
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



(defmulti website-view (fn [data _] (:view data)))

(defmethod website-view 0
  [data owner] 
  (logout-view data owner)
)

(defmethod website-view 1
  [data owner] 
  (navigation-view data owner)
)


; (defcomponent index-page-view [app owner]
;   (render
;    [_]
;    (dom/div
;     ;(om/build navigation-view {})
;     (om/build website-view app {})
;     (dom/h1 "Index Page"))))


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


(sec/defroute index-page "/" []
  (om/root index-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))



(defcomponent something-page-view [_ _]
  (render
   [_]
   (dom/div
    (om/build navigation-view {})
    (dom/h1 "Something Page"))))

(sec/defroute something-page "/something" []
  (om/root something-page-view
           {}
           {:target (. js/document (getElementById "app"))}))



(defcomponent about-page-view [_ _]
  (render
   [_]
   (dom/div
    (om/build navigation-view {})
    (dom/h1 "About Page"))))

(defn checklogin [owner]
  (let [theUserName (-> (om/get-node owner "txtUserName")
    .-value)]
    ;(aset js/window "location" "http://localhost:3449/#/something")
    ;(.log js/console owner ) 
    (.log js/console (str  theUserName ))
    (dologin "nacho" "") 
  )
)

(defcomponent login-page-view [data owner]
  (render
    [_]
    (dom/div
      (om/build website-view data {})
      (dom/h1 "Login Page")
      (dom/form {:className "form-origin"}
        (dom/input #js {:type "text" :ref "txtUserName"})
        (dom/input {:className "form-control" :id "txtPassword" :placeholder "Password"})
        (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick (fn [e](checklogin owner))} "Login")
      )
    )
  )
)


(defcomponent home-page-view [data owner]
  (render
    [_]
    (dom/div
      (om/build website-view data {})
      (dom/div {:className "panel panel-primary"}
        (dom/div {:className "panel-heading"}
                   "基本信息" 
        )
        (dom/table {:className "table table-bordered"}
          (dom/tbody
            (dom/tr
              (dom/td #js {:rowSpan "3" :className "portrait"}
                (dom/img {:src "http://localhost/T5PWebAPI/Content/Portrait/charles.jpg" :className "img-rounded portrait"})
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


(sec/defroute home-page "/home" []
  (om/root home-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))


(sec/defroute about-page "/about" []
  (om/root about-page-view
           {}
           {:target (. js/document (getElementById "app"))}))





(sec/defroute login-page "/login" []
  (om/root login-page-view 
           app-state
           {:target (. js/document (getElementById "app"))}))




(defn root-component [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil (dom/h1 nil (:text app)))
      ;(om/build website-view app )
    )
  )
)

(defn main []
  (-> js/document
      .-location
      (set! "#/")))

(main)




