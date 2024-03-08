;; # cmdstan-clj

(ns index
  (:require [tablecloth.api :as tc]
            [clojure.java.shell :as shell]
            [charred.api :as charred]
            [clojure.java.io :as io]
            [tech.v3.dataset.print :as print]
            [clojure.string :as str]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [aerial.hanami.templates :as ht]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.cmdstan-clj.v1.api :as stan]))


;; ## Walkthrough

(def model (-> "notebooks/bernoulli.stan"
               slurp
               stan/model))

(def data
  {:N 10
   :y [0 1 0 0 0 0 0 0 0 1]})

(def sampling
  (stan/sample model data))

(-> sampling
    :out
    kind/code)

(-> sampling
    :samples)

(-> sampling
    :samples
    (hanami/histogram :theta {:nbins 100}))

(-> sampling
    :samples
    (hanami/plot ht/line-chart {:X :i
                                :Y :theta}))
