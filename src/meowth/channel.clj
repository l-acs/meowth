(ns meowth.channel
  (:gen-class)
  (:use [meowth.rest :only [rocket-post response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.get :as get]
   [meowth.user :refer [capitalize-first-letter]]))

;; encourage abstraction and reuse by only using full-fledged
;; user (constructed with gen-fields) -- not ids or usernames on
;; their own
;; todo: maybe do similarly using full-fledged templatable room info

(defn add-all [channel-name]
  (->> channel-name
       (get/id :channels channel-name)
       (rocket-post :channels "addAll" :roomId)
       response-body))

(defn invite-user-to-room [domain room-name user]
  (rocket-post domain "invite"
               :userId (:id user)
               :roomId (get/id domain room-name)))

(defn modify-user-in-room
  ;; user is a full set of user fields (from gen-fields, from
  ;; get/info), e.g.

  ;; {:_id "qFG4...", :name "Luc", :rooms [...], etc. }

  ;; operation is :add or :remove
  ;; status is :leader or :owner

  ;; room-name is name, not id
  ;; domain is :channels or :groups
  [user operation status domain room-name]

  (let [operation (name operation)
        status (capitalize-first-letter (name status))
        verb (str operation status)]
    (rocket-post domain verb
                 :userId (:id user)
                 :roomId (get/id domain room-name))))
