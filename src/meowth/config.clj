(ns meowth.config
  (:gen-class)
  (:require
   [clojure.edn :as edn]))

(defn parse-config [config-file]
  (-> config-file slurp edn/read-string))

(def ^:dynamic *config* (parse-config "config.edn"))

;; to change this, e.g.:
;; (binding [*config* (parse-conf "another-config.edn")]
;;   *config*)

(defmacro with-config [config call]
  `(binding [*config* ~config]
        ~call))

;; maybe: (def rooms (gather/get-user-rooms (:id *config*)))
