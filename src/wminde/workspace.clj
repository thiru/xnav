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



(comment
  (get-num-workspaces))

