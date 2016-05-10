(ns t5pmobile.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [t5pmobile.core-test]))

(enable-console-print!)

(doo-tests 't5pmobile.core-test)
