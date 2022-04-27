(ns wminde.workspace
  (:require [better-cond.core :as b]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [wminde.workspace.utils :as wsu]
            [utils.common :as c]
            [utils.results :as r]))



(s/fdef activate-workspace-num
        :args (s/cat :num pos-int?)
        :ret ::r/result)

(defn activate-workspace-num
  "Set the active workspace.

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  [num]
  (b/cond
    (not (pos-int? num))
    (r/r :error
         (c/fmt "Workspace number must be a positive integer but was '%d'"
                num))

    do (-> wsu/cache-active-workspace-num r/print-msg)

    let [cmd-r (c/sh-r "xdotool" "set_desktop" (str (dec num)))]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r (str "Failed to set the active workspace to " num
                              " due to: "))

    :else
    cmd-r))



(s/fdef activate-next-workspace
        :ret ::r/result)

(defn activate-next-workspace
  "Set the active workspace to the next (higher) one.

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  []
  (b/cond
    do (-> wsu/cache-active-workspace-num r/print-msg)

    let [cmd-r (c/sh-r "xdotool" "set_desktop" "--relative" "--" "1")]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r "Failed to activate the next workspace due to: ")

    :else
    cmd-r))



(s/fdef activate-previous-workspace
        :ret ::r/result)

(defn activate-previous-workspace
  "Set the active workspace to the previous (lower) one.

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "set_desktop" "--relative" "--" "-1")]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r "Failed to activate the previous workspace due to: ")

    do (-> wsu/cache-active-workspace-num r/print-msg)

    :else
    cmd-r))



(s/fdef activate-last-workspace
        :ret ::r/result)

(defn activate-last-workspace
  "Set the active workspace to the last one that was active (like many OS' use
  ALT-TAB for windows).

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  []
  (b/cond
    let [last-workspace-r (wsu/get-last-workspace)]

    (r/failed? last-workspace-r)
    (r/prepend-msg last-workspace-r "Can't activate last workspace due to: ")

    do (-> wsu/cache-active-workspace-num r/print-msg)

    let [last-workspace-num last-workspace-r
         cmd-r (activate-workspace-num last-workspace-num)]

    (r/failed? cmd-r)
    (r/prepend-msg cmd-r "Can't activate last workspace due to: ")

    :else
    cmd-r))



(comment
  (activate-workspace-num 1)
  (activate-next-workspace)
  (activate-previous-workspace)
  (activate-last-workspace))

