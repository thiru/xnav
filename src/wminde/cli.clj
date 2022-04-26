(ns wminde.cli
  "Command-line interface abstraction."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [wminde.workspace :as workspace]
            [utils.common :as c]
            [utils.results :as r]))



(def version (-> (slurp "VERSION")
                 str/trim))
(def help (-> (slurp "HELP")
              (format version)))



(s/def ::cmd-name keyword?)
(s/def ::cmd-args (s/coll-of string?))
(s/def ::parse-r (s/keys :req-un [::r/level ::r/message
                                  ::cmd-name ::cmd-args]))
(s/fdef parse
        :args (s/cat :args (s/coll-of string?))
        :ret ::parse-r)

(defn parse
  "Parse the given CLI arguments."
  [args]
  (if (empty? args)
    (r/r :error "No command specified. Try running: wminde --help"
         :cmd-name nil
         :cmd-args [])

    (let [cmd-name (first args)
          cmd-kw (-> cmd-name str/lower-case keyword)]
      (cond
        (contains? #{:help :--help :-h} cmd-kw)
        (r/r :success ""
             :cmd-name :help
             :cmd-args [])

        (contains? #{:version :--version} cmd-kw)
        (r/r :success ""
             :cmd-name :version
             :cmd-args [])

        (contains? #{:desktop :workspace} cmd-kw)
        (r/r :success ""
             :cmd-name cmd-kw
             :cmd-args (rest args))

        :else
        (r/r :error (c/fmt ["Unrecognised command/option: '%s'. Try running: "
                            "wminde --help"]
                           (first args))
             :cmd-name cmd-kw
             :cmd-args [])))))



(s/fdef run-cmd
        :args (s/cat :cli-r ::parse-r)
        :ret ::r/result)

(defn run-cmd [parse-r]
  (if (r/failed? parse-r)
    parse-r
    (case (:cmd-name parse-r)
      :help
      (r/r :success help)

      :version
      (r/r :success version)

      (:desktop :workspace)
      (let [workspace-num-str (-> parse-r :cmd-args first)
            workspace-num (c/parse-int workspace-num-str)]
        (if (not (pos-int? workspace-num))
          (r/r :error (c/fmt "Workspace number must be a positive integer but was '%s'"
                             workspace-num-str))
          (do
            (workspace/set-active-workspace workspace-num)
            (r/r :success "")))))))

