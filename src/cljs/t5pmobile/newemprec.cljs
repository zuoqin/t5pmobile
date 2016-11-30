(ns t5pmobile.newemprec (:use [net.unit8.tower :only [t]])
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

            [om.dom :as omdom :include-macros true]
            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]

            [cljs-time.format :as tf]
            
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
        :mainmenu {
          :newemprec "New Employee Maintenance"
          :newempform "New Employee to Be Hired"
        }
        :newemp{
          :empid "#"
          :empcode "Employee Code"
          :empname "Name"
          :hirestatus "Hire Status"
          :birthday "Birthday"
          :gender "Gender"
          :major "Major"
          :organization "Organization"
          :position "Position"
          :supervisor "Supervisor"
          :hiredate "Hire Date"
          :calendar "Calendar"
          :payrollgroup "Payroll Group"
        }
      }
      :cn{
        :mainmenu {
          :newemprec "新员工管理"
          :newempform "要雇佣的员工"
        }
        :newemp{
          :empid "号码"
          :empcode "员工编码"
          :empname "姓名"
          :hirestatus "雇佣状态"
          :birthday "生日"
          :gender "性别"
          :major "专业"
          :organization "所属组织"
          :position "应聘职位"
          :supervisor "主管"
          :hiredate "雇佣日"
          :calendar "日历"
          :payrollgroup "薪资组"
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



(defn OnGetNewEmployee [response]
  (let [ 
    newdata {
      :empid (get response "empid") 
      :empcode (get response "empcode") :empname (get response "empname")
      :hirestatus (get response "hirestatus") :birthday (get response "birthday")
      :gender (get response "gender")
      :major (get response "major")
      :payrollgroupid (get response "payrollgroupid")
      :payrollgroupname (get response "payrollgroupname")
      }
    ]
    (swap! app-state assoc-in [:employee]  newdata )
    (swap! app-state assoc-in [:form :empcode]  (:empcode newdata )  )
    (swap! app-state assoc-in [:form :payrollgroupid]  (:payrollgrouid newdata )  )
    ;; (if (> (count (:payrollgroups @app-state)) 0)
    ;;   (jquery
    ;;     (fn []
    ;;       (-> (jquery "#payrollgroups" )
    ;;         (.selectpicker "val" (:payrollgroupid newdata))
    ;;       )
    ;;     )
    ;;   )

    ;; )
  )
)


(def custom-formatter1 (tf/formatter "MMM dd yyyy hh:mm:ss"))
;(def custom-formatter2  (tf/formatter (:datemask (:User @t5pcore/app-state)) ))

(def custom-formatter3 (tf/formatter "yyyy/MM/dd"))

(defn setdatepickers []
  (let [custom-formatter2  (tf/formatter (:datemask (:User @t5pcore/app-state)) )] 

    (jquery
      (fn []
        (-> (jquery (str "#hiredate") )
          (.datepicker #js{:format "dd/mm/yyyy" })
          (.on "show"
            (fn [e] (
               let [
                 dt (js/Date (.. e -date))
                 ;dtstring (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                 dtstring (if
                   (= (count (.. e -dates) ) 0)
                     nil ;(tf/parse custom-formatter1 "May 26 2016 08:00:00"  )
                     (tf/parse custom-formatter1 (subs (str (.. e -date)  )  4 24)  )
                 )


               ]
               ;;(swap! app-state assoc-in [:leavetypes :ivyt03 :fields :leavefromdate :value] (str (subs dt 8 10)  "/05/"    (subs dt 11 16)  ) )

              ;;(.log js/console (str (.. e -date)  ) )
              ;(.log js/console (count (.. e -dates)))
              ;(.log js/console (subs (str (.. e -date)  ) 4 24))
               (
                  if (= dtstring nil) "nil"
                  (
                    if (not= 
                                        ;( (keyword (.. e -target -id)) (:leaveapp @app-state)  )  
                        ( (keyword (.. e -target -id)) (:leaveapp @app-state))
                        (tf/unparse custom-formatter2 dtstring)

                        )
                    (
                      ;setNewLeaveAppValue (.. e -target -id) (tf/unparse custom-formatter2 dtstring)
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
  ;(.log js/console (get field  "fieldcode"    )   )  
  
)


(defn onDropDownChange [id value]
  ;(.log js/console () e)
  (swap! app-state assoc-in [:form (keyword id)] value) 
)

(defn handle-change [e owner]
  (.log js/console () e)
  (swap! app-state assoc-in [:form (keyword (.. e -target -id))] 
    (.. e -target -value)
  ) 
)

(defn setpayrollgroupsDropDown []
  (put! ch 46) 
  (.log js/console (count (:payrollgroups @app-state)) )
  (jquery
    (fn []
      (-> (jquery "#payrollgroups" )
        (.selectpicker {})
        (.on "change"
          (fn [e]
            (
              onDropDownChange (.. e -target -id) (.. e -target -value)
            )
          )
        )
      )
    )
  )
)

(defn setorganizationsDropDown []
  (put! ch 47) 
  (jquery
    (fn []
      (-> (jquery "#organizations" )
        (.selectpicker {:noneSelectedText "your none beautiful text"})
        (.on "change"
          (fn [e]
            (
              onDropDownChange (.. e -target -id) (.. e -target -value)
            )
          )
        )
      )
    )
  )
)


(defn setpositionsDropDown []
  (put! ch 48) 
  ;(.log js/console (count (:positions @app-state)) )
  (jquery
    (fn []
      (-> (jquery "#positions" )
        (.selectpicker {})
        (.on "change"
          (fn [e]
            (
              onDropDownChange (.. e -target -id) (.. e -target -value)
            )
          )
        )
      )
    )
  )
)

(defn setcalendarsDropDown []
  (put! ch 48) 
  ;(.log js/console (count (:positions @app-state)) )
  (jquery
    (fn []
      (-> (jquery "#calendars" )
        (.selectpicker {})
        (.on "change"
          (fn [e]
            (
              onDropDownChange (.. e -target -id) (.. e -target -value)
            )
          )
        )
      )
    )
  )
)

(defn setallDropDowns []
  (setpayrollgroupsDropDown)
  (setorganizationsDropDown)
  (setcalendarsDropDown)
  (setpositionsDropDown)
  (jquery
    (fn []
      (-> (jquery "#side-menu")
        (.metisMenu)
      )
    )
  )
)

(defn setcontrols [value]
  (case value
    42 (setorganizationsDropDown)
    43 (setpayrollgroupsDropDown)
    44 (jquery
        (fn []
          (-> (jquery "#calendars" )
              (.selectpicker {})
              (.on "change"
                (fn [e] (                       
                  onDropDownChange (.. e -target -id) (.. e -target -value) 
                  )
                )
              )
            )      
          )
        )
    45 (jquery
        (fn []
          (-> (jquery "#positions" )
              (.selectpicker {})
              )      
          )
        )


    46 (jquery
         (fn []
           (-> (jquery "#payrollgroups" )
               (.selectpicker "val"
                 (if (nil? (:payrollgroupid (:employee @app-state)))
                   (:payrollgroups (:form @app-state))
                   (:payrollgroupid (:employee @app-state))
                   
                   ) )
           )
         )
       )
    47 (jquery
         (fn []
           (-> (jquery "#organizations" )
               (.selectpicker "val" (:orgid (:employee @app-state)))
           )
         )
       )
    48 (jquery
         (fn []
           (-> (jquery "#calendars" )
               (.selectpicker "val" (:calendar (:employee @app-state)))
           )
         )
       )
    50 (setdatepickers)
    51 (setallDropDowns)
  )
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           ;.log js/console v
           ;(setcalculatedfields) 
           setcontrols v
           
           ;(.log js/console v)  
          )
        )
      )
    )
  )
)

(initqueue)


(defn getNewEmployee []
  (GET (str settings/apipath "api/empnew?id=" (:empid @app-state)) {
    :handler OnGetNewEmployee
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn positions-to-map [position]
  (let [     
      newdata {
               :positionid (get position "positionid") 
               :positioncode (get position "positioncode") 
               :name (get position "name")
               :orgname (get position "orgname")
               :orgid (get position "orgid")
      }
    ]
    newdata
  ) 
)


(defn calendars-to-map [calendar]
  (let [     
      newdata {
               :code (get calendar "code") 
               :name (get calendar "name")
               :countryname (get calendar "countryname")
      }
    ]
    ;(.log js/console newdata)
    newdata
  ) 
)

(defn payrollgroups-to-map [payrollgroup]
  (let [     
      newdata {
               :payrollgroupid (get payrollgroup "payrollgroupid") 
               :name (get payrollgroup "name")
      }
    ]
    ;(.log js/console newdata)
    newdata
  )  
)

(defn organizations-to-map [organization]
  (let [     
      newdata {
               :orgid (get organization "orgid") 
               :orgcode (get organization "orgcode") :orgname (get organization "orgname")
               :orglevel (get organization "orglevel") :parentorgid (get organization "parentorgid")
      }
    ]
    ;(.log js/console newdata)
    newdata
  )  
)

(defn OnGetPositions [response]
  (let [ 
    newdata (map positions-to-map response)
    ]
    (swap! app-state assoc-in [:positions]  (into [] newdata) )
    (put! ch 45)
  )
)

(defn OnGetCalendars [response]
  (let [ 
    newdata (map calendars-to-map response)
    ]
    (swap! app-state assoc-in [:calendars]  (into [] newdata) )
    (put! ch 44)
  )
)


(defn OnGetPayrollGroups [response]
  (let [ 
    newdata (map payrollgroups-to-map response)
    ]
    (swap! app-state assoc-in [:payrollgroups]  (into [] newdata) )
    (.log js/console "Received payroll groups list in newEmpRec")
    (put! ch 43)
  )
)

(defn OnGetOrganizations [response]
  (let [ 
    newdata (map organizations-to-map response)
    ]
    (swap! app-state assoc-in [:organizations]  (into [] newdata) )
    (put! ch 42)
  )
)

(defn getPositions []
  (GET (str settings/apipath "api/position") {
    :handler OnGetPositions
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)

(defn getCalendars []
  (GET (str settings/apipath "api/calendar") {
    :handler OnGetCalendars
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)

(defn getPayrollGroups []
  (GET (str settings/apipath "api/payrollgroups") {
    :handler OnGetPayrollGroups
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn getOrganizations []
  (GET (str settings/apipath "api/organization") {
    :handler OnGetOrganizations
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)


(defn buildPositionsList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:positionid text) :data-subtext (:positioncode text) :value (:positionid text)
                    :onChange #(handle-change % owner)} (:name text))
    )
    (:positions @app-state )
  )
)

(defn buildCalendarsList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:code text) :data-subtext (:countryname text) :value (:code text)
                    :onChange #(handle-change % owner)} (:name text))
    )
    (:calendars @app-state )
  )
)


(defn buildOrganizationsList [data owner]
  ;;(omdom/option #js {:data-subtext "Rep California"} "Tom Foolery")
  (map
    (fn [text]
      (dom/option {:key (:orgid text) :data-subtext (:orgcode text) :value (:orgid text)
                    :onChange #(handle-change % owner)} (:orgname text))
    )
    (:organizations @app-state )
  )
)


(defn buildPayrollGroupsList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:payrollgroupid text) :data-subtext (:payrollgroupid text) :value (:payrollgroupid text)
                    :onChange #(handle-change % owner)} (:name text))
    )
    (:payrollgroups @app-state )
  )
)





(defn buildMainWrapper [data owner]
  (dom/div {:id "page-wrapper"}
    ;; (dom/div {:className "row"}
    ;;   (dom/div {:className "col-lg-12"}
    ;;     (dom/h1 {:className "page-header"} "New Employee List")
    ;;   )
    ;; )

    (dom/div {:className "row"}
      (dom/div {:className "col-lg-12"}
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"}
            (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :mainmenu/newempform)
          )


          (dom/div {:className "panel-body"}
            (dom/div {
              :className (if (= (count (:empcode (:form @app-state)) ) 0) "form-group has-error" "form-group")
              }
              ;(dom/label  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/empcode))
              (dom/input {
                :id "empcode"
                :className "form-control"
                :placeholder (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/empcode)
                :defaultValue (:empcode (:employee @app-state))}
              )
            )

            (dom/div {:className "form-group"}
              ;(dom/label  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/empcode))
              (dom/input {
                :id "empname"
                :className "form-control"
                :placeholder (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/empname)   }
              )
            )

            (dom/div {:className "form-group"}
              ;(dom/label  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/empcode))
              (dom/input {
                :id "hiredate"
                :className "form-control"
                :placeholder (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/hiredate)   }
              )
            )

            (dom/div {:className "form-group"}
              (dom/p
                (dom/label {:className "control-label" :for "organizations" }
                  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/organization)
                )
              
              )
             
              (omdom/select #js {:id "organizations"
                                 :className "selectpicker"
                                 :data-show-subtext "true"
                                 :data-live-search "true"
                                 :onChange #(handle-change % owner)
                                 }
                ;(dom/option {:key 0 :data-subtext "" :value 0} "")
                (buildOrganizationsList data owner)
              )            
            )

            (dom/div {:className "form-group"}
              (dom/p
                (dom/label {:className "control-label" :for "positions" }
                  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/position)
                )
              
              )
             
              (omdom/select #js {:id "positions"
                                 :className "selectpicker"
                                 :data-show-subtext "true"
                                 :data-live-search "true"
                                 :onChange #(handle-change % owner)
                                 }                
                (buildPositionsList data owner)
              )            
            )

            (dom/div {:className "form-group"}
              (dom/p
                (dom/label {:className "control-label" :for "payrollgroups" }
                  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/payrollgroup)
                )
              
              )
             
              (omdom/select #js {:id "payrollgroups"
                                 :className "selectpicker"
                                 :data-show-subtext "true"
                                 :data-live-search "true"
                                 :onChange #(handle-change % owner)
                                 }
                (buildPayrollGroupsList data owner)
              )            
            )

            (dom/div {:className "form-group"}
              (dom/p
                (dom/label {:className "control-label" :for "calendars" }
                  (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :newemp/calendar)
                )
              
              )
             
              (omdom/select #js {:id "calendars"
                                 :className "selectpicker"
                                 :data-show-subtext "true"
                                 :data-live-search "true"
                                 :onChange #(handle-change % owner)
                                 }                
                (buildCalendarsList data owner)
              )            
            )

          )
        )
      )
    )
  
  )
)


(defn onMount [data]
  (.log js/console "On Mount NewEmpRec")
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) my-tconfig :mainmenu/newemprec)
  )
  (swap! app-state assoc-in [:sysmenus] (:sysmenus @t5pcore/app-state))

  (getNewEmployee)
  (if (= (count (:organizations @app-state)) 0)
    (getOrganizations)
    (put! ch 42)
  )

  (if (= (count (:payrollgroups @app-state)) 0)
    (getPayrollGroups)
    (put! ch 43)
  )

  (if (= (count (:calendars @app-state)) 0)
    (getCalendars)
    (put! ch 44)
  )

  (if (= (count (:positions @app-state)) 0)
    (getPositions)
    (put! ch 45)
  )

  (put! ch 50)   ;;setdatepickers 

)

(defn onDidMount []
  ;(.log js/console "Update NewEmpRec happened") 
  ;; (jquery
  ;;   (fn []
  ;;     (-> (jquery "#side-menu")
  ;;       (.metisMenu)
  ;;     )
  ;;   )
  ;; )
  (put! ch 51)
  (.log js/console "Did mount newempRec happened") 
)

(defn onDidUpdate []
  ;(.log js/console "Update NewEmpRec happened") 
  ;; (jquery
  ;;   (fn []
  ;;     (-> (jquery "#side-menu")
  ;;       (.metisMenu)
  ;;     )
  ;;   )
  ;; )
  (put! ch 51)
  (.log js/console "Did update newempRec happened") 
)


(defn newemprec-page-view [data owner]

  (reify
    om/IWillMount
    (will-mount [_]
      (onMount data)
    )
    om/IDidMount
    (did-mount [_]
      (onDidMount) 
    )
    om/IDidUpdate
    (did-update [_ _ _]
      (onDidUpdate) 
    )
    om/IRender
    (render [_]
      (dom/div
        (om/build t5pcore/website-view data {})
        (if (and
              (> (count (:organizations @app-state)) 0)
              (> (count (:calendars @app-state)) 0)
              (> (count (:payrollgroups @app-state)) 0)
              (> (count (:positions @app-state)) 0)
              (> (count (:employee @app-state)) 0)
            )
          (buildMainWrapper data owner)
        )
        
      )
    )
  )

)




(sec/defroute newemprec-page "/newemprec/:id" {id :id}
  (let [empid id]
    (swap! app-state assoc-in [:empid]   empid )
    (om/root newemprec-page-view
           app-state
           {:target (. js/document (getElementById "app"))})
  )
)

