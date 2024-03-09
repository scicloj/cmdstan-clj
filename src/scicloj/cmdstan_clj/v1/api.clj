(ns scicloj.cmdstan-clj.v1.api
  (:require [scicloj.tempfiles.api :as tempfiles]
            [clojure.java.io :as io]
            [charred.api :as charred]
            [clojure.java.shell :as shell]
            [tech.v3.dataset :as tmd]
            [clojure.string :as str]))

(def stan-home (System/getenv "STAN_HOME"))

(def code->stan-path
  (memoize (fn [code]
             (let [path (:path (tempfiles/tempfile! ".stan"))]
               (spit path code)
               path))))

(defn model [code]
  (let [path (-> code
                 code->stan-path
                 (str/replace #"\.stan$" ""))
        ret (shell/sh "make" path
                      :dir stan-home)]
    (assoc ret
           :path path)))

(defn sample
  ([model data]
   (sample model data {}))
  ([model data {:keys [num-chains]}]
   (let [data-path (:path (tempfiles/tempfile! ".json"))
         samples-path (:path (tempfiles/tempfile! ".csv"))
         _ (charred/write-json data-path data)
         args [(when num-chains
                 (str "num_chains=" num-chains))
               "data" (str "file=" data-path)
               "output" (str "file=" samples-path)]
         ret (->> args
                  (filter some?)
                  (apply shell/sh (:path model) "sample"))
         read-csv (fn [path]
                    (-> path
                        (tmd/->dataset {:key-fn keyword})
                        (as-> ds
                            (tmd/add-or-update-column
                             ds
                             :i
                             (-> ds tmd/row-count range)))))
         samples (if num-chains
                   ;; a csv file per chain
                   (->> num-chains
                        range
                        (map (fn [chain]
                               (-> samples-path
                                   (str/replace #"\.csv$"
                                                (str "_" (inc chain) ".csv"))
                                   read-csv
                                   (tmd/add-or-update-column :chain chain))))
                        (apply tmd/concat))
                   ;; else - one csv file
                   (-> samples-path
                       read-csv))]
     (assoc ret
            :samples (-> samples
                         (tmd/set-dataset-name "model samples"))))))
