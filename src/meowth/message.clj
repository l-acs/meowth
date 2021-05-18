(ns meowth.message
  (:gen-class)
  (:refer meowth.rest)
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [meowth.user :as u]))

(defn send-message-to-rid [cfg msg rid]
  (-> cfg
      (rocket-post-new
       "chat.sendMessage"
       :message { :rid rid :msg msg })))

  ;; (client/post
  ;;  (rocket-gen-url cfg "chat.postMessage")
  ;;  (assoc (headers cfg) :form-params {:channel room :text msg :content-type :json})))

(defn send-message-to-user [cfg msg username]
  (->> username
       (u/get-dm-info cfg)
       :rid
       (send-message-to-rid cfg msg)))

(defn send-message-to-user-fast [cfg rooms msg username] ;; this doesn't bother to get a user's rooms each time; it takes them as an argument
  (->> username
       (u/dm-info-fast cfg rooms)
       :rid
       (send-message-to-rid cfg msg)))


(defn post-message [cfg room msg] ;; 'you must be logged in to do this', despite being logged in
  (client/post
   (rocket-gen-url cfg "chat.postMessage")
   (assoc (headers cfg) :form-params {:channel room :text msg :content-type :json})))

(defn calculate-message-type-time
  "Type message at appropriate WPM delay"
  [msg wpm]
  (if (or (= 0 wpm) (nil? wpm))
    0
    (let [len (count (str/split msg #""))
          words (/ len 10) ;; according to Wikipedia, WPM is calculated by counting a 'word' as a sequence of any 5 characters. This felt crazy slow, so I upped it.
          minutes (/ words wpm)
          milliseconds (* minutes 60000)]
      (long milliseconds))))

(defn delay-send-message [cfg username msg]
  (do
    (Thread/sleep (calculate-message-type-time msg (:wpm cfg)))
    (post-message cfg (str "@" username) msg)))

(defn send-blurb-to-user [cfg username msg] ;; this should be based on "send message to user" which doesn't yet exist
  (if (:send-paragraphs-as-separate-messages cfg)
    (run! #(delay-send-message cfg username %) (str/split msg #"\n\n"))
    (post-message cfg (str "@" username) msg)))
