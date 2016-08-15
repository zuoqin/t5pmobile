(ns t5pmobile.user  (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [t5pmobile.core :as t5pcore]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST PUT]]
            [t5pmobile.settings :as settings]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
  )
  (:import goog.History)
)

(def jquery (js* "$"))

(enable-console-print!)

(defonce app-state (atom  {:languages { :0 "English", :1 "简体中文", :2  "繁體中文" :3 "日本語"} } ))

(defn UpdatePageTitle [data]
  (swap! t5pcore/app-state assoc-in [:current] 
    (t (t5pcore/numtolang  (:language (:User @t5pcore/app-state))) t5pcore/my-tconfig :mainmenu/settings)
  ) 
)

(defn language-to-number [language]
  (let [     
      newdata {(keyword (str (get language "id")))  (get language "description") }
    ]
    ;(.log js/console (str "language: " language))
    ;(.log js/console newdata)
    newdata
  )
  
)

(defn OnGetLanguages [response]
  (let [ 
    newdata (map language-to-number response)
    ]
    ;(.log js/console "Starting to map Languages Array")
    ;(.log js/console newdata)
    (swap! app-state assoc :languages  (into {} newdata)) 
  )
)

(defn setNewUser [newUser]
   ;(swap! app-state assoc :page inc)
  (swap! t5pcore/app-state assoc-in [:User :language] 
    (:language newUser)
  )
)




(defn OnUpdateUser [response]
   ;(swap! app-state assoc :page inc)
  (let [     
      newdata {:language (get response "language") }
    ]
    (setNewUser newdata)
    (UpdatePageTitle t5pcore/app-state)
  ) 
)




(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn UpdateLanguage [data]
  (PUT (str settings/apipath "api/user") {
    :handler OnUpdateUser
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
    :format :json
    :params {:language data}
  })
)


(defn getLanguages [data]
  (GET (str settings/apipath "api/language") {
    :handler OnGetLanguages
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
  })
)





(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)



(defn onMount [data]
  (getLanguages data)
  (UpdatePageTitle data)
)


(defn alertselected [event]
  (jquery
   (fn []
     (-> (jquery "#languagesbtngroup")
       (.trigger  "click")
     )
   )
  )
  ;(swap! app-state assoc-in [:leaveapp :leavecode] event) 
   ;(.log js/console (str "#" "leavefromdate" )) 
  ;(setdatepickers2)
  ;(.log js/console event)
  (.log js/console ((keyword (subs event 1) ) (:languages @app-state)))
  (UpdateLanguage (js/parseInt (subs event 1)))
)

(defcomponent user-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (render [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]

      (dom/div
        (om/build t5pcore/website-view data {})
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
                    (b/menu-item {:key  (str (first item))  :on-select (fn [e](alertselected e))   
                      } (get item 1))
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


