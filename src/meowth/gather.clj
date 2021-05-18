(ns meowth.gather
  (:gen-class)
  (:refer meowth.rest))

(defn get-some [cfg method amt offset]
  (response-body (rocket-get cfg method 'count amt 'offset offset)))

(defn get-all [cfg method field]
  (defn _help [acc amt offset]
    (let [response (get-some cfg method amt offset)
          total (:total response)
          acc (concat (field response) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

(defn get-all-users [cfg]
  (get-all cfg "users.list" :users))

(defn get-all-channels [cfg]
  (get-all cfg "channels.list" :channels))

(defn get-rid-from-channel-name [cfg name]
  (->> name (rocket-get cfg "channels.info" "roomName") response-body :channel :_id))

(defn get-user-rooms [cfg id]
  (-> cfg
      (rocket-get "users.info" "userId" id "fields" "{\"userRooms\": 1}")
      response-body
      :user
      :rooms))
;; todo: memoize

(defn get-user-dms [cfg id]
  (filter
   #(= (:t %) "d") (get-user-rooms cfg id)))
