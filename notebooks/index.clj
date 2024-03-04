;; # cmdstan-clj

(ns index
  (:require [tablecloth.api :as tc]
            [clojure.java.shell :as shell]
            [charred.api :as charred]
            [clojure.java.io :as io]
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

(-> samples-path
    tc/dataset)
