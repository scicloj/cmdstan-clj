
# cmdstan-clj

Accessing the [Stan](https://mc-stan.org/) statistical modeling language from Clojure through [CmdStan](https://mc-stan.org/users/interfaces/cmdstan).

Eventually, this will be a library.

## Status 

Currently, this is an evolving proof-of-concept.

## Why

Stan is a programming language for Bayesian statistical modelling with wonderful community and ecosystem.

While Clojure has a few work-in-progress options for Bayesian computing, at the moment none of them is as efficient as Stan in some typical use cases.

One way to interoperate with Stan is through the commind line CmdStan. It is a common way, used in other languages. Ideally, it should allow choosing a version of CmdStan, as well as compiling it for using the GPU.

[clj-stan](https://github.com/thomasathorne/clj-stan) is great, but only supports an old version of CmdStan.


