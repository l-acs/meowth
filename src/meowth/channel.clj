(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.get :as get]))

(defn add-all [channel-name]
  (->> channel-name
       (get/id :channels channel-name)
       (rocket-post :channels "addAll" :roomId)
       response-body))

(defn _remove-leader [domain rid userid]
  (rocket-post domain "removeLeader"
               :userId userid
               :roomId rid))

(defn _add-leader [domain rid userid]
  (rocket-post domain "addLeader"
               :userId userid
               :roomId rid))

(defn remove-leader [domain room username]
  (_remove-leader domain
                  (get/id domain room)
                  (get/id :users username)))

(defn add-leader [domain room username]
  (_add-leader domain
               (get/id domain room)
               (get/id :users username)))

(defn _add-owner [domain rid username]
  (rocket-post domain "addOwner"
               :userId username
               :roomId rid))

(defn _remove-owner [domain rid username]
  (rocket-post domain "removeOwner"
               :userId username
               :roomId rid))

(defn add-owner [domain room username]
  (_add-owner domain
              (get/id domain room)
              (get/id :users username)))

(defn remove-owner [domain room username]
  (_remove-owner domain
                 (get/id domain room)
                 (get/id :users username)))

(defn _invite-user-to-room [domain userid rid]
  (rocket-post domain "invite"
               :userId userid
               :roomId rid))

(defn invite-user-to-room [domain username group]
  (_invite-user-to-room domain
                        (get/id :users username)
                        (get/id domain group)))
