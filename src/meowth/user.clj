(ns meowth.user
  (:gen-class)
  (:use [meowth.rest :only [rocket-get response-body]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [meowth.gather :as g]))

;; this is probably mostly all bad

(defn get-data-from-username [cfg username]
  (->> username (rocket-get cfg "users.info" "username") response-body :user))

(defn get-id-from-username [cfg username]
  (->> username (get-data-from-username cfg) :_id))

(defn get-dm-info [cfg username]
  (first
   (filter #(= (:name %) username)
           (g/get-user-dms cfg (:id cfg)))))

(defn dm-info-fast [cfg rooms username]
  (first
   (filter #(= (:name %) username) rooms)))

(defn messaged? [cfg dms id] ;; hm, probably make this nicer
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


;; note about cfg: the objective is to make a function that, when
;; called on a namespace, will evaluate all of its functions as a
;; second version which takes an additional `cfg` argument
