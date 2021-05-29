(ns meowth.role
  (:gen-class)
  (:refer meowth.rest))

(defn create-role
  "Create a role with a name (for the system, e.g. 'bot')  and a description (what will be shown, e.g 'Bot')"
  [name description]
  (rocket-post :roles "create" :name name :description description))

(defn give-user-role [username role]
  (rocket-post :roles "addUserToRole" :roleName role :username username))
