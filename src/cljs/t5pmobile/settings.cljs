(ns t5pmobile.settings
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

(def apipath "http://testapi.t5p.hk/")

