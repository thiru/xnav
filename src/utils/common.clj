(ns utils.common
  "Common/generic utilities."
  (:require [clojure.java.shell :as sh]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [puget.printer :as puget]
            [utils.results :as r]))



(defmacro spy
  "A simpler version of Timbre's spy which simply pretty-prints to stdout
  and returns the eval'd expression."
  [expr]
  `(let [evaled# ~expr]
     (print (str '~expr " => "))
     (puget/cprint evaled#)
     evaled#))



(s/fdef non-neg?
        :args (s/cat :n number?)
        :ret boolean?)

(defn non-neg?
  "Determine whether the given number is non-negative."
  [n]
  (<= 0 n))



(s/fdef exit!
        :args (s/cat :result ::r/result)
        :ret nil?)

(defn exit!
  "Exit app with a success/failure exit code based on the given result.
  The result's message is also printed to stdout or stderr as appropriate."
  [result]
  (if (r/success? result)
    (println (:message result))
    (binding [*out* *err*]
      (println (:message result))))
  (System/exit (case (:level result)
                 (:success :trace :info :warn :debug) 0
                 1)))



(s/fdef fmt
        :args (s/cat :formatter string?
                     :args (s/coll-of string?))
        :ret string?)

(defn fmt
  "A convenience function to create a formatted string (via `format`).

  The first argument is the format control string. If it's a list it will be
  concatenated together. This makes it easier to format long strings."
  [formatter & args]
  (if (sequential? formatter)
    (apply format (str/join "" formatter) args)
    (apply format formatter args)))



(s/fdef slurp-file
        :args (s/cat :file-path string?)
        :ret (s/or :string string?
                   :result ::r/result))

(defn slurp-file
  "Read all contents of the given file.

  Returns the contents of the string if successfully read, otherwise a
  `r/result`."
  [file-path]
  (if (str/blank? file-path)
    (r/r :error "No file was provided")
    (let [file (io/file file-path)]
      (if (not (.exists file))
        (r/r :error
             (format "File '%s' was not found or inaccessible" file-path))
        (slurp file)))))



(s/fdef parse-int
        :args (s/cat :input string?
                     :fallback int?)
        :ret int?)

(defn parse-int
  "Exception-free integer parsing.

   Returns the parsed integer if successful, otherwise fallback."
  [input & {:keys [fallback]
            :or {fallback 0}}]
  (try
   (Integer/parseInt input)
   (catch Exception _
     fallback)))



(s/fdef parse-float
        :args (s/cat :input string?
                     :fallback float?)
        :ret float?)

(defn parse-float
  "Exception-free float parsing.

   Returns the parsed float if successful, otherwise fallback."
  [input & {:keys [fallback]
            :or {fallback 0.0}}]
  (try
   (Float/parseFloat input)
   (catch Exception _
     fallback)))



(s/fdef sh-r
        :args (s/cat :args (s/coll-of string?))
        :ret ::r/result)

(defn sh-r
  "Run `sh/sh` and wrap in a result."
  [& args]
  (let [cmd-res (apply sh/sh args)]
    (merge cmd-res
           (if (zero? (:exit cmd-res))
             (r/r :success (:out cmd-res))
             (r/r :error (:err cmd-res))))))

