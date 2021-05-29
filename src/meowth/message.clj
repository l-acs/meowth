(ns meowth.message
  (:gen-class)
  (:refer meowth.rest)
  (:use [meowth.config :only [*config*]])
  (:require
   [clojure.string :as str]))

(defn send-message
  "Send message to a user directly (where @ is prepended) or to a channel or private group (where # is prepended)"
  [room msg]
  (rocket-post
   :chat "postMessage"
   :channel room
   :text msg))

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

(defn delay-send-message [room msg]
  (do
    (Thread/sleep (calculate-message-type-time msg (:wpm *config*)))
    (send-message room msg)))

(defn send-blurb [room msg]
  (if (:send-paragraphs-as-separate-messages *config*)
    (run! #(delay-send-message room %) (str/split msg #"\n\n"))
    (send-message room msg)))
