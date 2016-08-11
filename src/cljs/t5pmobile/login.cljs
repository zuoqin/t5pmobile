(ns t5pmobile.login
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [t5pmobile.settings :as settings]
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
      newdata {:empid (get (get response "Emphr") "empid")  :EmpName (get (get response "Emphr") "EmpName") :Roster (get response "RosterList")  }
    ]
    (swap! t5pcore/app-state assoc-in [:Employee] newdata )
  )
)

(defn requser []
  (GET (str settings/apipath "api/user") {
    :handler OnGetUser
    :error-handler error-handler
    :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)

(defn reqemployee []
  (GET (str settings/apipath "api/employee") {:handler OnGetEmployee
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
    (aset js/window "location" "#/home")

  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)

(defn dologin [username password]
  (POST (str settings/apipath "Token") {:handler OnLogin
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
  (did-update [this prev-props prev-state]
    (.log js/console "jhkjhkjkjhkj" ) 
    
  )
  (render
    [_]
    (dom/div {:className "container"}
      ;(om/build t5pcore/website-view data {})
      ;(dom/h1 "Login Page")
      (dom/img {:src "images/LogonBack.jpg" :className "img-responsive company-logo-logon"})
      (dom/form {:className "form-signin"}
        (dom/input #js {:type "text" :ref "txtUserName" :value "sunny" :className "form-control" :placeholder "User Name"})
        (dom/input {:className "form-control" :ref "txtPassword" :id "txtPassword" :value "1016" :type "password"  :placeholder "Password"} )
        (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick (fn [e](checklogin owner))} "Login")
      )
    )
  )
)



(defmulti website-view
  (
    fn [data _]
      (:view (if (= data nil) @t5pcore/app-state @data ))
  )
)

(defmethod website-view 0
  [data owner] 
  (login-page-view data owner)
)

(defmethod website-view 1
  [data owner] 
  (login-page-view data owner)
)

;; (defn index-page-view [app owner]
;;  (reify
;;    om/IRender
;;    (render
;;      [_]
;;       (dom/div
;;         (om/build  loginForm  app {})
;;         ;(dom/h1 "Index Page")
;;       )
;;     )
;;   )
;; )



;; (sec/defroute index-page "/" []
;;   (om/root index-page-view
;;            t5pcore/app-state
;;            {:target (. js/document (getElementById "app"))}))

;; (sec/defroute index-page "/" []
;;   (om/root login-page-view 
;;            {}
;;            {:target (. js/document (getElementById "app"))}))

(sec/defroute login-page "/login" []
  (om/root login-page-view 
           t5pcore/app-state
           {:target (. js/document (getElementById "app"))}))



(defn main []
  (-> js/document
      .-location
      (set! "#/"))

  (aset js/window "location" "#/login")
)
  
(main)






