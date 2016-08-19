(ns t5pmobile.hrmshome
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


(defonce app-state (atom  {:view 2  :current "Admin"} ))

(defcomponent hrms-home-page-view [data owner]
  (render
   [_]
   (dom/div
    (om/build t5pcore/website-view data {})
    (dom/h1 "About Page"))))




(sec/defroute hrms-home-page "/hrms" []
  (om/root hrms-home-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))




