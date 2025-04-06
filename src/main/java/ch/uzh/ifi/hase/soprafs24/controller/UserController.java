package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ManageProfileDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

    @GetMapping("/users/{id}/groups")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupGetDTO> getGroupsForUser(@PathVariable("id") Long id) {
        List<Group> groups = userService.getGroupsForUser(id);
        return groups.stream()
                .map(DTOMapper.INSTANCE::convertEntityToGroupGetDTO)
                .toList();
    }

  @PostMapping("/users/login")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // login user
    User loggedUser = userService.loginUser(userInput);
    
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedUser);
  }









  


  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  public ManageProfileDTO getUser(@PathVariable Long id) {

    User user = userService.getUser(id);
    return DTOMapper.INSTANCE.ManageUserProfileDTO(user);

  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  public UserPutDTO putEdits(@PathVariable Long id, @RequestBody UserPutDTO edits) {

    User userEdited = userService.putUserEdits(id, edits);

    return DTOMapper.INSTANCE.convertEntityToUserPutDTO(userEdited);
  }



}