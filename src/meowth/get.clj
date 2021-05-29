(ns meowth.get
  (:gen-class)
  (:refer meowth.rest)
  (:use
   [meowth.config :only [*config*]]))

(defn info [domain thing]
  (case domain
    :users (->> thing
                (rocket-get :users "info" "username") response-body :user)
    :groups (->> thing
                 (rocket-get :groups "info" "roomName") response-body :group)
    :channels (->> thing
                   (rocket-get :channels "info" "roomName") response-body :channel)
    nil))

(defn id [domain thing]
  (:_id (info domain thing)))

(defn _room-id [user-rooms room-name]
  ;; Potentially misleading with a `with-config` call because it
  ;; relies on `user-rooms`, not an actual API call. To simulate using
  ;; a different config, just pass an appropriate `user-rooms` as
  ;; otherwise expected.

  ;; Naturally this function fails if the user is not in the room. But
  ;; it works marvelously for any channels, groups, and other
  ;; rooms (i.e. DMs) the user is in.

  (->> user-rooms
       (filter
        #(= (:name %) room-name))
       first
       :rid))

(defn _some [ns method amt offset] ;; there's almost certainly a better way to solve this problem
  (response-body (rocket-get ns method 'count amt 'offset offset))) ;; todo change symbols to enums?

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
  (-> (rocket-get :users "info" "userId" id "fields" "{\"userRooms\": 1}")
      response-body
      :user
      :rooms))
;; todo: memoize

(defn roles []
  (:roles (response-body
           (rocket-get :roles "list"))))

(defn users-in-role [role]
  (response-body
   (rocket-get :roles "getUsersInRole" "role" role)))

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

 ;; this 'might should' go elsewhere
(defn dm-info-fast [rooms username]
  (first
   (filter #(= (:name %) username) rooms)))
