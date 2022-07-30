(ns meowth.user
  (:gen-class)
  (:use [meowth.config :only [*config*]])
  (:require
   [clojure.string :as str]
   [clojure.set :as set]))

;; a note on parameter naming:
;; `username` is just the username
;; `user-info` is exactly what the API returns for a GET to /users/info
;; `user` an abstraction, the result of a `gen-fields` on a `user-info`

(defn email [user-info]
  (-> user-info :emails first :address))

(defn _first-name [user-info]
  (when-some [wholename
              (if-some [self-set-name (:name user-info)]
                (first (str/split self-set-name #"\s"))
                (email user-info))]
    (first (str/split wholename #"\."))))

(defn capitalize-first-letter [str]
  (let [split (str/split str #"")]
    (str/join (cons (str/upper-case (first split)) (rest split)))))

(defn first-name [user-info]
  (when-some [fname (_first-name user-info)]
    (capitalize-first-letter fname)))

(defn rooms-by-type [user-info as-key]
  ;; "d" (:direct), "c" (:channel), "p" (:private)
  (let [swapped       {:channel "c" :direct "d" :private "p"}
        room-type  (get swapped as-key)]
    (->> user-info
         :rooms
         (filter #(= (:t %) room-type))
         (map :name))))

(defn channels [user-info] (rooms-by-type user-info :channel))
(defn groups [user-info] (rooms-by-type user-info :private))
(defn dms [user-info] (rooms-by-type user-info :direct))

(defn messaged? [dms username]
  (some #{ username } dms))

(defn messaged-in-group-dm? [dms username]
  (let [r (str "^" username ",.*|.*," username "(,.*|$)")
        matches? (partial re-matches (re-pattern r))
        results (filter matches? dms)]
    (seq results)))

(defn joined-channels-list
  "From user `get/info` information and a list of channels, which of
  those channels is that user in?"
  [user-info clist]
  (set/intersection
   (into #{} clist)
   (into #{} (channels user-info))))

(defn channel-group-subsets
  "Take each map entry in :channel-groups and make a new one of the
  form `:channel-group-name subset` where `subset` is the subset of
  channels in that group which the user has joined"
  [user-info]
  (let [c-g (:channel-groups *config*)
        subsetf (fn [clist] (joined-channels-list user-info clist))
        reducef (fn [m entry] (update m (key entry) subsetf))]
    (reduce reducef c-g c-g))) ;; c-g twice provides it as both the object to iterate over and the initial value

(defn gen-fields
  "Template-able info from what the API returns about a user"
  [user-info] ;; `user-info` is just the data from `get/info`"
  {
   :first-name (first-name user-info)
   :email (email user-info)
   :id (:_id user-info)
   :rooms (:rooms user-info)
   :username (:username user-info)
   :bio (:statusText user-info)
   :self-set-name (:name user-info)
   :timezone (:utcOffset user-info)
   :connection (:statusConnection user-info)
   :join-date (:createdAt user-info)
   :channels (channels user-info)
   :groups (groups user-info)
   :dms (dms user-info)
   :channel-groups (channel-group-subsets user-info)
   ;; todo fix by making a different wrapper around joined-channels-list
   :messaged? (messaged? (:dms *config*) (:username user-info))
   :__all user-info ;; still include _all_ info from the `user-info` hashmap, in case
   })
