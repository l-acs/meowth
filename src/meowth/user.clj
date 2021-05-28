(ns meowth.user
  (:gen-class)
  (:use [meowth.rest :only [rocket-get response-body]]
        [meowth.config :only [*config*]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]))

;; this is probably mostly all bad

(defn messaged? [dms id] ;; hm, probably make this nicer
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
  [user]
  (into {}
        (map
         (fn [[grpname channelids]]
           [grpname (joined-channels-list user channelids)])
         (:channel-groups-ids *config*))))

(defn gen-fields
  "Template-able info for a user"
  [user]
  {
   :first-name (first-name user)
   :email (email user)
   :rooms (rooms user)
   :username (:username user)
   :bio (:statusText user)
   :self-set-name (self-set-name user)
   :timezone (:utcOffset user)
   :connection (:statusConnection user)
   :channel-groups (channel-groups-hashmap user)
   :messaged? (messaged? (:dms *config*) (:_id user))
   :__all user ; still include _all_ info in the `user` hashmap, in case
   })


;; note about cfg: the objective is to make a function that, when
;; called on a namespace, will evaluate all of its functions as a
;; second version which takes an additional `cfg` argument
