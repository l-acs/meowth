(ns meowth.config
  (:gen-class)
  (:require
   [clojure.edn :as edn]))

(defn parse-conf [cfg-file]
  (-> cfg-file slurp edn/read-string))

(def ^:dynamic *config* (parse-conf "ctf.edn"))

;; to change:
;; (binding [*config* (parse-conf "conf.edn")]
;;   *config*)


;; maybe: (def rooms (gather/get-user-rooms (:id *config*)))
