(ns wminde.workspace
  (:require [better-cond.core :as b]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [utils.common :as c]
            [utils.results :as r]))



(def cache-dir (str (System/getProperty "user.home") "/.cache/wminde"))
(def last-workspace-file (str cache-dir "/last-workspace"))



(defn init
  "Create cache dir to store last active workspace."
  []
  (io/make-parents cache-dir "some-file")
  (spit last-workspace-file ""))



(s/fdef get-num-workspaces
        :ret (s/or :count int?
                   :error-result ::r/result))

(defn get-num-workspaces []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "get_num_desktops")]

    (r/failed? cmd-r)
    (assoc cmd-r :message "Failed to get number of workspaces")

    let [num-workspaces (-> cmd-r :out str/trim c/parse-int)]

    (not (pos-int? num-workspaces))
    (assoc cmd-r :message "Failed to get number of workspaces")

    :else
    num-workspaces))



(s/fdef get-active-workspace
        :ret (s/or :num int?
                   :error-result ::r/result))

(defn get-active-workspace
  "Get the active workspace number (1-based)."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "get_desktop")]

    (r/failed? cmd-r)
    (assoc cmd-r :message "Failed to get the active workspace")

    let [active-workspace (-> cmd-r :out str/trim c/parse-int)]

    (not (pos-int? active-workspace))
    (assoc cmd-r :message "Failed to get the active workspace")

    :else
    (inc active-workspace)))



(s/fdef activate-workspace-num
        :args (s/cat :num pos-int?)
        :ret ::r/result)

(defn activate-workspace-num
  "Set the active workspace (1-based).

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  [num]
  (b/cond
    (not (pos-int? num))
    (r/r :error
         (c/fmt "Workspace number must be a positive integer but was '%d'"
                num))

    let [cmd-r (c/sh-r "xdotool" "set_desktop" (str (dec num)))]

    (r/failed? cmd-r)
    (assoc cmd-r :message (str "Failed to set the active workspace to " num))

    do (spit last-workspace-file num)

    :else
    cmd-r))



(s/fdef activate-next-workspace
        :ret ::r/result)

(defn activate-next-workspace
  "Set the active workspace to the next (higher, 1-based) one.

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "set_desktop" "--relative" "--" "1")]

    (r/failed? cmd-r)
    (assoc cmd-r :message "Failed to activate the next workspace")

    let [curr-workspace-r (get-active-workspace)]

    do (if (r/failed? curr-workspace-r)
         (binding [*out* *err*]
           (println (:message curr-workspace-r)))
         (spit last-workspace-file curr-workspace-r))

    :else
    cmd-r))



(s/fdef activate-previous-workspace
        :ret ::r/result)

(defn activate-previous-workspace
  "Set the active workspace to the previous (lower, 1-based) one.

  NOTE: this will always return a successful result since the underlying tool
  (xdotool) never indicates an error."
  []
  (b/cond
    let [cmd-r (c/sh-r "xdotool" "set_desktop" "--relative" "--" "-1")]

    (r/failed? cmd-r)
    (assoc cmd-r :message "Failed to activate the previous workspace")

    let [curr-workspace-r (get-active-workspace)]

    do (if (r/failed? curr-workspace-r)
         (binding [*out* *err*]
           (println (:message curr-workspace-r)))
         (spit last-workspace-file curr-workspace-r))

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
    let [last-workspace-str (slurp last-workspace-file)]

    (str/blank? last-workspace-str)
    (r/r :warn "Last workspace unknown")

    let [last-workspace-num (-> last-workspace-str str/trim c/parse-int)]

    (zero? last-workspace-num)
    (r/r :error (c/fmt ["Expected last workspace to be a positive integer but "
                        "was '%s'"]
                       last-workspace-num))

    let [cmd-r (activate-workspace-num last-workspace-num)]

    (r/failed? cmd-r)
    (assoc cmd-r :message "Failed to activate the last active workspace")

    :else
    cmd-r))



(comment
  (init)
  (get-num-workspaces)
  (get-active-workspace)
  (activate-workspace-num 1)
  (activate-next-workspace)
  (activate-previous-workspace))

