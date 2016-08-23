(ns t5pmobile.hrmshome  (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            [t5pmobile.settings :as settings]

            [t5pmobile.newemplist :as newemplist]
            [t5pmobile.newemprec :as newemprec]
            
  )
  (:import goog.History)
)

(enable-console-print!)


(defonce app-state (atom  {:view 2  :current "Admin"} ))



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn menus-to-map [menu]
  (let [     
      newdata {:menucode (get menu "menucode") :menulevel (get menu "menulevel") :menuopt (get menu "menuopt")
               :menuorder (get menu "menuorder") :name (get menu "name") :submenu (get menu "submenu")}
    ]
    ;(.log js/console newdata)
    newdata
  )
  
)

(defn OnGetSysMenu [response]
  (let [ 
    newdata (map menus-to-map response)
    ]
    (swap! app-state assoc-in [:sysmenus]   (into []  newdata) )
    ;(.log js/console newdata)
  )
)


(defn getSysMenus []
  (GET (str settings/apipath "api/sysmenu") {
    :handler OnGetSysMenu
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @app-state)))) }
  })
)

(defn buildSysMenuLevel2 [data]
  (map (fn [text]
    (dom/li
      (dom/a {:href "#"} 
        (:name text)
      )
    )
    )
    (sort 
      #(compare ( :menuorder %1) ( :menuorder %2)) 
      (filter (fn [x] ( = (:menulevel x) 0  )         )
        (into[] (:sysmenus data )  )   
      )
    )
  )
)


(defn displaySystemMenuBlock [data]
  (dom/li
    (dom/a {:href "#"}
      (dom/i {:className "fa fa-sitemap fa-fw"})
      (dom/span {:className "fa arrow"})
      "System Menu"
    )
    (dom/ul {:className "nav nav-second-level collapse" :aria-expanded "false" :style {:height "0px"}}
      (buildSysMenuLevel2 data)
    )
  )
)

(defn displaySideBarBlock [data]
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
            "Charts"
            (dom/span {:className "fa arrow"})
            
          )
          (dom/ul {:className "nav nav-second-level"}
            (dom/li
              (dom/a {:href "#"} "Flot Charts")
            )
            (dom/li
              (dom/a {:href "#"} "Morris.js Charts")
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
        ;(displaySystemMenuBlock data)
      )
    )
  )
)

(defn onMount [data]
  (swap! app-state assoc-in [:current] 
       (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/hrms)
  )

  (getSysMenus)
)


(defcomponent hrms-home-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (render [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      (dom/h1 {:style {:margin-top "100px"}} "About Page")
      ;(displaySideBarBlock data)
    )
  )
)




(sec/defroute hrms-home-page "/hrms" []
  (om/root hrms-home-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))

(defn main []
  (-> js/document
      .-location
      (set! "#/hrms"))

  ;(aset js/window "location" "#/login")
)
  
(main)





