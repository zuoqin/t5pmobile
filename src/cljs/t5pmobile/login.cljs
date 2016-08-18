(ns t5pmobile.login  (:use [net.unit8.tower :only [t]])
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
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
            
            [t5pmobile.home :as home]
            [t5pmobile.leave :as leave]
            [t5pmobile.messages :as messages]
            [t5pmobile.msgdetail :as msgdetail]
            [t5pmobile.applicationdetail :as applicationdetail]
            [t5pmobile.user :as user]
            [t5pmobile.subordinate :as subordinate]
  )
  (:import goog.History)
)

(enable-console-print!)

(def jquery (js* "$"))
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

(defonce app-state (atom  {:error "" :modalText "Modal Text" :modalTitle "Modal Title"} ))


(defn setLoginError [error]
  (swap! app-state assoc-in [:error] 
    (:error error)
  )

  (swap! app-state assoc-in [:modalTitle] 
    (str "Login Error")
  ) 

  (swap! app-state assoc-in [:modalText] 
    (str (:error error))
  ) 
  ;;(.log js/console (str  "In setLoginError" (:error error) ))
  (jquery
    (fn []
      (-> (jquery "#loginModal")
        (.modal)
      )
    )
  )
)


(defn OnLoginError [response]
  (let [     
      newdata { :error (get (:response response)  "error") }
    ]
   
    (setLoginError newdata)
  )
  
  ;(.log js/console (str  response ))
)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn OnGetUser [response]
  (
    let [     
      newdata {:userid (get response "userid") :datemask (get response "datemask") :timemask (get response "timemask")
        :language (get response "language")}
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
                                            :error-handler OnLoginError
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


(defn addModal []
  (dom/div
    (dom/div {:id "loginModal" :className "modal fade" :role "dialog"}
      (dom/div {:className "modal-dialog"} 
        ;;Modal content
        (dom/div {:className "modal-content"} 
          (dom/div {:className "modal-header"} 
                   (b/button {:type "button" :className "close" :data-dismiss "modal"})
                   (dom/h4 {:className "modal-title"} (:modalTitle @app-state) )
                   )
          (dom/div {:className "modal-body"}
                   (dom/p (:modalText @app-state))
                   )
          (dom/div {:className "modal-footer"}
                   (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
  )
)



(defcomponent login-page-view [data owner]
  (did-update [this prev-props prev-state]
    (.log js/console "starting login screen" ) 
    
  )
  (render
    [_]
    (dom/div {:className "container"}
      ;(om/build t5pcore/website-view data {})
      ;(dom/h1 "Login Page")
      (dom/img {:src "images/LogonBack.jpg" :className "img-responsive company-logo-logon"})
      (dom/form {:className "form-signin"}
        (dom/input #js {:type "text" :ref "txtUserName"
           :defaultValue  settings/demouser  :className "form-control" :placeholder "User Name"} )
        (dom/input {:className "form-control" :ref "txtPassword" :id "txtPassword"
           :defaultValue settings/demopassword :type "password"  :placeholder "Password"} )
        (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick (fn [e](checklogin owner))} "Login")
      )
      (addModal)
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
           app-state
           {:target (. js/document (getElementById "app"))}))



(defn main []
  (-> js/document
      .-location
      (set! "#/login"))

  ;(aset js/window "location" "#/login")
)
  
(main)






