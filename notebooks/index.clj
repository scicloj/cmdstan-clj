;; # cmdstan-clj

(ns index
  (:require [tablecloth.api :as tc]
            [clojure.java.shell :as shell]
            [charred.api :as charred]
            [clojure.java.io :as io]
            [tech.v3.dataset.print :as print]
            [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]))


;; ## Walkthrough

;; Here we reproduce [CmdStanR's intro](https://mc-stan.org/cmdstanr/articles/cmdstanr.html). Eventually, of course, most of the details below should be generalized and transparent.

(def cmdstan-path (System/getenv "CMDSTAN_PATH"))

(def model-path
  (str (System/getProperty "user.dir")
       "/notebooks/bernoulli"))

(shell/sh "make" model-path
          :dir cmdstan-path)

(def data
  {:N 10
   :y [0 1 0 0 0 0 0 0 0 1]})


(def json-path "temp/bernoulli.data.json")

(io/make-parents json-path)

(charred/write-json json-path data)

(def samples-path "temp/bernoulli.samples.csv")

(-> (shell/sh model-path "sample"
              "data" (str "file=" json-path)
              "output" (str "file=" samples-path))
    :out
    kind/code)

(def processed-samples-path "temp/bernoulli.samples.processed.csv")

;; Filter out comment lines.
(with-open [reader (io/reader samples-path)
            writer (io/writer
                    processed-samples-path)]
  (loop [line (.readLine reader)]
    (when line
      (do (when-not
              (.startsWith line "#")
            (.write writer line)
            (.write writer "\n"))
          (recur (.readLine reader))))))


(-> processed-samples-path
    tc/dataset
    (tc/set-dataset-name "model samples"))
