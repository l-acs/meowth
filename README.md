# meowth

## About

Make a DIY Rocketchat bot!

This program's original use case was to send messages to many or all
the users on a server. My goal in doing this was to make what it sent
so customizable that recipients would doubt it was even automated, and
would respond to it as though I had just sent it myself.

## Goals
* To build upon and streamline core functionalities of the RocketChat
  REST API to enhance usability
* To construct new high-level operations on RocketChat, made intuitive
  and easy with abstractions for simple and semantic programatic use
  (or interactive use via REPL)
* To provide non-developers (or anyone not eager to touch code) a way
  to leverage these operations, whether en masse as a one-off or on
  demand interactively


## Non-goals
* (WIP) To be a (boilerplate-y) wrapper around the RocketChat REST API

# Features

## Templating

To solve my initial problem, meowth employs weavejester's
[comb](https://github.com/weavejester/comb) to read in a templated,
user-defined blurb which would vary according to particular attributes
about a user.

This could simply being using a first name:

> Hi `<%=first-name%>`! We're going through the list of users, and it looks like we haven't messaged before.

For each user, there are a number of fields available:


```clojure
   :first-name ;; a user's first name, either taken from the name they set for themselves or by parsing their email address.
   :email
   :rooms ;; the list of channels a user is in
   :username
   :bio ;; a user's status
   :self-set-name ;; the name a user sets for themselves on Rocketchat
   :timezone
   :connection ;; is the user online?
   :channel-groups ;; which of the predefined groupings of channels in the configuration file is the user a part of?
   :messaged? ;; has this account message this user before?
   :__all ;; all 'raw' details about a user, in case this partial interface to that information is insufficient

```

Anything missing could be gleaned from `:__all` in a pinch.


### Channel groups
One notable field is the optional `:channel-groups`. There you can
define a list of channels in the configuration by which to sort
users. For example, in one of my use cases (a server for students in
the linguistics department), there were two channel groups:


```clojure
 :channel-groups {
          :language-channels [
                              "wolof"
                              "xhosa"
                              "yiddish"
							  ...
                              "urdu"
                              "welsh"
							  ]
          :course-channels [
                            "comp-ling-445"
                            "ling-201"
                            "ling-210"
                            "ling-360"
							...
							]
						}
```

The 'bot' would then send different sentences according to which
category the user was in (among being in one of both types of the
channels, in just the language channel group, in just the course
channel group, or in neither). But meowth's real power comes in the
more advanced operations you can think up.

## Leveraging Clojure
meowth can interpret Clojure logic interspersed within a blurb thanks
to `comb` in order to make more complex templating possible.

Here is a simplified version of the above example's `:blurb`:

```clojure
We're looking for feedback about how you like the site. Please share your thoughts!

<%=
(let [incourse? (not (empty? (:course-channels channel-groups)))
      inlang? (not (empty? (:language-channels channel-groups)))]
  (case [incourse? inlang?]
	[true true] \"Okay, so it looks like you've gotten a chance to join some course and language channels! That's awesome. \"
	[true false] \"Okay, so it looks like you've gotten a chance to join course channels, which is great! But by the way, we also have a _lot_ of language channels. They're in the directory (the little globe button in the menu), like #french, #cree, #ipa, etc!\"
	[false true] \"Okay, so it looks like you've gotten a chance to join some language courses, awesome.\n\n Oh but oh no, you're not in any course groupchats yet- be sure to join them! They're named #ling-201, #phil-210, #comp-ling-445, etc- click the directory (globe button in the menu) to see the full list. This was the main reason we made the site\n\nTLDR, our goal is to provide students a unified, centralized place for all things linguistics, from tutoring to class groups, to event information.\"
	[false false] \"Oh no, okay, so it looks like you're not in any course groupchats yet- be sure to join them! If you don't already know, they're like the main reason we made the site! There's a chat for every course in the department ( #ling-201, etc) and others in the program ( #phil-210, #comp-ling-445, etc).\n\nYou can scroll through them and join yours in the directory (click the globe button in the menu to see the full list). Basically our goal is to provide ling students a unified, centralized place for all things linguistics, from tutoring to class groups, to event information.\n\nTo that end, we also have a bunch of language exchange channels to nerd out, practice, ask questions, whatever. For example, there's #french, #mandarin, #ipa ... again, you can see all that in the directory! Even if you're totally new to linguistics, talking with these folks is a lot of fun and it's an awesome way to make friends in your classes or just generally meet people who share your interests :)\")) %>
```

As you can see, the `:blurb` field (a **string**!) allows you to be
quite elaborate (if you wish) in how personalized a message might get.
For another example, this message ended with:

```clojure
<%= (if (not selfsetname) \"\n\n(Also, I'm pretty sure other people can't see your email/username, so it might be a good idea to set your name in settings so people can recognize you!)\" \"\") %>
```

# Basic configuration
Currently, this program reads a configuration file in order to
determine who to message when it runs, and what to say to each of
them. Hopefully in the future there will be a simpler and 'easier' way
for users to interface with it.

Written in Clojure, meowth naturally is configured in EDN. It is
important to include basic information about the server and the
account you'll use:

```clojure
 :url "https://your.rocketchat.url"
 :token "your-43-character-token"
 :id "your-17-character-token"
```

There are also a couple of other settings:

  * If `:send-paragraphs-as-separate-messages` is true, the blurb will
    be split on double newlines into separate Rocketchat
    messages.
  * `:wpm` defines the rate, in words per minute, at which the bot
    should 'type' messages (to seem more human). If set to 0 or left
    `nil` (or thus omitted), there will be no delay between the
    messages the user sends.


As mentioned in earlier sections, for these operations you must define
a `:blurb` and, optionally, `:channel-groups`.


# License
meowth is free software.


Copyright © 2020 l-acs


This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

[Art](https://www.newgrounds.com/art/view/d34thmonk3y/space-cat) thanks to d34thmonk3y.
