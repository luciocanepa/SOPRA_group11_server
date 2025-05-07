Below is a consolidated REST‐interface summary for all three controllers. For each operation it shows the HTTP method, path, expected request body (if any), required headers, response body, and status code.

| Controller          | Operation                           | HTTP Method | Path                                 | Request Body DTO        | Request Header                | Response Body DTO               | HTTP Status | Description                                         |
|---------------------|-------------------------------------|-------------|--------------------------------------|-------------------------|-------------------------------|---------------------------------|-------------|-----------------------------------------------------|
| **GroupController** | List all groups                     | GET         | `/groups`                            | –                        | `Authorization: Bearer <token>` | `List<GroupGetDTO>`             | 200 OK      | Retrieve all groups visible to the user             |
|                     | Get a single group                  | GET         | `/groups/{gid}`                      | –                        | `Authorization: Bearer <token>` | `GroupGetDTO`                   | 200 OK      | Retrieve details of group with id `gid`             |
|                     | Create a new group                  | POST        | `/groups`                            | `GroupPostDTO`           | `Authorization: Bearer <token>` | `GroupGetDTO`                   | 201 Created | Create group; returns created resource              |
|                     | Update an existing group            | PUT         | `/groups/{gid}`                      | `GroupPutDTO`            | `Authorization: Bearer <token>` | `GroupGetDTO`                   | 200 OK      | Update group with id `gid`                          |
|                     | Delete a group                      | DELETE      | `/groups/{gid}`                      | –                        | `Authorization: Bearer <token>` | –                               | 204 No Content | Permanently delete group `gid`                   |
|                     | Remove user from group              | DELETE      | `/groups/{gid}/users/{uid}`          | –                        | `Authorization: Bearer <token>` | –                               | 204 No Content | Remove user `uid` from group `gid`               |
| **UserController**  | List all users                      | GET         | `/users`                             | –                        | `Authorization: Bearer <token>` | `List<UserGetDTO>`              | 200 OK      | Retrieve all users                                 |
|                     | Register a new user                 | POST        | `/users/register`                    | `UserPostDTO`            | –                              | `UserGetDTO`                    | 201 Created | Create and return new user                         |
|                     | Get a single user                   | GET         | `/users/{id}`                        | –                        | `Authorization: Bearer <token>` | `UserGetDTO`                    | 200 OK      | Retrieve details of user `id`                      |
|                     | List groups for a user              | GET         | `/users/{id}/groups`                 | –                        | `Authorization: Bearer <token>` | `List<GroupGetDTO>`             | 200 OK      | Get all groups that user `id` belongs to           |
|                     | Login                               | POST        | `/users/login`                       | `UserPostDTO`            | –                              | `UserGetDTO`                    | 200 OK      | Authenticate and return user data                  |
|                     | Update user profile                 | PUT         | `/users/{id}`                        | `UserPutDTO`             | `Authorization: Bearer <token>` | `UserPutDTO`                    | 200 OK      | Modify user’s profile fields                       |
|                     | Update user timer/status            | PUT         | `/users/{id}/timer`                  | `UserTimerPutDTO`        | –                              | `UserTimerPutDTO`               | 200 OK      | Update user’s timer/status without auth header     |
|                     | Logout                              | POST        | `/users/{id}/logout`                 | –                        | `Authorization: Bearer <token>` | `UserGetDTO`                    | 200 OK      | Invalidate session, return updated user info       |
| **InvitationController** | Send group invitation          | POST        | `/groups/{gid}/invitations`          | `InvitationPostDTO`      | `Authorization: Bearer <token>` | `InvitationGetDTO`              | 201 Created | Invite another user to group `gid`                 |
|                     | List invitations in a group         | GET         | `/groups/{gid}/invitations`          | –                        | `Authorization: Bearer <token>` | `List<InvitationGetDTO>`        | 200 OK      | Get pending invites for group `gid`                |
|                     | List invitations for a user         | GET         | `/users/{uid}/invitations`           | –                        | `Authorization: Bearer <token>` | `List<InvitationGetDTO>`        | 200 OK      | Get pending invites received by user `uid`         |
|                     | Accept an invitation                | PUT         | `/invitations/{iid}/accept`          | –                        | `Authorization: Bearer <token>` | `GroupGetDTO`                   | 200 OK      | Accept invite `iid`, returns updated group details |
|                     | Reject an invitation                | PUT         | `/invitations/{iid}/reject`          | –                        | `Authorization: Bearer <token>` | –                               | 204 No Content | Reject invite `iid` without returning content    |

**Notes on headers and DTOs**  

- All secured endpoints require an `Authorization` header with a Bearer token.  
- DTO abbreviations:  
  - **GetDTO** = data returned to client  
  - **PostDTO** = payload for creation  
  - **PutDTO** = payload for updates  

This table should allow frontend and API‐documentation tools to generate a clear specification of your REST interface.
