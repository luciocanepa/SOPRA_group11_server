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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

  private static final String NOT_FOUND = "%s with ID %s was not found";
  private static final String CONFLICT = "User with username %s already exists";
  private static final String UNAUTHORIZED = "Invalid credentials";
  private static final String FORBIDDEN = "User is not authorized to perform this action";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MembershipService membershipService;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository,
                    MembershipService membershipService,
                    PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.membershipService = membershipService;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User getUser(Long id) {
    
    return this.userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", id)));
    
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

    updateStatus(userByUsername);
    userByUsername.setStatus(UserStatus.ONLINE);
    
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", id)));
  }

  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(CONFLICT, userToBeCreated.getUsername()));
    }
  }

  public void updateStatus(User user) {
    user.setStatus(UserStatus.OFFLINE);
    userRepository.save(user);
    userRepository.flush();
  }

  public User logoutUser(User user) {
    user.setStatus(UserStatus.OFFLINE);
    userRepository.save(user);
    userRepository.flush();
    return user;

  }

  public List<Group> getGroupsForUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", userId)));

    // Use the MembershipService instead of directly accessing the user's active groups
    return membershipService.getActiveGroupsForUser(user);
  }

  public User putUserEdits(Long id , UserPutDTO edits , String token ) {
    User user = findByToken(token); // validity of token already checked by the method

    if (!user.getId().equals(id)) { //id we got via token does not match id from the url
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
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