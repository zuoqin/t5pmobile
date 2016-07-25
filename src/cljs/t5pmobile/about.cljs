(ns t5pmobile.about
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            
  )
  (:import goog.History)
)

(enable-console-print!)




(defcomponent about-page-view [_ _]
  (render
   [_]
   (dom/div
    (om/build t5pcore/navigation-view {})
    (dom/h1 "About Page"))))




(sec/defroute about-page "/about" []
  (om/root about-page-view
           {}
           {:target (. js/document (getElementById "app"))}))




