(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-get rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as gather]))

(defn get-channel-info [cfg channel-name]
  (response-body
   (rocket-get cfg "channels.info" "roomName" channel-name)))

(defn add-all [cfg channel-name]
  (->> channel-name
       (gather/get-rid-from-channel-name cfg)
       (rocket-post cfg "channels.addAll" "roomId")
       response-body))
