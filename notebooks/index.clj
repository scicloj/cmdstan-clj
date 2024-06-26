;; # cmdstan-clj

;; [cmdstan-clj](https://github.com/scicloj/cmdstan-clj) is a Clojure wrapper of [Stan](https://mc-stan.org/) probabilistic programming language that uses the [CmdStan](https://mc-stan.org/users/interfaces/cmdstan) CLI.

;; **Source:** [![(GitHub repo)](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/scicloj/cmdstan-clj)
;;
;; **Artifact:** [![(Clojars coordinates)](https://img.shields.io/clojars/v/org.scicloj/cmdstan-clj.svg)](https://clojars.org/org.scicloj/cmdsta-nclj)
;;
;; **Status:** an evolving proof-of-concept.
;;

(ns index
  (:require [tablecloth.api :as tc]
            [clojure.java.shell :as shell]
            [charred.api :as charred]
            [clojure.java.io :as io]
            [tech.v3.dataset.print :as print]
            [clojure.string :as str]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [scicloj.noj.v1.vis.stats :as vis.stats ]
            [aerial.hanami.templates :as ht]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.noj.v1.stats :as stats]
            [scicloj.cmdstan-clj.v1.api :as stan]))


;; ## Walkthrough

;; Let us define our model.
;;
;; In our probabilistic model here,
;; we have we have an observed vector $y$
;; of $N$ samples.
;; We have an unobserved parameter $\theta \sim Beta(1,1)$,
;; and the elements of $y$ are conditionally independent
;; given $\theta$, and distributed $Bernoulli(\theta)$ each.

(def model-code
  "
data {
      int<lower=0> N;
      array[N] int<lower=0,upper=1> y;
      }
parameters {
            real<lower=0,upper=1> theta;
            }
model {
       theta ~ beta(1,2);  // uniform prior on interval 0,1
       y ~ bernoulli(theta);
}")


;; Now we may compile the model,
;; if this has not been done yet.
(def model
  (stan/model model-code))

;; Here is the output of compiling our model:

(-> model
    :out
    kind/code)

;; Here are some toy data:

(def data
  {:N 10
   :y [0 1 0 0 0 0 0 0 0 1]})

;; Let us sample from the posterior of $\theta$
;; given out observed $y$ in the data.
;; (Soon we will support relevant options
;; to control the sampling process.)

(def sampling
  (stan/sample model data {:num-chains 4}))

;; Here is the output of sampling process.

(-> sampling
    :out
    kind/code)

;; Here are the sampels:

(-> sampling
    :samples)

;; The histogram of $\theta$:

(-> sampling
    :samples
    (tc/group-by [:chain] {:result-type :as-map})
    (update-vals
     (fn [chain-samples]
       (-> chain-samples
           (vis.stats/histogram :theta {:nbins 100})))))

;; The trace plot of $\theta$:

(-> sampling
    :samples
    (hanami/plot ht/line-chart {:X :i
                                :Y :theta
                                :COLOR "chain"
                                :OPACITY 0.5}))
