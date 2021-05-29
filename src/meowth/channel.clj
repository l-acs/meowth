(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.get :as get]))

(defn add-all [channel-name]
  (->> channel-name
       (get/rid-from-channel-name)
       (rocket-post :channels "addAll" "roomId")
       response-body))

(defn _channel-remove-leader [rid userid]
  (rocket-post :channels "removeLeader"
               "userId" userid
               "roomId" rid))

(defn _channel-add-leader [rid userid]
  (rocket-post :channels "addLeader"
               "userId" userid
               "roomId" rid))

(defn channel-remove-leader [channel-name username]
  (_channel-remove-leader
   (get/rid-from-channel-name channel-name)
   (get/id-from-username username)))


(defn channel-add-leader [channel-name username]
  (_channel-add-leader
   (get/rid-from-channel-name channel-name)
   (get/id-from-username username)))

(defn _channel-add-owner [rid username]
  (rocket-post :channels "addOwner"
               "userId" username
               "roomId" rid))

(defn _channel-remove-owner [rid username]
  (rocket-post :channels "removeOwner"
               "userId" username
               "roomId" rid))

(defn channel-add-owner [channel-name username]
  (_channel-add-owner
               (get/rid-from-channel-name channel-name)
               (get/id-from-username username)))

(defn channel-remove-owner [channel-name username]
  (_channel-remove-owner
               (get/rid-from-channel-name channel-name)
               (get/id-from-username username)))

(defn _invite-user-to-group [userid rid]
  (rocket-post :groups "invite"
               "userId" userid
               "roomId" rid))

(defn invite-user-to-group [user-rooms username group]
  (_invite-user-to-group
               (get/id-from-username username)
               (get/room-id user-rooms group)))

(defn _group-remove-leader [rid userid]
  (rocket-post :groups "removeLeader"
               "userId" userid
               "roomId" rid))

(defn _group-add-leader [rid userid]
  (rocket-post :groups "addLeader"
               "userId" userid
               "roomId" rid))

(defn group-remove-leader [user-rooms room-name username]
  (_group-remove-leader
               (get/room-id user-rooms room-name)
               (get/id-from-username username)))


(defn group-add-leader [user-rooms room-name username]
  (_group-add-leader
               (get/room-id user-rooms room-name)
               (get/id-from-username username)))
