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

(defn rocket-url
  "Generate urls for RocketChat endpoints. method may be nil but must be
  passed explicitly"
  [url ns method]
  (str url "/api/v1/" (name ns)
   (when method
     (str "." (name method)))))

(defn rocket-get [ns method & args]
  (client/get
   (rocket-url (:url *config*) ns method)
   {:headers (headers)
    :insecure? true ;; todo: make this optional
    :query-params (apply hash-map args)}))

(defn rocket-post [ns method & args]
  (client/post
   (rocket-url (:url *config*) ns method)
   {:headers (headers)
    :form-params (apply hash-map args)}))

(defn response-body
  "Gets the :body of a response as a hashmap"
  [raw]
  (json/parse-string (:body raw) true))
