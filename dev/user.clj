(ns user
  "Initial namespace loaded when using a REPL (e.g. using `clj`)."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.pprint :refer :all]
            [clojure.reflect :refer :all]
            [clojure.repl :refer :all]
            [clojure.string :as str]
            [puget.printer :as puget]
            [reloader.core :as reloader]
            [utils.common :as c]
            [utils.results :as r]
            [xnav.cli :as cli]))

(defonce started? (atom false))

(when (not @started?)
  (reset! started? true)
  (reloader/start ["src" "dev"]))

(defonce ^{:doc "A short-hand to quit gracefully (otherwise rebel-readline hangs process)."}
  q (delay (System/exit 0)))

(defn PP
  []
  (puget/cprint *1))

