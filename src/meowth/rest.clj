(ns meowth.rest
  (:gen-class)
  (:use [meowth.config :only [*config*]])
  (:require
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.string :as str]))

(defn headers [cfg]
  {"X-Auth-Token" (:token cfg),
   "X-User-Id" (:id cfg),
   "Content-type" "application/json"})

;; (defn rocket-gen-url [cfg call & args]
;;   (str (cfg :url)  "/api/v1/" call
;;     (when args (->> args conj (partition 2) (map (fn [[x y]] (str x "=" y))) (str/join "&") (str "?"))))) ;; maybe don't do this

(defn rocket-gen-url [url call]
  (str url "/api/v1/" call))

;; (defn rocket-url [url call & args]
;;   (str url  "/api/v1/" call
;;     (when args (->> args conj (partition 2) (map (fn [[x y]] (str x "=" y))) (str/join "&") (str "?"))))) ;; maybe don't do this


;; (defn rocket-get [cfg call & args]
;;   (client/get
;;    (apply rocket-gen-url cfg call args)
;;    {:headers (headers cfg)}))

(defn rocket-get [call & args]
  (client/get
   (rocket-gen-url (:url *config*) call)
   {:headers (headers *config*)
    :query-params (apply hash-map args)}))


(defn rocket-post [call & args]
  (client/post
   (rocket-gen-url (:url *config*) call)
   {:headers (headers *config*)
    :form-params (apply hash-map args)}))

(defn rocket-post-new [call & args]
  (client/post
   (rocket-gen-url (:url *config*) call)
   {:headers (headers *config*)
    :form-params (apply hash-map args)
    :content-type :json}))


(defn response-body
  "Gets the :body of a response as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))
