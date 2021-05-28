(ns meowth.gather
  (:gen-class)
  (:refer meowth.rest)
  (:use
   [meowth.config :only [*config*]]))

(defn get-channel-info [channel-name]
  (response-body
   (rocket-get "channels.info" "roomName" channel-name)))

(defn get-rid-from-channel-name [name] ;; near duplicate: get-room-id
  ;; this works for channels the user is not in
  ;; but naturally not for private groups the user is not in
  (->> name get-channel-info :channel :_id))



;; potentially misleading with a `with-config` call because it relies on `user-rooms`, not an actual API call:
;; to simulate using a different config, just pass an appropriate `user-rooms`, as expected
(defn get-room-id [user-rooms room-name] ;; near duplicate: get-rid-from-channel-name
  ;; naturally this fails if the user is not in the room
  ;; but it works marvelously for rooms the user is in, incl. dms
  ;; this _does_ work for channels the user _is_ in, though
  (->> user-rooms
       (filter
        #(= (:name %) room-name))
       first
       :rid))

(defn get-data-from-username [username]
  (->> username (rocket-get "users.info" "username") response-body :user))

(defn get-id-from-username [username]
  (->> username (get-data-from-username) :_id))

(defn _get-some [method amt offset] ;; there's almost certainly a better way to solve this problem
  (response-body (rocket-get method 'count amt 'offset offset))) ;; todo change symbols to enums?

(defn _get-all [method field]
  (defn _help [acc amt offset]
    (let [response (_get-some method amt offset)
          total (:total response)
          acc (concat (field response) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

(defn get-all-users []
  (_get-all "users.list" :users))

(defn get-all-channels []
  (_get-all "channels.list" :channels))

(defn get-user-rooms [id]
  (-> (rocket-get "users.info" "userId" id "fields" "{\"userRooms\": 1}")
      response-body
      :user
      :rooms))
;; todo: memoize

(defn get-user-dms [id]
  (filter
   #(= (:t %) "d") (get-user-rooms id)))

(defn get-dm-info [username]
  (first
   (filter #(= (:name %) username)
           (get-user-dms (:id *config*)))))

 ;; this 'might should' go elsewhere
(defn dm-info-fast [rooms username]
  (first
   (filter #(= (:name %) username) rooms)))
