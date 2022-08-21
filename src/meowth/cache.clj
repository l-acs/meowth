(ns meowth.cache
  (:gen-class)
  (:require
   [meowth.get :as get]
   [meowth.user :as user]))

(defonce users (agent {}))
(defonce channels (agent {}))

(defn ignore-first [_ f & args] (apply f args))

(defn send-ignore
  "Dispatch an action to an agent. Returns the agent immediately.
  Subsequently, in a thread from a thread pool, the state of the agent
  will be set to the value of:

  (apply action-fn args)

  That is, ignore the current state of the agent."
  [a f & args]
  (apply send a ignore-first f args))

(defn refresh-channels-once [channel-agent]
  (send-ignore channel-agent get/channels))

(defn refresh-users-once [user-agent]
  ;; we can add each user via assoc without risking loss of information
  (apply send user-agent user/all-users-hashmap (map :username (get/users))))

(defn refresh-channels-users [channel-agent user-agent seconds]
  (future
    (loop []
      (refresh-channels-once channel-agent)
      (refresh-users-once user-agent)
      (Thread/sleep (* 1000 seconds))
      (recur))))

(comment
  (refresh-channels-users channels users 300))

(comment
  (send users user/all-users-hashmap "username1" "username2" "username3" "username4")
  (apply send users user/all-users-hashmap '("username1" "username5"))
  (keys @users)
  ;; => ("username1" "username2" "username3" "username4" "username5")

  (apply send users user/all-users-hashmap (map :username (get/users))) ;; equivalent to (refresh-users-once users)

  (take 10 (keys @users))
  ;; => ("david2" "zang66"  "stella"  "damien"  "robert"  "xuan"  "jang"  "milkbag"  "wack"  "chen54")
  ;; i.e. users we didn't have there before!

  (send-ignore channels get/channels) ;; equivalent to (refresh-channels-once channels)

  ;; useful, included here since it uses the cache
  (def me
    (->> @cache/users
         vals
         (filter #(= (:id %) (:id *config*)))
         first))
)
