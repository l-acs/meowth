(ns meowth.rest
  (:gen-class)
  (:use [meowth.config :only [*config*]])
  (:require
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.string :as str]))

(defn headers []
  {"X-Auth-Token" (:token *config*),
   "X-User-Id" (:id *config*),
   "Content-type" "application/json"})

(defn rocket-gen-url [url method]
  (str url "/api/v1/" method))

(defn rocket-get [method & args]
  (client/get
   (rocket-gen-url (:url *config*) method)
   {:headers (headers)
    :insecure? true ;; todo: make this optional
    :query-params (apply hash-map args)}))

(defn rocket-post [method & args]
  (client/post
   (rocket-gen-url (:url *config*) method)
   {:headers (headers)
    :form-params (apply hash-map args)}))

(defn response-body
  "Gets the :body of a response as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))
