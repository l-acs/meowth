(ns meowth.get
  (:gen-class)
  (:require
   [meowth.rest :refer :all]
   [meowth.config :refer [*config*]]))

(defn info [domain thing]
  (case domain
    (:user :users)
      (->> thing
           (rocket-get :users "info" :fields "{\"userRooms\": 1}" :username)
           response-body
           :user)
    (:group :groups)
      (->> thing
           (rocket-get :groups "info" :roomName)
           response-body
           :group)
    (:channel :channels)
      (->> thing
           (rocket-get :channels "info" :roomName)
           response-body
           :channel)
    nil))

(defn id [domain thing]
  ;; e.g. (get/id :channels "wg-collab")
  (:_id (info domain thing)))

(defn _some [ns method amt offset] ;; there's almost certainly a better way to solve this problem
  (response-body (rocket-get ns method :count amt :offset offset)))

(defn _all [ns method field]
  (defn _help [acc amt offset]
    (let [response (_some ns method amt offset)
          total (:total response)
          acc (concat (field response) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

;; todo: maybe unify `users`, `channels`, `roles` as `all`

(defn users []
  (_all :users "list" :users))

(defn channels []
  (_all :channels "list" :channels))

(defn roles []
  (:roles (response-body
           (rocket-get :roles "list"))))

(defn users-in-role [role]
  (response-body
   (rocket-get :roles "getUsersInRole" :role role)))

(defn matching-roles
  "Get information about any roles whose given trait matches a particular value"
  [k v]
  (filter #(= (k %) v)
        (roles)))

(defn role
  "Get information about the role with a given id."
  [id]
  (first
   (matching-roles :_id id)))
