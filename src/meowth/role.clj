(ns meowth.role
  (:gen-class)
  (:refer meowth.rest)
  (:require
   [meowth.gather :as gather]
   [meowth.user :as user]))

(defn create-role
  "Create a role with a name (for the system, e.g. 'bot')  and a description (what will be shown, e.g 'Bot')"
  [cfg name description]
  (rocket-post cfg "roles.create" "name" name "description" description))

(defn give-user-role [cfg username role]
  (rocket-post cfg "roles.addUserToRole" "roleName" role "username" username))

(defn _channel-add-owner [cfg username rid]
  (rocket-post cfg "channels.addOwner"
               "userId" (user/get-id-from-username cfg username)
               "roomId" rid))

(defn _channel-remove-owner [cfg username rid]
  (rocket-post cfg "channels.removeOwner"
               "userId" (user/get-id-from-username cfg username)
               "roomId" rid))

(defn channel-add-owner [cfg username channelname]
  (rocket-post cfg "channels.addOwner"
               "userId" (user/get-id-from-username cfg username)
               "roomId" (gather/get-rid-from-channel-name cfg channelname)))

(defn channel-remove-owner [cfg username channelname]
  (rocket-post cfg "channels.removeOwner"
               "userId" (user/get-id-from-username cfg username)
               "roomId" (gather/get-rid-from-channel-name cfg channelname)))
