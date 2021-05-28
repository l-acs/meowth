(ns meowth.gather
  (:gen-class)
  (:refer meowth.rest)
  (:use
   [meowth.config :only [*config*]]))

(defn channel-info [channel-name]
  (response-body
   (rocket-get "channels.info" "roomName" channel-name)))

(defn rid-from-channel-name [name] ;; near duplicate: room-id
  ;; this works for channels the user is not in
  ;; but naturally not for private groups the user is not in
  (->> name channel-info :channel :_id))



;; potentially misleading with a `with-config` call because it relies on `user-rooms`, not an actual API call:
;; to simulate using a different config, just pass an appropriate `user-rooms`, as expected
(defn room-id [user-rooms room-name] ;; near duplicate: rid-from-channel-name
  ;; naturally this fails if the user is not in the room
  ;; but it works marvelously for rooms the user is in, incl. dms
  ;; this _does_ work for channels the user _is_ in, though
  (->> user-rooms
       (filter
        #(= (:name %) room-name))
       first
       :rid))

(defn data-from-username [username]
  (->> username (rocket-get "users.info" "username") response-body :user))

(defn id-from-username [username]
  (->> username (data-from-username) :_id))

(defn _some [method amt offset] ;; there's almost certainly a better way to solve this problem
  (response-body (rocket-get method 'count amt 'offset offset))) ;; todo change symbols to enums?

(defn _all [method field]
  (defn _help [acc amt offset]
    (let [response (_some method amt offset)
          total (:total response)
          acc (concat (field response) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

(defn all-users []
  (_all "users.list" :users))

(defn all-channels []
  (_all "channels.list" :channels))

(defn user-rooms [id]
  (-> (rocket-get "users.info" "userId" id "fields" "{\"userRooms\": 1}")
      response-body
      :user
      :rooms))
;; todo: memoize

(defn roles []
  (:roles (response-body
           (rocket-get "roles.list"))))

(defn users-in-role [role]
  (response-body
   (rocket-get "roles.getUsersInRole" "role" role)))

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
