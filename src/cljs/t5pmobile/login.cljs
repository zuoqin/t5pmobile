(ns t5pmobile.login
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            [om-bootstrap.input :as i]
            
            
  )
  (:import goog.History)
)

(enable-console-print!)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn OnGetUser [response]
  (
    let [     
      newdata {:userid (get response "userid") :datemask (get response "datemask") :timemask (get response "timemask") }
    ]
    (swap! t5pcore/app-state assoc-in [:User] newdata )
  )
)

(defn OnGetEmployee [response]
  (
    let [     
      newdata {:EmpName (get (get response "Emphr") "EmpName") :Roster (get response "RosterList")  }
    ]
    (swap! t5pcore/app-state assoc-in [:Employee] newdata )
  )
)

(defn requser []
  (GET "http://localhost/T5PWebAPI/api/user" {:handler OnGetUser
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                            })
)

(defn reqemployee []
  (GET "http://localhost/T5PWebAPI/api/employee" {:handler OnGetEmployee
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
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
    (swap! t5pcore/app-state assoc-in [:token] newdata )
    (swap! t5pcore/app-state assoc-in [:view] 1 )
    (reqemployee)
    (requser)
    (aset js/window "location" "http://localhost:3449/#/home")

  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)

(defn dologin [username password]
  (POST "http://localhost/T5PWebAPI/Token" {:handler OnLogin
                                            :error-handler error-handler
                                            :headers {:content-type "application/x-www-form-urlencoded"}
                                            :body (str "grant_type=password&username=" username "&password=" password) 
                                            })
)




(defn checklogin [owner]
  (let [
    theUserName (-> (om/get-node owner "txtUserName") .-value)
    thePassword (-> (om/get-node owner "txtPassword") .-value)
    ]
    ;(aset js/window "location" "http://localhost:3449/#/something")
    ;(.log js/console owner ) 
    (.log js/console (str  theUserName ))
    (dologin (str theUserName) (str thePassword)) 
  )
)

(defcomponent login-page-view [data owner]
  (render
    [_]
    (dom/div {:className "container"}
      ;(om/build t5pcore/website-view data {})
      ;(dom/h1 "Login Page")
       (dom/img {:src "images/LogonBack.jpg" :className "img-responsive company-logo-logon"})
      (dom/form {:className "form-signin"}
        (dom/input #js {:type "text" :ref "txtUserName" :value "sunny" :className "form-control" :placeholder "User Name"})
        (dom/input {:className "form-control" :ref "txtPassword" :value "1016" :id "txtPassword"  :placeholder "Password"})
        (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick (fn [e](checklogin owner))} "Login")
      )
    )
  )
)





(sec/defroute login-page "/login" []
  (om/root login-page-view 
           t5pcore/app-state
           {:target (. js/document (getElementById "app"))}))






