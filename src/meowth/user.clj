(ns meowth.user
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.set :as set]))

(defn messaged? [cfg dms id]
  (some? (seq (filter #(re-matches (re-pattern (str ".*" id ".*")) %) dms))))

(defn email [user]
  (-> user :emails first :address))

(defn self-set-name [user]
  (:name user))

(defn _first-name [user]
  (first
   (str/split
    (if-some [name (self-set-name user)]
      (first (str/split name #"\s"))
      (email user))
    #"\.")))

(defn capitalize-first-letter [str]
  (let [split (str/split str #"")]
    (str/join (cons (str/upper-case (first split)) (rest split)))))

(defn first-name [user]
  (capitalize-first-letter (_first-name user)))

(defn rooms [user]
  (:__rooms user))

(defn joined-channels-list
  "From a user and a list of channels, which of those channels is that user in?"
  [user clist]
  (set/intersection
   (into #{} clist)
   (into #{} (rooms user))))

(defn channel-groups-hashmap
  "Given a user and the config, return a hashmap containing, for each channel group, a key/value pair of that group name and the subset of those channels of which the user is a member"
  [cfg user]
  (into {}
        (map
         (fn [[grpname channelids]]
           [grpname (joined-channels-list user channelids)])
         (cfg :channel-groups-ids))))

(defn gen-fields
  "Template-able info for a user"
  [cfg user]
  {
   :first-name (first-name user)
   :email (email user)
   :rooms (rooms user)
   :username (:username user)
   :bio (:statusText user)
   :self-set-name (self-set-name user)
   :timezone (:utcOffset user)
   :connection (:statusConnection user)
   :channel-groups (channel-groups-hashmap cfg user)
   :messaged? (messaged? cfg (:dms cfg) (:_id user))
   :__all user ; still include _all_ info in the `user` hashmap, in case
   })
