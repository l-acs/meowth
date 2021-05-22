(ns meowth.message
  (:gen-class)
  (:refer meowth.rest)
  (:use [meowth.config :only [*config*]])
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [meowth.user :as u]))

(defn send-message-to-rid [msg rid]
  (rocket-post-new
   "chat.sendMessage"
   :message { :rid rid :msg msg }))

  ;; (client/post
  ;;  (rocket-gen-url cfg "chat.postMessage")
  ;;  (assoc (headers cfg) :form-params {:channel room :text msg :content-type :json})))

(defn send-message-to-user [msg username]
  (->> username
       (u/get-dm-info)
       :rid
       (send-message-to-rid msg)))

(defn send-message-to-user-fast [rooms msg username] ;; this doesn't bother to get a user's rooms each time; it takes them as an argument
  (->> username
       (u/dm-info-fast rooms)
       :rid
       (send-message-to-rid msg)))


(defn post-message [room msg] ;; 'you must be logged in to do this', despite being logged in
  (client/post
   (rocket-gen-url (:url *config*) "chat.postMessage")
   (assoc (headers) :form-params {:channel room :text msg :content-type :json})))

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

(defn delay-send-message [username msg]
  (do
    (Thread/sleep (calculate-message-type-time msg (:wpm *config*)))
    (post-message (str "@" username) msg)))

 ;; todo: rewrite this based on send-message-to-user
(defn send-blurb-to-user [username msg]
  (if (:send-paragraphs-as-separate-messages *config*)
    (run! #(delay-send-message username %) (str/split msg #"\n\n"))
    (post-message (str "@" username) msg)))
