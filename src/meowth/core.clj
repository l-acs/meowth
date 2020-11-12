(ns meowth.core
  (:gen-class)
  (:refer meowth.rest)
  (:require
   [clojure.edn :as edn]
   [comb.template :as template]
   [meowth.gather :as gather]
   [meowth.message :as message]
   [meowth.user :as user]))

(defn make-blurb [cfg fields]
  (template/eval (:blurb cfg) fields))

(defn parse-conf [cfg-file]
  (-> cfg-file slurp edn/read-string))

(defn get-rid-from-channel-name [cfg name]
  (->> name (rocket-get cfg "channels.info" "roomName") response-body :channel :_id))

(defn add-channel-group-ids-to-cfg
  "Given a config with a :channel-groups field corresponding to a hashmap of groups of channels, add a new field, :channel-groups-ids, where each channel name has been mapped to a channel id."
  [cfg]
  (assoc cfg :channel-groups-ids
         (into {}
               (map (fn [[k v]]
                      [k (map #(get-rid-from-channel-name cfg %) v)])
                    (:channel-groups cfg)))))

(defn add-bot-dms-to-cfg
  "Given a config, return a new map with an additional field for a list of the DM rooms the user is in"
  [cfg]
  (assoc cfg
         :dms (gather/get-user-dms cfg (:id cfg))))

(defn send-blurbs-to-users [cfg userfieldslist]
  (run! #(future (message/send-blurb-to-user cfg (:username %) (make-blurb cfg %))) userfieldslist))

(defn decide-users
  "Given a config, come up with the list of users to be messaged"
  [cfg]
  (case (:message-condition cfg)
    :new nil ; FIXME
    :all (map #(user/gen-fields cfg %) (gather/get-all-users cfg))
    :unmessaged (remove :messaged? (map #(user/gen-fields cfg %) (gather/get-all-users cfg)))))

(def conf (-> "conf.edn" parse-conf add-bot-dms-to-cfg add-bot-dms-to-cfg))

(defn -main
  "Based on the config, do the thing!"
  [& args]
  (println "doing the thing...")
  ;; (decide-users conf)
  (println "thing done."))
           


(comment

  (def allusers (gather/get-all-users conf))
  (def allchannels (gather/get-all-channels conf))
  (def alluserinfo (map #(user/gen-fields conf %) allusers))


)
