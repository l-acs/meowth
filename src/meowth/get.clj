(ns meowth.get
  (:gen-class)
  (:require
   [meowth.rest :refer :all]
   [meowth.config :refer [*config*]]))

(defn info [domain thing]
  (case domain
    :users (->> thing
                (rocket-get :users "info" :username) response-body :user)
    :groups (->> thing
                 (rocket-get :groups "info" :roomName) response-body :group)
    :channels (->> thing
                   (rocket-get :channels "info" :roomName) response-body :channel)
    nil))

(defn id [domain thing]
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

(defn all-users []
  (_all :users "list" :users))

(defn all-channels []
  (_all :channels "list" :channels))

(defn user-rooms [id]
  (-> (rocket-get :users "info"
                  :userId id
                  :fields "{\"userRooms\": 1}") ;; todo: make this not take json as a string! maybe look into putting chesire.core/encode for all of rocket-get args in its definition?
      response-body
      :user
      :rooms))
;; todo: memoize

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

(defn user-dms [id]
  (filter
   #(= (:t %) "d") (user-rooms id)))

(defn dm-info [username]
  (first
   (filter #(= (:name %) username)
           (user-dms (:id *config*)))))
