package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserTimerPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MembershipService membershipService;
  private final WebSocketService webSocketService;
  private final ActivityService activityService;

  private static final String NOT_FOUND = "%s with ID %s was not found";
  private static final String CONFLICT = "User with username %s already exists";
  private static final String UNAUTHORIZED = "Invalid token";
  private static final String FORBIDDEN = "User is not authorized to perform this action";

  public UserService(@Qualifier("userRepository") UserRepository userRepository,
      MembershipService membershipService,
      PasswordEncoder passwordEncoder,
      WebSocketService webSocketService,
      ActivityService activityService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.membershipService = membershipService;
    this.webSocketService = webSocketService;
    this.activityService = activityService;
  }

  public List<User> getUsers(String token) {
    validateToken(token);
    return this.userRepository.findAll();
  }

  public User getUserById(Long id, String token) {
    validateToken(token);
    return this.userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", id)));
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);

    checkIfUserExists(newUser);

    // Encode the password before saving
    newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

    // save user given the certain data
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User loginUser(User user) {
    User userByUsername = userRepository.findByUsername(user.getUsername());

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
    }

    if (!passwordEncoder.matches(user.getPassword(), userByUsername.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
    }

    userByUsername.setStatus(UserStatus.ONLINE);
    userByUsername = userRepository.save(userByUsername);
    userRepository.flush();

    // send status update to all groups the user is in with websocket
    List<Group> groupIds = membershipService.getActiveGroupsForUser(userByUsername);
    for (Group group : groupIds) {
      webSocketService.sendTimerUpdate(
          userByUsername.getId().toString(),
          userByUsername.getUsername(),
          group.getId().toString(),
          userByUsername.getStatus().toString(),
          "0",
          LocalDateTime.now().toString());
    }

    return userByUsername;
  }

  public User findByToken(String token) {
    User userByToken = userRepository.findByToken(token);

    if (userByToken == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
    }

    return userByToken;
  }

  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", id)));
  }

  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(CONFLICT, userToBeCreated.getUsername()));
    }
  }

  public User logoutUser(Long id, String token) {
    validateToken(token);

    User user = userRepository.findByToken(token);
    if (!user.getId().equals(id)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
    }

    // Create new activity when user logs out if it was in a work session
    if (user.getStatus() == UserStatus.WORK) {
      Activity activity = new Activity();
      activity.setUser(user);
      activity.setStartDateTime(user.getStartTime());
      activity.setEndDateTime(LocalDateTime.now());
      activityService.createActivity(activity, id, token);
    }

    user.setStatus(UserStatus.OFFLINE);
    userRepository.save(user);
    userRepository.flush();

    // send status update to all groups the user is in with websocket
    List<Group> groupIds = membershipService.getActiveGroupsForUser(user);
    for (Group group : groupIds) {
      webSocketService.sendTimerUpdate(
          user.getId().toString(),
          user.getUsername(),
          group.getId().toString(),
          user.getStatus().toString(),
          "0",
          LocalDateTime.now().toString());
    }

    return user;
  }

  public List<Group> getGroupsForUser(Long userId, String token) {
    validateToken(token);
    User user = userRepository.findByToken(token);
    if (!user.getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
    }

    return membershipService.getActiveGroupsForUser(user);
  }

  public User putUserEdits(Long id, UserPutDTO edits, String token) {
    User user = findByToken(token); // validity of token already checked by the method

    if (!user.getId().equals(id)) { // id we got via token does not match id from the url
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
    }
    // if the field for username has a value and the new username equals another
    // then it will not work
    if (edits.getUsername() != null) {
      if (edits.getUsername().equals(user.getUsername())) {
        // nothing happens when the entered username is the same
      } else if (userRepository.findByUsername(edits.getUsername()) != null) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(CONFLICT, edits.getUsername()));
      } else {
        user.setUsername(edits.getUsername());
      }
    }

    if (edits.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(edits.getPassword()));
    }

    if (edits.getName() != null) {
      user.setName(edits.getName());
    }

    if (edits.getBirthday() != null) {
      user.setBirthday(edits.getBirthday());
    }

    if (edits.getTimezone() != null) {
      user.setTimezone(edits.getTimezone());
    }

    if (edits.getProfilePicture() != null) {
      user.setProfilePicture(edits.getProfilePicture());
    }

    userRepository.save(user);
    userRepository.flush();
    return user;
  }

  public User updateStatus(UserTimerPutDTO userTimer, Long userId, String token) {
    User user = findById(userId);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", userId));
    }

    if (!user.getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
    }

    // Create new activity when finished a work session
    // If the user had status WORK, then any change in status should create a new
    // activity
    if (user.getStatus() == UserStatus.WORK) {
      Activity activity = new Activity();
      activity.setUser(user);
      activity.setStartDateTime(user.getStartTime());
      activity.setEndDateTime(userTimer.getStartTime());
      activityService.createActivity(activity, userId, token);
    }

    user.setStatus(userTimer.getStatus());
    user.setStartTime(userTimer.getStartTime());
    user.setDuration(userTimer.getDuration());

    user = userRepository.save(user);
    userRepository.flush();

    List<Group> groupIds = membershipService.getActiveGroupsForUser(user);
    for (Group group : groupIds) {
      webSocketService.sendTimerUpdate(
          user.getId().toString(),
          user.getUsername(),
          group.getId().toString(),
          user.getStatus().toString(),
          user.getDuration().toString(),
          user.getStartTime().toString());
    }

    return user;
  }

  @Transactional
  public boolean isUserInGroup(Long userId, Long groupId) {
    User user = findById(userId);
    return user.getMemberships().stream()
        .anyMatch(membership -> membership.getGroup().getId().equals(groupId) &&
            membership.getStatus() == MembershipStatus.ACTIVE);
  }

  public void validateToken(String token) {
    log.info("Validating token from UserService: {}", token);
    if (userRepository.findByToken(token) == null) {
      log.warn("Token validation failed from UserService: {}", token);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
    }
  }

}