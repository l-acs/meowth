(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.get :as get]))

;; encourage abstraction and reuse by only using full-fledged
;; user (constructed with gen-fields) -- not ids or usernames on
;; their own
;; todo: maybe do similarly using full-fledged templatable room info

(defn add-all [channel-name]
  (->> channel-name
       (get/id :channels channel-name)
       (rocket-post :channels "addAll" :roomId)
       response-body))

(defn add-leader [domain room-name user]
  (rocket-post domain "addLeader"
               :userId (:_id user) ;; user is a full-fledged user.clj gen-fields
               :roomId (get/id domain room-name)))

(defn remove-leader [domain room-name user]
  (rocket-post domain "removeLeader"
               :userId (:_id user)
               :roomId (get/id domain room-name)))

(defn add-owner [domain room-name user]
  (rocket-post domain "addOwner"
               :userId (:_id user)
               :roomId (get/id domain room-name)))

(defn remove-owner [domain room-name user]
  (rocket-post domain "removeOwner"
               :userId (:_id user)
               :roomId (get/id domain room-name)))

(defn invite-user-to-room [domain room-name user]
  (rocket-post domain "invite"
               :userId (:_id user)
               :roomId (get/id domain room-name)))
