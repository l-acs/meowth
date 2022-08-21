# Basic use

## User info
Get a user's info using the default configuration
```clojure
(rocket-get
      :users "info" :username "rhicke")
```

or with more abstraction
```clojure
(def my-user (get/info :users "rhicke"))
```

more still:
```clojure
(user/gen-fields my-user)
```

## Alternative configuration
Like the examples above:
```clojure
;; where `slum` is alternative configuration (for the Society of Linguistics Undergraduates at McGill RocketChat at https://chat.slum.club/)
(with-config slum
    (rocket-get :users "info" :fields "{\"userRooms\": 1}" :username "rhicke")

;; or
(with-config slum
	(get/info :users "rhicke"))

;; or
(with-config slum
     (user/gen-fields (get/info :users "rhicke")))
```


# Putting it all together

Send a message to `rhicke` with a human-readable list of the emails of
all new members (those who had joined since September 2020)

```clojure
(message/send-message
	"@rhicke"
	(->> (get/users)
	  (filter (comp :join-date re-matches (re-pattern "2021-( ... ")  ) )) ;; todo: compose all this so it works
	  (map :email)
	  (str/join " "))) ;; todo test this
```

## Modifying rooms: leaders and owners

```clojure
(channel/modify-user-in-room my-user :remove :owner :channels "wg-collab")

(channel/modify-user-in-room my-user :add :leader :channels "meowth-test")
```


## Remake a channel from scratch, define its description and topic, invite some users, and change the owner

```clojure
(def me
  (->> @cache/users
       vals
       (filter #(= (:id %) (:id *config*)))
       first))

(let [rocketcat (get @cache/users "rocket.cat")]  ;; this gets the full information about this user, including the id, which is what most functions care about
    (doto "meowth-test-2"
      channel/create
      (channel/set-topic "For testing! Again!")
      (channel/set-description "Using this channel to test out the RocketChat API. You can learn more in the #meowth channel. This got set by a nice `doto` statement.")
      (channel/invite-user rocketcat)
      (channel/grant-status :add :owner rocketcat)
      (channel/grant-status :remove :owner me)))
```

## Delete some unused channels
```clojure
(map channel/delete ["meowth-test-3" "meowth-test-1" "off-topic-test"])
```


## Find out more about the last users to have sent a message to channels ending in "taskforce" who are online now


```clojure
(->> @cache/channels
  (filter #(re-matches #".*taskforce" (:name %)))
  (map #(->> % :lastMessage :u :username (get @cache/users)))
  (filter #(not= (:connection %) "offline")))
```

## Create a new channel, provide some information, make it a default, add all users, then send an initial message

```clojure
(let [topic "...."
        description "What it says on the tin."
        msg "Hey everyone! [...] So we've made this #off-topic channel!  Please use it :smiling_imp: [...]"]

  (doto "off-topic"
    channel/create
    (channel/set-topic topic)
    (channel/set-description description)
    (channel/set-default true)
    channel/add-all
    (message/send-message msg)))
```
