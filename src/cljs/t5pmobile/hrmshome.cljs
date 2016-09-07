(ns t5pmobile.hrmshome  (:use [net.unit8.tower :only [t]])
    (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            [t5pmobile.settings :as settings]

            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]

            [t5pmobile.newemplist :as newemplist]
            [t5pmobile.newemprec :as newemprec]
            [t5pmobile.payrollcalculation :as payrollcalculation]
            [t5pmobile.payrolledit :as payrolledit]

            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            
  )
  (:import goog.History)
)

(enable-console-print!)

(def ch (chan (dropping-buffer 2)))

(defonce app-state (atom  {:view 2 :state 0 :current "Admin"} ))
(def jquery (js* "$"))


(def my-tconfig
  {:dev-mode? true
    :fallback-locale :en
    :dictionary{
      :en{
        :eventslist{
          :empid "#"
          :birthday "birthday"
          :value "value"
          :age "age"
          :name "Name"
          :begindate "begindate"
          :enddate "enddate"
          :attbegin "attbegin"
          :attend "attend"
          :totalemp "total"
          :afterpayperiod "payper"
          :aftereditlist "edit"
          :afterprvcalc "prvcalc"
          :aftercalculation "calc"
          :afterlock "lock"
          :afterpay "pay"
          :afterpost "post"
        }
      }
      :cn{
        :eventslist{
          :employee "员工"
          :value "薪资"
          :empid "号码"
          :age "age"
          :name "Name"
          :birthday "birthday"
          :enddate "enddate"
          :attbegin "attbegin"
          :attend "attend"
          :totalemp "totalemp"
          :afterpayperiod "afterpayperiod"
          :aftereditlist "aftereditlist"
          :afterprvcalc "afterprvcalc"
          :aftercalculation "aftercalculation"
          :afterlock "afterlock"
          :afterpay "afterpay"
          :afterpost "afterpost"          
        }
      }
    }
  }
)

(def js-results-object  (clj->js
  {
    :autoWidth false
    :columnDefs
    [
      {
        :visible false
        :targets [0]
      }
      { :width "30%" 
        :targets 1
      }
      { :className "dt-body-right"
        :targets [3]
      }
    ]
    :select {
            :style "multi"
            :selector "td:first-child"
        }
    :lengthMenu [[5, 10, 25, -1], [5, 10, 25, "All"]]
  }
))


(defn events-to-map [event]
  (let [     
      newdata {
               :empid (get event "empid") 
               :name (get event "name")
               :birthday (get event "birthday")
               :age (get event "age")               
      }
    ]
    newdata
  )
  
)

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)

(defn OnGetEvents [response]
  (let [ 
        newdata (map events-to-map response)
    ]
    (swap! app-state assoc-in [:events]   (into []  newdata) )
  )
)



(defn getEvents []
  (GET (str settings/apipath "api/employee?type=1") {
    :handler OnGetEvents
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn setEventTable []
  (swap! app-state assoc-in [:state] 1 )
  (jquery
    (fn []
      (-> (jquery "#dataTables-events" )
        (.DataTable js-results-object)
      )    
    )
  )
)

(defn setcontrols [value]
  (case value
    42 (setEventTable)
  )
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go
      (take! ch(
        fn [v] ( 
           setcontrols v
          )
        )
      )
    )
  )
)

(initqueue)

(defn UpdateEventsDataTable []
  (.log js/console "Updating Events DataTable")
  
  (put! ch 42)
)


(defn addModal []
  (dom/div
    (dom/div {:id "calculateModal" :className "modal fade" :role "dialog"}
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
            (b/button {:type "button" :className (if (= (:calculate @app-state) 0) "btn btn-warning" "btn btn-warning m-progress" ) } "Calculate")
            (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
            
    (dom/div

     ( b/button {:bs-style "primary"
                 :disabled? (not= (:calculate @app-state) 0)  } "Submit")
     )   
   )
)


(defn buildEventsList []
  (map
    (fn [text]
      (dom/tr {:className "odd gradeX"}
        (dom/td (:empid text))
        (dom/td (:name text))
        (dom/td {:className "center"} (:birthday text))
        (dom/td {:className "right"} (:age text))
      )
    )
    (:events @app-state )
  )
)


(defn buildMainWrapper [data]
  (dom/div {:id "page-wrapper"}
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/h1 {:className "page-header"} "Corporate Events Calendar")
      )
    )
    
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"} "Coming Employees Birthdays Calendar")


          (dom/div {:className "panel-body"}
            (dom/div {:className "dataTable_wrapper"}
              (dom/table {:className "table table-striped table-bordered table-hover" :id "dataTables-events" :style {:width "100%" :cellspacing "0"}}
                (dom/thead
                  (dom/tr
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :eventslist/empid) )
                    (dom/th (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :eventslist/name) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :eventslist/birthday) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :eventslist/age) )
                  )
                )
                (dom/tbody
                  (buildEventsList)
                )
              )
            )
          )
        )
      )
    )

    (addModal)
  )
  
)


(defn onMount [data]
  (.log js/console "OnMount HRMS Home")
  (.log js/console (count (:sysmenus @app-state)))
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/hrms)
  )

  (if (= (count (:events @app-state) )  0)
    (getEvents)
    (UpdateEventsDataTable)
  )
)

(defn onDidUpdate []
   (jquery
     (fn []
       (-> (jquery "#side-menu")
         (.metisMenu)
       )
     )
   )
   ;; (.log js/console (str "Update happened state =" (:state @app-state)
   ;;   " payrollgroups count = " (count (:payrollgroups @app-state)))
   ;;   " calculation results count =  " (count (:calculationresults @app-state))
   ;; ) 
   (if (and
        (> (count (:events @app-state)) 0)
        (= (:state @app-state) 0)
        )
     (UpdateEventsDataTable)
   )
)

(defcomponent hrms-home-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state] 
    (onDidUpdate)
  )
  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      (buildMainWrapper data)
    )
  )
)




(sec/defroute hrms-home-page "/hrms" []
  (om/root hrms-home-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))

;; (defn main []
;;   (-> js/document
;;       .-location
;;       (set! "#/hrms"))

;;   ;(aset js/window "location" "#/login")
;; )
  
;(main)






