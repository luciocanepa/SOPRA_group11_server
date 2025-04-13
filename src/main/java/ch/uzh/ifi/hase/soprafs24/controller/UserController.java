package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserTimerPutDTO;
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
  public List<UserGetDTO> getAllUsers(@RequestHeader("Authorization") String token) {
    List<User> users = userService.getUsers(token);
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    User createdUser = userService.createUser(userInput);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO getUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
      User user = userService.getUserById(id, token);
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @GetMapping("/users/{id}/groups")
  @ResponseStatus(HttpStatus.OK)
  public List<GroupGetDTO> getGroupsForUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
      List<Group> groups = userService.getGroupsForUser(id, token);
      return groups.stream()
              .map(DTOMapper.INSTANCE::convertEntityToGroupGetDTO)
              .toList();
  }

  @PostMapping("/users/login")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    User loggedUser = userService.loginUser(userInput);
    
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedUser);
  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  public UserPutDTO putEdits(@PathVariable Long id, @RequestBody UserPutDTO edits, @RequestHeader("Authorization") String token) {
    User userEdited = userService.putUserEdits(id, edits, token);
    return DTOMapper.INSTANCE.convertEntityToUserPutDTO(userEdited);
  }

  @PutMapping("/users/{id}/timer")
  @ResponseStatus(HttpStatus.OK)
  public UserTimerPutDTO updateTimer(@PathVariable("id") Long id, @RequestBody UserTimerPutDTO userTimerPutDTO) {
    User user = userService.updateStatus(userTimerPutDTO, id);

    return DTOMapper.INSTANCE.convertEntityToUserTimerPutDTO(user);
  }

  @PostMapping("/users/{id}/logout")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO logoutUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
    User user = userService.getUserById(id, token);
    user = userService.logoutUser(user);
    
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }
}
