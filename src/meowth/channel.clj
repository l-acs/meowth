(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [meowth.get :as get]
   [meowth.cache :as cache]
   [meowth.user :refer [capitalize-first-letter]]))

;; encourage abstraction and reuse by only using full-fledged
;; user (constructed with gen-fields) -- not ids or usernames on
;; their own

(defn exists? [channel-name]
  (some
   #(= (% :name) channel-name)
   @cache/channels))


(defn create [channel-name]
  (when-not (exists? channel-name)
    (response-body
     (rocket-post :channels "create" :name channel-name))))

(defn delete [channel-name]
  ;; this and the above are still prone to throwing an execution
  ;; error, if we delete (or create) a channel multiple times since
  ;; the cache was last updated
  (when (exists? channel-name)
    (response-body
     (rocket-post :channels "delete" :roomName channel-name))))

(defn rename [channel-old-name channel-new-name]
  (let [id (get/id :channels channel-old-name)]
    (response-body
     (rocket-post :channels "rename" :roomId id :name channel-new-name))))

(defn set-default [channel-name default?]
  (let [id (get/id :channels channel-name)]
    (response-body
     (rocket-post :channels "setDefault" :roomId id :default default?))))

(defn set-description [channel-name description]
  (let [id (get/id :channels channel-name)]
    (response-body
     (rocket-post :channels "setDescription" :roomId id :description description))))

(defn set-topic [channel-name topic]
  (let [id (get/id :channels channel-name)]
    (response-body
     (rocket-post :channels "setTopic" :roomId id :topic topic))))

(defn add-all [channel-name]
  (->> channel-name
       (get/id :channels)
       (rocket-post :channels "addAll" :roomId)
       response-body))

(defn invite-user-to-room [domain room-name user]
  (rocket-post domain "invite"
               :userId (:id user)
               :roomId (get/id domain room-name)))

(defn invite-user [channel-name user]
  (invite-user-to-room :channels channel-name user))

(defn invite-user-private [group-name user]
  (invite-user-to-room :groups group-name user))

(defn grant-room-status
  ;; `user` is a full set of user fields (from gen-fields, from
  ;; get/info), e.g.

  ;; {:_id "qFG4...", :name "Luc", :rooms [...], etc. }

  ;; `operation` is :add or :remove
  ;; `status` is :leader or :owner
  ;; `domain` is :channels or :groups
  [domain room-name operation status user]

  (let [operation (name operation)
        status (capitalize-first-letter (name status))
        verb (str operation status)]
    (rocket-post domain verb
                 :userId (:id user)
                 :roomId (get/id domain room-name))))

(defn grant-status [channel-name operation status user]
  (grant-room-status :channels channel-name operation status user))

(defn grant-status-private [group-name operation status user]
  (grant-room-status :groups group-name operation status user))


(comment
  ; see examples.md for a thorough use case
)
