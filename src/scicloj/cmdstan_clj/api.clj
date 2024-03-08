(ns scicloj.cmdstan-clj.api
  (:require [scicloj.tempfiles.api :as tempfiles]
            [clojure.java.io :as io]
            [charred.api :as charred]
            [clojure.java.shell :as shell]
            [tech.v3.dataset :as tmd]))

(def stan-home (System/getenv "STAN_HOME"))

(tempfiles/tempfile! [ ])

(defn stan-model [code]
  (let [path (tempfiles/tempfile! "")]
    (shell/sh "make" path
              :dir stan-home)
    {:path path}))

(defn sample [model data]
  (let [data-path (tempfiles/tempfile! "json")
        samples-path (tempfiles/tempfile! "csv")
        _ (charred/write-json data-path data)
        ret (shell/sh (:path model) "sample"
                      "data" (str "file=" data-path)
                      "output" (str "file=" samples-path))]
    (assoc ret
           :samples (-> samples-path
                        (tmd/->dataset {:key-fn keyword})
                        (tmd/set-dataset-name "model samples")
                        (tmd/add-column :i (fn [ds]
                                             (-> ds tmd/row-count range)))))))
