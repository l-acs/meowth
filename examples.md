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
