(ns wminde.workspace
  (:require [better-cond.core :as b]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [utils.common :as c]
            [utils.results :as r]))



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
        :ret (s/or :count int?
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



(s/fdef set-active-workspace
        :args (s/cat :num pos-int?)
        :ret ::r/result)

(defn set-active-workspace
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

    :else
    cmd-r))



(comment
  (get-num-workspaces)
  (get-active-workspace)
  (set-active-workspace 1))

