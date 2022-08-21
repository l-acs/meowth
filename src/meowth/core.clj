(ns meowth.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [comb.template :as template]
   [meowth.channel :as channel]
   [meowth.config :refer [*config* with-config]]
   [meowth.get :as get]
   [meowth.message :as message]
   [meowth.rest :refer :all]
   [meowth.role :as role]
   [meowth.user :as user]))

(defn make-blurb [cfg fields] ;; 'fields' meaning a user from user/gen-fields
  (template/eval (:blurb cfg) fields))

(defn send-blurbs-to-users [cfg userfieldslist]
  (run!
   #(future
      (message/send-blurb
       (str "@" (:username %))
       (make-blurb cfg %)))
   userfieldslist))

(defn decide-users
  "Given a config, come up with the list of users to be messaged"
  [cfg]
  (case (:message-condition cfg)
    :new nil ; FIXME
    :all (map #(user/gen-fields cfg %) (get/users cfg))
    :unmessaged (remove :messaged? (map #(user/gen-fields cfg %) (get/users cfg)))))

(defn -main
  "Based on the config, do the thing!"
  [& args]
  (println "doing the thing...")
  ;; (decide-users conf)
  (println "thing done."))
           

(comment
  (def allusers (get/users))
  (def allchannels (get/channels))
  (def alluserinfo (map #(user/gen-fields %) allusers))
)
