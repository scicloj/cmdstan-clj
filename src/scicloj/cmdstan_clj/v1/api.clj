(ns scicloj.cmdstan-clj.v1.api
  (:require [scicloj.tempfiles.api :as tempfiles]
            [clojure.java.io :as io]
            [charred.api :as charred]
            [clojure.java.shell :as shell]
            [babashka.process :as process]
            [tech.v3.dataset :as tmd]
            [clojure.string :as str]))

(defn read-line-from-reader [rdr]
  (binding [*in* rdr]
    (read-line)))

(defn verbose-shell [options & cmd-and-args]
  (let [{:keys [out err]} (->> cmd-and-args
                               (apply process/process
                                      (merge options
                                             {:pre-start-fn #(apply println "Running" (:cmd %))})))]
    (->> (with-open [out-rdr (io/reader out)
                     err-rdr (io/reader err)]
           (loop [all-printed-lines []]
             (if-let [lines (->> [out-rdr err-rdr]
                                 (map (fn [rdr]
                                        (when-let [line (read-line-from-reader rdr)]
                                          (println line)
                                          line)))
                                 (remove nil?)
                                 seq)]
               (recur (concat all-printed-lines lines))
               {:out (->> all-printed-lines
                          (str/join "\n"))}))))))

(def STAN_HOME (System/getenv "STAN_HOME"))

(when-not STAN_HOME
  (throw (ex-info "Missing STAN_HOME environment variable."
                  {})))

(when-not (.exists (io/file STAN_HOME))
  (throw (ex-info "Missing STAN_HOME directory."
                  {:STAN_HOME STAN_HOME})))

(def code->stan-path
  (memoize (fn [code]
             (let [path (:path (tempfiles/tempfile! ".stan"))]
               (spit path code)
               path))))

(defn model
  ([code]
   (model code []))
  ([code make-args]
   (let [path (-> code
                  code->stan-path
                  (str/replace #"\.stan$" ""))
         ret (apply verbose-shell
                    {:dir STAN_HOME}
                    "make"
                    (conj make-args path))]
     (assoc ret
            :path path))))

(defn sample
  ([model data]
   (sample model data {}))
  ([model data {:as options
                :keys [num-chains]}]
   (let [data-path (:path (tempfiles/tempfile! ".json"))
         samples-path (:path (tempfiles/tempfile! ".csv"))
         _ (charred/write-json data-path data)
         args (concat (some->> options
                               (map (fn [[k v]]
                                      (format "%s=%s"
                                              (-> k name (str/replace #"-" "_"))
                                              (str v)))))
                      ["data" (str "file=" data-path)
                       "output" (str "file=" samples-path)])
         ret (->> args
                  (filter some?)
                  (apply verbose-shell {} (:path model) "sample"))
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
