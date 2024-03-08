(ns scicloj.cmdstan-clj.v1.api
  (:require [scicloj.tempfiles.api :as tempfiles]
            [clojure.java.io :as io]
            [charred.api :as charred]
            [clojure.java.shell :as shell]
            [tech.v3.dataset :as tmd]
            [clojure.string :as str]))

(def stan-home (System/getenv "STAN_HOME"))

(defn model [code]
  (let [path-stan (:path (tempfiles/tempfile! ".stan"))
        path (str/replace path-stan #"\.stan$" "")
        _ (spit path-stan code)
        ret (shell/sh "make" path
                      :dir stan-home)]
    (assoc ret
           :path path)))

(defn sample [model data]
  (let [data-path (:path (tempfiles/tempfile! ".json"))
        samples-path (:path (tempfiles/tempfile! ".csv"))
        _ (charred/write-json data-path data)
        ret (shell/sh (:path model) "sample"
                      "data" (str "file=" data-path)
                      "output" (str "file=" samples-path))
        samples (-> samples-path
                    (tmd/->dataset {:key-fn keyword}))]
    (assoc ret
           :samples (-> samples
                        (tmd/set-dataset-name "model samples")
                        (tmd/add-or-update-column :i
                                                  (-> samples tmd/row-count range))))))
