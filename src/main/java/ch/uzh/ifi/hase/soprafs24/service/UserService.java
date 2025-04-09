package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
  private final BCryptPasswordEncoder passwordEncoder;
  private final MembershipService membershipService;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository,
                    MembershipService membershipService) {
    this.userRepository = userRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.membershipService = membershipService;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);

    checkIfUserExists(newUser);

    // save user given the certain data
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User loginUser(User user) {
    User userByUsername = userRepository.findByUsername(user.getUsername());

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    if (!passwordEncoder.matches(user.getPassword(), userByUsername.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    userByUsername.setStatus(UserStatus.ONLINE);
    userByUsername = userRepository.save(userByUsername);
    userRepository.flush();

    log.debug("User logged in: {}", userByUsername);
    return userByUsername;
  }

  public User findByToken(String token) {
    User userByToken = userRepository.findByToken(token);

    if (userByToken == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    return userByToken;
  }

  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          String.format(baseErrorMessage, "username", "is"));
    }
  }

  public void updateStatus(User user) {
    user.setStatus(UserStatus.OFFLINE);
    userRepository.save(user);
    userRepository.flush();
  }

  public List<Group> getGroupsForUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Use the MembershipService instead of directly accessing the user's active groups
    return membershipService.getActiveGroupsForUser(user);
  }




  public User getUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return user;
  }



  public User putUserEdits(Long id , UserPutDTO edits , String token ) {
    User user = findByToken(token); // validity of token already checked by the method

    if (!user.getId().equals(id)) { //id we got via token does not match id from the url
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this user");
    }
    //if the field for username has a value and the new username equals another then it will not work
    if (edits.getUsername() != null) {
      if (edits.getUsername().equals(user.getUsername())) {
          // nothing happens when the entered username is the same
      }
      else if (userRepository.findByUsername(edits.getUsername()) != null) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "There is another user with this username, choose another one.");
      }
      else {
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



}