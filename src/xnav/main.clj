(ns xnav.main
  "Entry-point into the application."
  (:require [clojure.spec.alpha :as s]
            [xnav.cli :as cli]
            [utils.common :as c])
  (:gen-class))

(set! *warn-on-reflection* true) ; for graalvm

(s/fdef -main
        :args (s/cat :args (s/coll-of string?))
        :ret (s/or :success zero?
                   :error pos-int?))

(defn -main
  "Entry-point into the application."
  [& args]
  (-> args cli/parse cli/run-cmd c/exit!))

