(ns t5pmobile.user
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [t5pmobile.core :as t5pcore]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
            [t5pmobile.settings :as settings]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
  )
  (:import goog.History)
)

(def jquery (js* "$"))

(enable-console-print!)

(defonce app-state (atom  {:languages { :0 "English", :1 "简体中文", :2  "繁體中文" :3 "日本語"} } ))

(defn OnGetLanguages [response]
   ;(swap! app-state assoc :page inc)
   (swap! app-state assoc :languages  (get response "Languages")  )
   (.log js/console (:msgcount @t5pcore/app-state)) 

)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)




(defn getLanguages [data]
  ;(.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))
  ;(swap! t5pcore/app-state assoc-in [:messages] (conj (:messages data) {:showmessages 0}) )
  ;(swap! t5pcore/app-state  assoc-in [:messages] {:showmessages 0} )


   
  ;; (GET (str settings/apipath "api/laguages") {
  ;;   :handler OnGetLanguages
  ;;   :error-handler error-handler
  ;;   :headers {
  ;;     :content-type "application/json"
  ;;     :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  ;; })


  ;(vector "English" "简体中文" "繁體中文" "日本語")
  {:languages [
  {:id 0 :description "English"}  
  {:id 1 :description "简体中文"}
  {:id 2 :description "繁體中文"}
  {:id 3 :description "日本語"} ]}
)





(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)



(defn onMount [data]
  (getLanguages data)
)


(defn alertselected [event]
  ;(js/alert (str event  "ClojureScript says 'Boo!'" ))
 
  ;(swap! app-state assoc :leavecode  event)

  (jquery
   (fn []
     (-> (jquery "#languagesbtngroup")
       (.trigger  "click")
     )
   )
  )
  (swap! app-state assoc-in [:leaveapp :leavecode] event) 
   ;(.log js/console (str "#" "leavefromdate" )) 
  ;(setdatepickers2)
  1
)

(defcomponent user-page-view [data owner]
  (render [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]

      (dom/div
        (om/build t5pcore/website-view nil {})
        (dom/div (assoc styleprimary  :className "panel panel-primary")
          (dom/div {:className "panel-heading"}
            "语言"
          )

          (dom/ul {:className "list-group"}
            (dom/li {:className "list-group-item"}
              (b/button-group
                {:id "languagesbtngroup" }
                (b/dropdown {:title   (get (first (filter #(= (first %)  (keyword (str (:language (:User @t5pcore/app-state))))) (:languages @app-state))) 1)  }
                  (map (fn [item]
                    (b/menu-item {:key  (str (first item))  :on-select (fn [e](alertselected e))   } (get item 1))
                    ) (into [] (:languages @app-state)) 
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




(sec/defroute user-page "/user" []
  (om/root user-page-view
           t5pcore/app-state
           {:target (. js/document (getElementById "app"))}))


