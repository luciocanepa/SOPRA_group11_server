package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.HexFormat;
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

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    // Hash the password before saving
    try{
      String hashedPassword = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(newUser.getPassword().getBytes()));
      newUser.setPassword(hashedPassword);
    } catch (Exception e) { 
      System.out.println("An error occurred: " + e.getMessage());
    }
    
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This method finds a user by their token
   *
   * @param token the token to search for
   * @return User if found
   * @throws org.springframework.web.server.ResponseStatusException if user not found
   */
  public User findByToken(String token) {
    User user = userRepository.findByToken(token);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this token was not found");
    }
    return user;
  }
  
  /**
   * This method finds a user by their ID
   *
   * @param id the user ID to search for
   * @return User if found
   * @throws org.springframework.web.server.ResponseStatusException if user not found
   */
  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
            "User with ID " + id + " was not found"));
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,"The username provided is not unique. Therefore, the user could not be created!");
    } 
  }
}
