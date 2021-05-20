(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-get rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as gather]
   [meowth.user :as user]))

(defn get-channel-info [channel-name]
  (response-body
   (rocket-get "channels.info" "roomName" channel-name)))

(defn add-all [channel-name]
  (->> channel-name
       (gather/get-rid-from-channel-name)
       (rocket-post "channels.addAll" "roomId")
       response-body))

(defn _remove-leader [userid rid]
  (rocket-post "channels.removeLeader"
               "userId" userid
               "roomId" rid))

(defn _add-leader [userid rid]
  (rocket-post "channels.addLeader"
               "userId" userid
               "roomId" rid))

(defn remove-leader [channel-name username]
  (_remove-leader
                  (user/get-id-from-username username)
                  (gather/get-rid-from-channel-name channel-name)))

(defn add-leader [channel-name username]
  (_add-leader
               (user/get-id-from-username username)
               (gather/get-rid-from-channel-name channel-name)))
