(ns xnav.workspace.utils
  (:require [better-cond.core :as b]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [utils.common :as c]
            [utils.results :as r]))



(def cache-dir (str (System/getProperty "user.home") "/.cache/xnav"))
(def last-workspace-file (str cache-dir "/last-workspace"))



(defn ensure-cache-dir
  "Create cache dir to store last active workspace."
  []
  (io/make-parents cache-dir "some-file")
  (spit last-workspace-file ""))



(s/fdef get-workspace-count
        :ret (s/or :count int?
                   :error-result ::r/result))

(defn get-workspace-count
  "Get the total number of workspaces."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "get_num_desktops")]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r "Failed to count workspaces due to: ")

    let [workspace-cnt (-> cmd-r :out str/trim c/parse-int)]

    (not (pos-int? workspace-cnt))
    (r/prepend-msg cmd-r "Failed to count workspaces due to: ")

    :else
    workspace-cnt))



(s/fdef get-active-workspace
        :ret (s/or :num int?
                   :error-result ::r/result))

(defn get-active-workspace
  "Get the active workspace number."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "get_desktop")]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r "Failed to get the active workspace due to: ")

    let [active-workspace (-> cmd-r :out str/trim (c/parse-int :fallback -1))]

    (neg? active-workspace)
    (r/prepend-msg cmd-r "Failed to get the active workspace due to: ")

    :else
    (inc active-workspace)))



(s/fdef get-last-workspace
        :ret (s/or :num int?
                   :error-result ::r/result))

(defn get-last-workspace
  "Get the last active workspace number."
  []
  (b/cond
    let [last-workspace-str (slurp last-workspace-file)]

    (str/blank? last-workspace-str)
    (r/r :error "Last workspace unknown")

    let [last-workspace-num (-> last-workspace-str str/trim c/parse-int)]

    (zero? last-workspace-num)
    (r/r :error (c/fmt ["Expected last workspace to be a positive integer but "
                        "was '%s'"]
                       last-workspace-num))

    :else
    last-workspace-num))



(defn cache-active-workspace-num
  "Save/cache the currently active workspace (so it can be retrieved later as
  the 'last active workspace'."
  []
  (b/cond
    let [curr-workspace-r (get-active-workspace)]

    (r/failed? curr-workspace-r)
    (r/prepend-msg curr-workspace-r
                   "Can't update last workspace file due to: ")

    do (spit last-workspace-file curr-workspace-r)

    :else
    (r/r :success "")))



(comment
  (ensure-cache-dir)
  (get-workspace-count)
  (get-active-workspace)
  (get-last-workspace)
  (cache-active-workspace-num))
