(ns meowth.room
  (:gen-class)
  (:use [meowth.rest :only [rocket-get rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as gather]
   [meowth.user :as user]))

;; potentially misleading with a `with-config` call because it relies on `user-rooms`, not an actual API call
;; to simulate using a different config, just pass an appropriate `user-rooms`, as expected
(defn get-room-id [user-rooms room-name] ;; this already exists elsewhere
  (->> user-rooms
       (filter
        #(= (:name %) room-name))
       first
       :rid))

(defn _invite-user-to-group [userid rid]
  (rocket-post "groups.invite"
               "userId" userid
               "roomId" rid))

(defn invite-user-to-group [user-rooms username group]
  (_invite-user-to-group
               (user/get-id-from-username username)
               (get-room-id user-rooms group)))

(defn _remove-leader [userid rid]
  (rocket-post "groups.removeLeader"
               "userId" userid
               "roomId" rid))

(defn _add-leader [userid rid]
  (rocket-post "groups.addLeader"
               "userId" userid
               "roomId" rid))

(defn remove-leader [user-rooms room-name username]
  (_remove-leader
               (user/get-id-from-username username)
               (get-room-id user-rooms room-name)))

(defn add-leader [user-rooms room-name username]
  (_add-leader
               (user/get-id-from-username username)
               (get-room-id user-rooms room-name)))
