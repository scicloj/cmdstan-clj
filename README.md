
# cmdstan-clj

Accessing the [Stan](https://mc-stan.org/) statistical modeling language from Clojure through [CmdStan](https://mc-stan.org/users/interfaces/cmdstan).

## Status 

Currently, this is an evolving proof-of-concept.

## Usage

This project has not been deployed to Clojars yet. For now, you may clone the repo and play with it.

You will need [CmdStan](https://mc-stan.org/users/interfaces/cmdstan.html) installed in your system, and the environment variable `STAN_HOME` pointing to its location.

Them you can follow the [walkthrough](https://scicloj.github.io/cmdstan-clj).

## Why

Stan is a programming language for Bayesian statistical modelling with wonderful community and ecosystem.

Clojure has a few actively developed probabilistic programming options such as [Inferme](https://github.com/generateme/inferme) and [Gen.clj](https://github.com/probcomp/Gen.clj). Still, at some use casese, Stan is more efficient and certainly has a more complete ecosystem around it. While the pure-Clojure options keep evolving, it would be good to have Stan as well in our toolset. It may also help us in benchmarking and testing the other options.

One way to interoperate with Stan is through the command line CmdStan. It is a common way, used in other languages. Ideally, it should allow choosing a version of CmdStan, as well as compiling it for using the GPU.

[clj-stan](https://github.com/thomasathorne/clj-stan) is great, but only supports an old version of CmdStan.

In the current project, we explore the current version of CmdStan. Possibly, when the details clarify, we may propose merging the new implementation into the existing clj-stan.

## License

Copyright © 2024 Scicloj

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.


 
