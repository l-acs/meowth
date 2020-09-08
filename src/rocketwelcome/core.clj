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


(defn message [cfg rid msg]
  (client/post
   (rocket-gen-url cfg "chat.sendMessage")
   (assoc (headers cfg) :form-params {:message {:rid rid :msg msg}} :content-type :json  )))


(defn get-request-body
  "Gets the :body of a request as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))

(defn get-room-info [cfg rid]
  (rocket-get cfg "rooms.info" "roomId" rid))

(defn get-data-from-username [cfg username]
  (->> username (rocket-get cfg "users.info" "username") get-request-body))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


