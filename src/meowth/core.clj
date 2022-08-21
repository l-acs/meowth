(ns meowth.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [comb.template :as template]
   [meowth.cache :as cache]
   [meowth.channel :as channel]
   [meowth.config :refer [*config* with-config]]
   [meowth.get :as get]
   [meowth.message :as message]
   [meowth.rest :refer :all]
   [meowth.role :as role]
   [meowth.user :as user]))

(defn make-blurb [cfg user] ;; `user` from user/gen-fields
  (template/eval (:blurb cfg) user))

(defn send-blurbs-to-users [cfg userfieldslist]
  (run!
   #(future
      (message/send-blurb
       (str "@" (:username %))
       (make-blurb cfg %)))
   userfieldslist))

(defn decide-users
  "Given a config, come up with the list of users to be messaged"
  [cfg users]
  (case (:message-condition cfg)
    :new nil ; FIXME
    :all (map #(user/gen-fields cfg %) (get/users cfg))
    :unmessaged (remove #(-> % val :messaged?) users)))

(defn -main
  "Based on the config, do the thing!"
  [& args]
  (println "doing the thing...")
  ;; (decide-users conf)
  (println "thing done."))
