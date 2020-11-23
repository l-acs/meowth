(ns meowth.rest
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.string :as str]))

(defn headers [cfg]
  {"X-Auth-Token" (:token cfg),
   "X-User-Id" (:id cfg),
   "Content-type" "application/json"})

(defn rocket-gen-url [cfg call & args]
  (str (cfg :url)  "/api/v1/" call
    (when args (->> args conj (partition 2) (map (fn [[x y]] (str x "=" y))) (str/join "&") (str "?"))))) ;; maybe don't do this

(defn rocket-get [cfg call & args]
  (client/get
   (apply rocket-gen-url cfg call args)
   {:headers (headers cfg)}))

(defn rocket-post [cfg call & args]
  (client/post
   (rocket-gen-url cfg call)
   {:headers (headers cfg)
    :form-params (apply hash-map args)}))

(defn response-body
  "Gets the :body of a response as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))
