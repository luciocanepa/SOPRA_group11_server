# Pomodoro Time Tracking app

## Group 11 (SOPRA fs-25)

Studying or working alone at home can feel isolating and unmotivating — and it’s easy to lose focus. That’s where our app comes in.

We created a web-based Pomodoro timer app that lets people study together in real time, even if they’re not in the same place. Users can join or create study groups, see each other’s live timer status, chat within the group, sync timers, and even plan sessions using Google Calendar.

Our goal was to build something that helps people stay connected and focused while studying remotely. Whether you’re working alone or as part of a group, our app helps bring structure to your time, makes studying feel a bit more social, and boosts motivation — all through a familiar and effective time management method.

## Built With

- [Java Springboot](https://spring.io/projects/spring-boot) - The Java backend framework
- [Gradle](https://gradle.org/) - Dependency Management and build tool
- [Lombok](https://projectlombok.org/) - Simplify getters and setters setup

## High level components

- **User Management**: The role of the [User](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java) is to handle registration, authentication, and user profile data. This also includes keeping track of the [Memberships](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/GroupMembership.java) to [Groups](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Group.java) the user is part of, as well as maintaining their current timer status, duration, and start time.

- **Group and Membership Management**: [Groups](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Group.java) store group data, including the ID of the group's creator. [Memberships](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/GroupMembership.java) act as connectors between users, groups, and invitations. They track which users are invited to a group, the status of those invitations, and therefore also the active members of a group.

- **Activity Tracking**: Whenever a [User](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java) updates their timer-related data (duration, status, or start time), an [Activity](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Activity.java) is created. This records the time the user worked during that session, which can later be retrieved for individual or group statistics.

- **WebSocket**: The [WebSocketService](src/main/java/ch/uzh/ifi/hase/soprafs24/service/WebSocketService.java) handles real-time communication. It broadcasts group chat messages, live timer and status updates of group members, and sends synchronization requests. These requests contain all the necessary information for group members to align their timers if they choose to accept the synchronization.



## Launch & deployment

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

1. Clone the repo locally:

```bash
git clone git@github.com:luciocanepa/SOPRA_group11_server.git
```

2. make sure you have the correct java version installed (v 17) and the correct gradle version (7.6.3):

```bash
java -version
```

```bash
./gradlew -version
```

3. you can build the project with gradle:

```bash
./gradlew build
```

4. and run the application locally

```bash
./gradlew bootrun
```

You can reference the [Gradlew wrapper documentation](https://docs.gradle.org/current/userguide/gradle_wrapper_basics.html#gradle_wrapper_basics) for additional commands and flags.

5. The app has 3 worklflows for deployment:

- `dockerize.yml`: create a Docker container that will be hosted on google cloud.
- as per instructions in `main.yml`: this also contains settings for sonarqube analysis (hosted on [sonarcloud](https://sonarcloud.io/organizations/luciocanepa-1/projects)).
- `pr.yml` define the correct environment and run the tests.

## Roadmap

We have built the core functionalities of our collaborative Pomodoro Application, but there are a few features and improvements that could be added to enhance the user experience even more. Therefore, future developers may consider the following additions:

#### 1. Real-Time Group Sync via WebSockets

**Goal:** Improve group responsiveness by updating group membership in real time.

**Description:** When a user joins or leaves a group, the change should be visible instantly for all other group members using WebSockets instead of requiring a manual page refresh on the group dashboard.

#### 2. Invite User via WebSocket
**Goal:** Improve user invitation flow and interactivity.

**Description:** Receiving group invitations should appear in real-time on the user's dashboard, without having to refresh their user dashboard in order to view it. This should ideally be solved via WebSocket.

#### 3. Enhanced Chat Features
**Goal:** Make the break-time chat more interactive and engaging.

**Suggestions:**
- Add message reactions or emojis to quickly reflect and show emotions
- Show who is currently typing
- Enable replies to a specific message / reference a message by tagging it
- Have private (1 on 1) chat rooms within the group (and/or outside of the group)

#### 4. Increased Gamification & Break Activities
**Goal:** Encourage user engagement and make breaks more enjoyable.

**Suggestions:**
- Introduce short mini-games during break sessions (like tic tac toe, rock paper scissors or hangman)
- Add a leaderboard or continuous streak counter 
- Implement reward badges for consistent study behavior
- Group-based achievements to build team motivation and measure your groups with others (e.g. A group gets a reward each day, as long as all group members have spent some time studying.)


## Authors

| Name | Email | Matriculation Number | GitHub Account |
|------|--------|-------------------|----------------|
| Lucio Canepa (group leader) | <lucio.canepa@uzh.ch> | 21-915-905 | luciocanepa |
| Anna Pang | <anna.pang@uzh.ch> | 17-968-660 | annapangUZH |
| Sharon Kelly Isler | <sharonkelly.isler@uzh.ch> | 19-757-103 | sharonisler |
| Moritz Leon Böttcher | <moritzleon.boettcher@uzh.ch> | 23-728-371 | moritzboet |
| Helin Capan | <helin.capan@uzh.ch> | 21-718-895 | HelinCapan |

## License

MIT License

Copyright (c) 2025 SOPRA-fs-25-group-11

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Acknowledgments

- Hat tip to anyone whose code was used
- Inspiration
- etc
