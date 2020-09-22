(ns meowth.core
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [cheshire.core :as json]
   [clojure.set :as set]
   [comb.template :as template]))

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

(defn response-body
  "Gets the :body of a response as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))

(defn get-room-info [cfg rid]
  (rocket-get cfg "rooms.info" "roomId" rid))

(defn get-rid-from-channel-name [cfg name]
  (->> name (rocket-get cfg "channels.info" "roomName") response-body :channel :_id))

(defn get-data-from-username [cfg username]
  (->> username (rocket-get cfg "users.info" "username") response-body :user))

(defn get-id-from-username [cfg username]
  (->> username (get-data-from-username cfg) :_id))

(defn post-message [cfg room msg]
  (client/post
   (rocket-gen-url cfg "chat.postMessage")
   (assoc (headers cfg) :form-params {:channel room :text msg :content-type :json})))

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

(defn make-dm-rid-list [id userlist]
  (map #(str (:_id %) id) userlist))

(defn get-user-rooms [cfg id]
  (map :rid (:rooms (:user (response-body (rocket-get cfg "users.info" "userId" id "fields" "{\"userRooms\": 1}"))))))

(defn get-user-dms [cfg id]
 (remove #(not= (count %) 34) (get-user-rooms cfg id)))

(defn get-unmessaged-dm-rids [cfg id userlist]
 (set/difference
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

(defn user-joined-channels-list
  "From a user and a list of channels, which of those channels is that user in?"
  [user clist]
  (set/intersection
   (into #{} clist)
   (into #{} (user-rooms user))))

(defn user-channel-groups-hashmap
  "Given a user and the config, return a hashmap containing, for each channel group, a key/value pair of that group name and the subset of those channels of which the user is a member"
  [cfg user]
  (into {}
        (map
         (fn [[grpname channelids]]
           [grpname (user-joined-channels-list user channelids)])
         (cfg :channel-groups-ids))))

(defn userfields
  "Template-able info for a user"
  [cfg user]
  {
   :first-name (user-first-name user)
   :email (user-email user)
   :rooms (user-rooms user)
   :username (:username user)
   :bio (:statusText user)
   :selfsetname (user-name user)
   :timezone (:utcOffset user)
   :connection (:statusConnection user)
   :channel-groups (user-channel-groups-hashmap cfg user)
   :__all user ; still include _all_ info in the `user` hashmap, in case
   })

(defn make-blurb [cfg fields]
  (template/eval (:blurb cfg) fields))

(defn add-channel-group-ids-to-cfg
  "Given a config with a :channel-groups field corresponding to a hashmap of groups of channels, add a new field, :channel-groups-ids, where each channel name has been mapped to a channel id."
  [cfg]
  (assoc cfg :channel-groups-ids
         (into {}
               (map (fn [[k v]]
                      [k (map #(get-rid-from-channel-name cfg %) v)])
                    (:channel-groups cfg)))))

(defn calculate-message-type-time
  "Type message at appropriate WPM delay"
  [msg wpm]
  (if (or (= 0 wpm) (nil? wpm))
    0
    (let [len (count (str/split msg #""))
          words (/ len 10) ; according to Wikipedia, WPM is calculated by counting a 'word' as a sequence of any 5 characters. This felt crazy slow, so I upped it.
          minutes (/ words wpm)
          milliseconds (* minutes 60000)]
      (long milliseconds))))

(defn delay-send-message [cfg username msg]
  (do
    (Thread/sleep (calculate-message-type-time msg (:wpm cfg)))
    (post-message cfg (str "@" username) msg)))

(defn send-blurb-to-user [cfg username msg]
  (if (:send-paragraphs-as-separate-messages cfg)
    (run! #(delay-send-message cfg username %) (str/split msg #"\n\n"))
    (post-message cfg (str "@" username) msg)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(comment

  (def conf (add-channel-group-ids-to-cfg (parse-conf "conf.edn")))
  (def allusers (get-all-users conf))
  (def allchannels (get-all-channels conf))
  (def alluserinfo (map #(userfields conf %) allusers))

  (make-blurb conf (first alluserinfo))

  (defn all-rooms [userlist]
    (->>  userlist (map :__rooms) (apply concat) distinct))

)
