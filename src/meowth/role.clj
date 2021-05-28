(ns meowth.role
  (:gen-class)
  (:refer meowth.rest)
  (:require
   [meowth.gather :as gather]
   [meowth.user :as user]))

(defn get-roles []
  (:roles (response-body
           (rocket-get "roles.list"))))

(defn get-users-in-role [role]
  (response-body
   (rocket-get "roles.getUsersInRole" "role" role)))

(defn get-matching-roles
  "Get information about any roles whose given trait matches a particular value"
  [k v]
  (filter #(= (k %) v)
        (get-roles)))

(defn get-role
  "Get information about the role with a given id."
  [id]
  (first
   (get-matching-roles :_id id)))

(defn create-role
  "Create a role with a name (for the system, e.g. 'bot')  and a description (what will be shown, e.g 'Bot')"
  [name description]
  (rocket-post "roles.create" "name" name "description" description))

(defn give-user-role [username role]
  (rocket-post "roles.addUserToRole" "roleName" role "username" username))
