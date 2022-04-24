(ns wminde.cli
  "Command-line interface abstraction."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [better-cond.core :as b]
            [puget.printer :as puget]
            [utils.common :as c]
            [utils.results :as r]))



(def version (-> (slurp "VERSION")
                 str/trim))
(def help (-> (slurp "HELP")
              (format version)))



(s/def ::cmd-name keyword?)
(s/def ::cmd-args (s/coll-of string?))
(s/def ::parse-r (s/keys :req-un [:r/level :r/message
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



(s/fdef action!
        :args (s/cat :cli-r ::parse-r)
        :ret nil?)

(defn action!
  "Action the specified CLI command."
  [parse-r]
  (when (r/failed? parse-r)
    (c/abort 1 (:message parse-r)))

  (case (:cmd-name parse-r)
    :help (println help)

    :version (println version)

    (:desktop :workspace)
    (println :TODO)))

