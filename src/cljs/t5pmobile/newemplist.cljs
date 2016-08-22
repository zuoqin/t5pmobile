(ns t5pmobile.newemplist (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [t5pmobile.settings :as settings]
            [ajax.core :refer [GET POST]]
            
  )
  (:import goog.History)
)

(enable-console-print!)



(def my-tconfig
  {:dev-mode? true
    :fallback-locale :en
    :dictionary{
      :en{
        :emplist{
          :empcode "Code"
          :name "Name"
          :hirestatus "Hire Status"
          :birthday "Birthday"
          :gender "Gender"
          :major "Major"
        }
      }
      :cn{
        :emplist{
          :empcode "编码"
          :name "姓名"
          :hirestatus "雇佣状态"
          :birthday "生日"
          :gender "性别"
          :major "专业"
          
        }
      }
    }
  }
)


(defonce app-state (atom  {:view 2  :current "New Employee List"} ))
(def jquery (js* "$"))



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn employees-to-map [employee]
  (let [     
      newdata {
               ;:empid (get employee "empid") 
               :empcode (get employee "empcode") :empname (get employee "empname")
               :hirestatus (get employee "hirestatus") :birthday (get employee "birthday")
               ;:gender (get employee "gender")
               ;:major (get employee "major")
      }
    ]
    ;(.log js/console newdata)
    newdata
  )
  
)

;(def js-object  (clj->js  :responsive "true" :data (:employees @app-state) ))


(defn newempmap-to-array [employee]
  (let [     
      newdata [(:empcode employee) (:name employee) (:hirestatus employee) (:birthday employee)] 
    ]
    ;(.log js/console newdata)
    newdata
  )
  
)

(defn OnGetNewEmployees [response]
  (let [ 
    newdata (map employees-to-map response)
    newdata2 (map newempmap-to-array newdata)
    ]
    (swap! app-state assoc-in [:employees]   (into []  newdata) )

    (swap! app-state assoc-in [:employees2]   (into []  newdata2) )
    ;(.log js/console newdata)

    (jquery
      (fn []
        (-> (jquery "#dataTables-example")
          (.DataTable   (clj->js {:responsive "true"
                                  :data (into [] (into [] (:employees2 @app-state)))
;; [
;;                                                             ["Tiger Nixon" "System Architect" "Edinburgh" "5421"]
;;                                                             ["Garrett Winters" "Director" "moscow" "8956"]
;;   ]
                                  } )  )
        )
      )
    )
  )
)


(defn getNewEmployees []
  (GET (str settings/apipath "api/empnew") {
    :handler OnGetNewEmployees
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn buildEmployeeList []
  (map
    (fn [text]
      (dom/tr {:className "odd gradeX"}
        (dom/td (:empcode text))
        (dom/td (:name text))
        (dom/td (:hirestatus text))
        (dom/td {:className "center"} (:birthday text))
      )
    )
    (:employees @app-state )
  )
)

(defn buildMainWrapper [data]
  (dom/div {:id "page-wrapper"}
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/h1 {:className "page-header"} "New Employee List")
      )
    )

    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"} "New Employee List")


          (dom/div {:className "panel-body"}
            (dom/div {:className "dataTable_wrapper"}
              (dom/table {:className "table table-striped table-bordered table-hover" :id "dataTables-example" :style {:width "100%" :cellspacing "0"}}
                (dom/thead
                  (dom/tr
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :emplist/empcode) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :emplist/name) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :emplist/hirestatus) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :emplist/birthday))
                  )
                )
              )
            )
          )
        )
      )
    )
  
  )
)


(defn onMount [data]
 
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/newemplist)
  )
  (swap! app-state assoc-in [:sysmenus] (:sysmenus @t5pcore/app-state))

  (getNewEmployees)
)



(defcomponent newemplist-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "Update happened") 
    (jquery
      (fn []
        (-> (jquery "#side-menu")
          (.metisMenu)
        )
      )
    )
    ;; (if (> (:employees @app-state) 0)
    ;;   (jquery
    ;;     (fn []
    ;;       (-> (jquery "#dataTables-example")
    ;;         (.DataTable "responsive" "true")
    ;;       )
    ;;     )
    ;;   )
    ;; )

  )

  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      ;(dom/h1 "About Page")
      (buildMainWrapper data)
    )
  )
)




(sec/defroute newemplist-page "/newemplist" []
  (om/root newemplist-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))




