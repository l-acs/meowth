(ns meowth.gather
  (:gen-class)
  (:refer meowth.rest))

(defn get-some [method amt offset]
  (response-body (rocket-get method 'count amt 'offset offset)))

(defn get-all [method field]
  (defn _help [acc amt offset]
    (let [response (get-some method amt offset)
          total (:total response)
          acc (concat (field response) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

(defn get-all-users []
  (get-all "users.list" :users))

(defn get-all-channels []
  (get-all "channels.list" :channels))

(defn get-rid-from-channel-name [name]
  (->> name (rocket-get "channels.info" "roomName") response-body :channel :_id))

(defn get-user-rooms [id]
  (-> (rocket-get "users.info" "userId" id "fields" "{\"userRooms\": 1}")
      response-body
      :user
      :rooms))
;; todo: memoize

(defn get-user-dms [id]
  (filter
   #(= (:t %) "d") (get-user-rooms id)))
