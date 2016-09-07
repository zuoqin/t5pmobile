(ns t5pmobile.payrolledit (:use [net.unit8.tower :only [t]])
    (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [t5pmobile.settings :as settings]
            [ajax.core :refer [GET POST]]

            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]

            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            
  )
  (:import goog.History)
)

(enable-console-print!)

(def ch (chan (dropping-buffer 2)))

(def my-tconfig
  {:dev-mode? true
    :fallback-locale :en
    :dictionary{
      :en{
        :payrollgroupslist{
          :payrollgroupid "#"
          :pay_curyear "pay_curyear"
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
        :payrollgroupslist{
          :payrollgroupid "号码"
          :pay_curyear "pay_curyear"
          :name "Name"
          :begindate "begindate"
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


(defonce app-state (atom  {:view 2  :state 0 :current "Salary and Income Tax Calculation"} ))
(def jquery (js* "$"))



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn payrollgroups-to-map [payrollgroup]
  (let [     
      newdata {
               :payrollgroupid (get payrollgroup "payrollgroupid") 
               :aftercalculation (get payrollgroup "aftercalculation") :name (get payrollgroup "name")
               :begindate (get payrollgroup "begindate") :enddate (get payrollgroup "enddate")
               :pay_curyear (get payrollgroup "pay_curyear")
               :totalemp (get payrollgroup "totalemp")
               :attbegin (get payrollgroup "attbegin") :attend (get payrollgroup "attend")
               
      }
    ]
    newdata
  )
  
)

(def js-object  (clj->js
  {
    :autoWidth false
    :columnDefs
    [
      {
        :orderable false
        :className "select-checkbox"
        :targets 0
      }
      {
        :visible false
        :targets [1]
      }
      { :width "20%" 
        :targets 2
      }
      { :className "dt-body-right"
        :targets [8]
      }
      ;; { :width "5%"
      ;;   :targets 4
      ;; }
      ;; { :width "5%"
      ;;   :targets 5
      ;; }
      ;; { :width "5%"
      ;;   :targets 6
      ;; }
      ;; { :width "5%"
      ;;   :targets 7
      ;; }
      ;; { :width "5%"
      ;;   :targets 8
      ;; }
      ;; { :width "5%"
      ;;   :targets 9
      ;; }

      ;; { :width "5%"
      ;;   :targets 10
      ;; }

      ;; { :width "5%"
      ;;   :targets 11
      ;; }
    ]
    :select {
            :style "multi"
            :selector "td:first-child"
        }
    :lengthMenu [[5, 10, 25, -1], [5, 10, 25, "All"]]
  }
))


(defn gotoSelection [empid]
  (-> js/document
      .-location
      (set! (str "#/newemprec/" empid) ))
)


(defn setcontrols []
  (.log js/console "In set controls")
  (swap! app-state assoc-in [:state] 1 )
  (jquery
    (fn []
      (-> (jquery "#dataTables-example" )
        (.DataTable js-object)
        (.on "click" "tr"
          (fn [e] (
            let [table (-> (jquery "#dataTables-example")
                              (.DataTable)   
                            )
                 res (.data (.row table (.. e -currentTarget)) )


            ]
            (gotoSelection (first res)) 
            )
          )
        )
      )      
    )
  )
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go
      (take! ch(
        fn [v] ( 
           setcontrols 
          )
        )
      )
    )
  )
)

(initqueue)

(defn UpdatePayrollGroupsDataTable []
  (.log js/console "Updating DataTable")
  
  (put! ch 42)
)


(defn OnGetPayrollGroups [response]
  (let [ 
        newdata (map payrollgroups-to-map response)
    ]
    (swap! app-state assoc-in [:payrollgroups]   (into []  newdata) )
  )
)


(defn getPayrollGroups []
  (GET (str settings/apipath "api/payrollgroups?param=0") {
    :handler OnGetPayrollGroups
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn buildPayrollGroupsList []
  (map
    (fn [text]
      (dom/tr {:className "odd gradeX"}
        (dom/td "")
        (dom/td (:payrollgroupid text))
        (dom/td (:name text))
        (dom/td (:pay_curyear text))
        (dom/td {:className "center"} (:begindate text))
        (dom/td {:className "center"} (:enddate text))
        (dom/td {:className "center"} (:attbegin text))
        (dom/td {:className "center"} (:attend text))
        (dom/td {:className "right"} (:totalemp text))
        ;; (dom/td (:aftereditlist text))
        ;; (dom/td (:afterprvcalc text))
        ;; (dom/td (:aftercalculation text))
        ;; (dom/td (:afterpost text))
      )
    )
    (:payrollgroups @app-state )
  )
)

(def js-selectobject  (clj->js
  {
    :selected true
  }
))


(defn datatablerow-to-payrollgroup [row]
  (.log js/console (nth row 1))
  (nth row 1)
)


(defn setSelectedPayrollGroups [selection]
  (let [ 
        newdata (map datatablerow-to-payrollgroup selection)
    ]
    (swap! app-state assoc-in [:selectedpayrollgroups] newdata )
    (dorun newdata  ) 
  )
)



(defn calculateEmployees []
  (let [
    params  (clj->js  { :selected true }  )
    table (-> (jquery "#dataTables-example")
                  (.DataTable)   
                  )
        jsdata (.data (.rows table params) )
        data (js->clj (.toArray jsdata))
    ]
    (.log js/console (.stringify js/JSON (clj->js (.toArray jsdata)) nil 2))
    (setSelectedPayrollGroups data )
  )

)


(defn addModal []
   (dom/div

   ;(b/button {:type "button" :className "btn btn-info btn-lg" :data-toggle "modal" :data-target "#myModal"} "Open Modal")
           (dom/div {:id "leaveModal" :className "modal fade" :role "dialog"}
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
            
           (dom/div

            ( b/button {:bs-style "primary"
                        :onClick (fn [e](calculateEmployees))
                        :disabled? (not= true  true)  } "Submit")
            )   
   )

)

(defn buildLoadingWrapper [data]
  (dom/div {:id "page-wrapper"}
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/img {:src "/images/loader.gif"})
      )
    )


  )
  
)

(defn buildMainWrapper [data]
  (dom/div {:id "page-wrapper"}
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/h1 {:className "page-header"} "Salary and Income Tax Calculation")
      )
    )
    
    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"} "Salary and Income Tax Calculation")


          (dom/div {:className "panel-body"}
            (dom/div {:className "dataTable_wrapper"}
              (dom/table {:className "table table-striped table-bordered table-hover" :id "dataTables-example" :style {:width "100%" :cellspacing "0"}}
                (dom/thead
                  (dom/tr
                    (dom/th "" )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/payrollgroupid) )
                    (dom/th (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/name) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/pay_curyear) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/begindate) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/enddate) )
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/attbegin))
                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/attend))


                    (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/totalemp))
                    ;; (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/aftereditlist))
                    ;; (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/afterprvcalc))
                    ;; (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/aftercalculation))
                    ;; (dom/th  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :payrollgroupslist/afterpost))
                  )
                )
                (dom/tbody
                  (buildPayrollGroupsList)

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
 
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/payrollcalculation)
  )
  (swap! app-state assoc-in [:sysmenus] (:sysmenus @t5pcore/app-state))

  (if (= (count (:payrollgroups @app-state) )  0)
    (getPayrollGroups)
    (UpdatePayrollGroupsDataTable)
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
   (.log js/console (str "Update happened state =" (:state @app-state) " payrollgroups count = " (count (:payrollgroups @app-state))) ) 
   (if (> (count (:payrollgroups @app-state)) 0)
     (if (= (:state @app-state) 0)
       (UpdatePayrollGroupsDataTable) 
     )
   )
)

(defcomponent payrollcalculation-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state] 
    (onDidUpdate)
  )

  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      (if (> (count (:payrollgroups @app-state)) 0 )
        (buildMainWrapper data)
        (buildLoadingWrapper data)
      )
      
    )
  )
)




(sec/defroute payrollcalculation-page "/editlist" []
  (om/root payrollcalculation-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))




