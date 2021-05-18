(ns meowth.room
  (:gen-class)
  (:use [meowth.rest :only [rocket-get rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as gather]
   [meowth.user :as user]))

(defn get-room-id [cfg user-rooms channel-name] ;; this already exists elsewhere
  (->> user-rooms
       (filter
        #(= (:name %) channel-name))
       first
       :rid))

(defn _invite-user-to-group [cfg userid rid]
  (rocket-post cfg "groups.invite"
               "userId" userid
               "roomId" rid))

(defn invite-user-to-group [cfg user-rooms username group]
  (_invite-user-to-group cfg
               (user/get-id-from-username cfg username)
               (get-room-id cfg user-rooms group)))


(defn _remove-leader [cfg userid rid]
  (rocket-post cfg "groups.removeLeader"
               "userId" userid
               "roomId" rid))

(defn _add-leader [cfg userid rid]
  (rocket-post cfg "groups.addLeader"
               "userId" userid
               "roomId" rid))

(defn remove-leader [cfg user-rooms channel-name username]
  (_remove-leader cfg
               (user/get-id-from-username cfg username)
               (get-room-id cfg user-rooms channel-name)))

(defn add-leader [cfg user-rooms channel-name username]
  (_add-leader cfg
               (user/get-id-from-username cfg username)
               (get-room-id cfg user-rooms channel-name)))
