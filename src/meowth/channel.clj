(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-get rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as gather]
   [meowth.user :as user]))

(defn get-channel-info [cfg channel-name]
  (response-body
   (rocket-get cfg "channels.info" "roomName" channel-name)))

(defn add-all [cfg channel-name]
  (->> channel-name
       (gather/get-rid-from-channel-name cfg)
       (rocket-post cfg "channels.addAll" "roomId")
       response-body))

(defn _remove-leader [cfg userid rid]
  (rocket-post cfg "channels.removeLeader"
               "userId" userid
               "roomId" rid))

(defn _add-leader [cfg userid rid]
  (rocket-post cfg "channels.addLeader"
               "userId" userid
               "roomId" rid))

(defn remove-leader [cfg user-rooms channel-name username]
  (_remove-leader cfg
               (user/get-id-from-username cfg username)
               (gather/get-rid-from-channel-name cfg user-rooms channel-name)))

(defn add-leader [cfg user-rooms channel-name username]
  (_add-leader cfg
               (user/get-id-from-username cfg username)
               (gather/get-rid-from-channel-name cfg user-rooms channel-name)))
