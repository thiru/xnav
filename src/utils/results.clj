(ns utils.results
  "Generic facilities around reporting and validation."
  (:require [clojure.spec.alpha :as s]))

(set! *warn-on-reflection* true) ; for graalvm

;; ## Level
;; ----------------------------------------------------------------------------

(def levels
  "A generic map of levels that can be used for logging, reporting, etc.

  Negative values represent some level of failure. Non-negative values indicate
  some level of success, or just plain reporting."
  {:success 4
   :trace 3
   :info 2
   :warn 1
   :debug 0
   :error -1
   :fatal -2})

(s/def ::level #(contains? levels %))

;; ## Result
;; ----------------------------------------------------------------------------

(defn result?
  "Determine whether the given object is a valid result."
  [obj]
  (s/valid? ::result obj))

;; NOTE: Allow a message to be any type, since we can usually get a meaningful
;; string representation of most objects.
(s/def ::message any?)
(s/def ::result (s/keys :req-un [::level ::message]))
(s/fdef r
        :args (s/cat :level ::level
                     :message (s/? ::message)
                     :more (s/? map?))
        :ret ::result)

(defn r
  "Creates a map representing the result of some operation.

  I deliberately chose to use a very short function name because it will be
  used heavily throughout the codebase. Perhaps in limited cases the length of
  the name of a thing should be inversely proportional to its frequency of use?
  The other potential short name that might work is 'res', but it seems
  ambiguous in some environments (e.g. response for the web).

  * `level`
    * A value specifying the success/failure level
    * By convention, keys that map to:
      * _negative_ values are considered some level of failure
      * while _non-negative_ values are considered informational or successful
  * `message`
    * A message describing the result (usually a string)
  * `rest`
    * Additional key/value pairs to merge into the result map"
  ([level]
   (r level ""))
  ([level message]
   {:level level :message message})
  ([level message & {:as rest}]
   (merge (r level message) rest)))

(defmulti success?
  "Determine whether the given object represents a successful outcome.

  `obj` is considered successful in all cases except the following:
  * `nil`
  * `false`
  * An instance of `Throwable`
  * A result map where the value of `:level` is a keyword defined in
  `glu.results/levels` which maps to a negative number"
  class)

(defmethod success? nil nil-type [_]
  false)

(defmethod success? boolean boolean-type [bool]
  bool)

(defmethod success? Throwable throwable-type [_]
  false)

(defmethod success? clojure.lang.PersistentArrayMap map-type [maybe-r]
  (if (s/valid? ::result maybe-r)
    (<= 0 (get levels (:level maybe-r) (:error levels)))
    true))

(defmethod success? :default [_]
  true)

(defn failed?
  "Determine whether the given object represents a failure outcome.

  This is basically the opposite of `success?`."
  [obj]
  (not (success? obj)))

(defn warned?
  "Determine whether the given object represents a warning or failure outcome.

  This is basically the same as `failed?` except also returns true if `obj`
  is a result map where `:level` is `:warn`."
  [obj]
  (or (failed? obj)
      (= :warn (:level obj))))

(defn prepend-msg
  [result msg]
  (assoc result :message (str msg (:message result))))


(s/fdef print-msg
        :args (s/cat :result ::result)
        :ret nil?)
(defn print-msg
  "Prints the message of the given result to stdout or stderr accordingly.
  No printing is done if the message is empty."
  [result]
  (when (not (empty? (or (:message result) "")))
    (if (success? result)
      (println (:message result))
      (binding [*out* *err*]
        (println (:message result))))))



(comment
  (prepend-msg (r :info "original") "prepended - "))
