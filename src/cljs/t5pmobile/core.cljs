(ns t5pmobile.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
            
  )
  (:import goog.History)
)

(enable-console-print!)

(defonce app-state (atom {:view 0}))



(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))



(defcomponent navigation-view [_ _]
  (render
   [_]
   (let [style {:style {:margin "10px;"}}]
     (dom/div style
              (dom/a (assoc style :href "#/home") 
                     "Home")
              (dom/a (assoc style :href "#/something") 
                     "Something")
              (dom/a (assoc style :href "#/messages") 
                     "Messages")
              (dom/a (assoc style :href "#/leave") 
                     "Leave")
              (dom/a (assoc style :href "#/login") 
                     "Logout")
      )
    )
  )
)


(defcomponent logout-view [_ _]
  (render
   [_]
   (let [style {:style {:margin "10px;"}}]
     (dom/div style
       (dom/a (assoc style :href "#/login") 
              "Login"
              )
      )
    )
  )
)



(defmulti website-view
  (
    fn [data _]
      (:view (if (= data nil) @app-state @data ))
  )
)

(defmethod website-view 0
  [data owner] 
  (logout-view data owner)
)

(defmethod website-view 1
  [data owner] 
  (navigation-view data owner)
)


(defn index-page-view [app owner]
 (reify
   om/IRender
   (render
     [_]
      (dom/div
        (om/build website-view app {})
        ;(dom/h1 "Index Page")
      )
    )
  )
)


(sec/defroute index-page "/" []
  (om/root index-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))



(defn root-component [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil (dom/h1 nil (:text app)))
      ;(om/build website-view app )
    )
  )
)

(defn main []
  (-> js/document
      .-location
      (set! "#/")))

(main)




