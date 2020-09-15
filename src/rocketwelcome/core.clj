(ns rocketwelcome.core
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [cheshire.core :as json]))

(defn parse-conf [cfg-file]
  (-> cfg-file slurp edn/read-string))

(defn headers [cfg]
  {:headers
   {"X-Auth-Token" (:token cfg),
    "X-User-Id" (:id cfg),
    "Content-type" "application/json"}})

(defn rocket-gen-url [cfg call & args]
  (str (cfg :url)  "/api/v1/" call
    (when args (->> args conj (partition 2) (map (fn [[x y]] (str x "=" y))) (str/join "&") (str "?")))))

(defn rocket-get [cfg call & args]
  (client/get
   (apply rocket-gen-url cfg call args)
   (headers cfg)))

(defn rocket-post [cfg call & args]
  (client/post
   (rocket-gen-url cfg call)
   (assoc
    (headers cfg)
    :form-params
    {  (first args)
       (->> args rest (apply hash-map))})))

(defn request-body
  "Gets the :body of a request as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))

(defn get-room-info [cfg rid]
  (rocket-get cfg "rooms.info" "roomId" rid))

(defn get-rid-from-channel-name [cfg name]
  (->> name (rocket-get cfg "channels.info" "roomName") request-body :channel :_id))

(defn get-data-from-username [cfg username]
  (->> username (rocket-get cfg "users.info" "username") request-body :user))

(defn get-id-from-username [cfg username]
  (->> username (get-data-from-username cfg) :_id))

(defn get-pm-rid-from-username [cfg username]
  (str (get-id-from-username cfg username) (:id cfg)))

(defn message-rid [cfg rid msg]
  (client/post
   (rocket-gen-url cfg "chat.sendMessage")
   (assoc (headers cfg) :form-params {:message {:rid rid :msg msg}} :content-type :json  )))

(defn message-user [cfg username msg]
  (message-rid cfg (get-pm-rid-from-username cfg username) msg))

(defn get-some [cfg method amt offset]
  (request-body (rocket-get cfg method 'count amt 'offset offset)))

(defn get-all [cfg method field]
  (defn _help [acc amt offset]
    (let [request (get-some cfg method amt offset)
          total (:total request)
          acc (concat (field request) acc)]
      (if (= (count acc) total)
        acc
        (_help acc amt (+ offset amt)))))
  (_help '() 100 0))

(defn get-all-users [cfg]
  (get-all cfg "users.list" :users))

(defn get-all-channels [cfg]
  (get-all cfg "channels.list" :channels))

(defn make-dm-rid-list [id userlist]
  (map #(str (:_id %) id) userlist))

(defn get-user-rooms [cfg id]
  (map :rid (:rooms (:user (request-body (rocket-get cfg "users.info" "userId" id "fields" "{\"userRooms\": 1}"))))))

(defn get-user-dms [cfg id]
 (remove #(not= (count %) 34) (get-user-rooms cfg id)))

(defn get-unmessaged-dm-rids [cfg id userlist]
 (clojure.set/difference
  (into #{} (make-dm-rid-list id userlist))
  (into #{} (get-user-dms cfg id))))

(defn user-email [user]
  (-> user :emails first :address))

(defn user-name [user]
  (:name user))

(defn _first-name [user]
  (first
   (str/split
    (if-some [name (user-name user)]
      (first (str/split name #"\s"))
      (user-email user))
    #"\.")))

(defn capitalize-first-letter [str]
  (let [split (str/split str #"")]
    (str/join (cons (str/upper-case (first split)) (rest split)))))

(defn user-first-name [user]
  (capitalize-first-letter (_first-name user)))

(defn user-rooms [user]
  (:__rooms user))

(defn userfields [user]
  {
   :first-name (user-first-name user)
   :email (user-email user)
   :rooms (user-rooms user)
   :username (:username user)
   :bio (:statusText user)
   :selfsetname (user-name user)
   :timezone (:utcOffset user)
   :connection (:statusConnection user)
   })

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(comment

  (def conf (parse-conf "conf.edn"))
  (def allusers (get-all-users conf))
  (def allchannels (get-all-channels conf))

  (defn all-rooms [userlist]
    (->>  userlist (map :__rooms) (apply concat) distinct))

)
