(ns xnav.cli
  "Command-line interface abstraction."
  (:require [better-cond.core :as b]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [xnav.workspace :as workspace]
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
    (r/r :error "No command specified. Try running: xnav --help"
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
                            "xnav --help"]
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
      (b/cond
        let [workspace-spec (-> parse-r :cmd-args first)]

        (nil? workspace-spec)
        (r/r :error "No workspace specification provided")

        let [workspace-spec (str/lower-case workspace-spec)]

        (= "next" workspace-spec)
        (workspace/activate-next-workspace)

        (= "previous" workspace-spec)
        (workspace/activate-previous-workspace)

        (= "last" workspace-spec)
        (workspace/activate-last-workspace)

        let [workspace-num (c/parse-int workspace-spec)]

        (zero? workspace-num)
        (r/r :error (c/fmt ["Expected workspace specification to be a "
                            "positive integer, 'next' or 'previous' but "
                            "was '%s'"]
                           workspace-spec))

        :else
        (workspace/activate-workspace-num workspace-num)))))

