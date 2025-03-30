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
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    
    String hashedPassword = hashPassword(newUser.getPassword());
    newUser.setPassword(hashedPassword);
    
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User loginUser(User user) {

    User userByUsername = userRepository.findByUsername(user.getUsername());

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User with username: " + user.getUsername() + " not found");
    }
    String userPassword = hashPassword(user.getPassword());

    if(userByUsername.getPassword() != userPassword) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "wrong password");
    }
    updateStatus(user);
    return user;
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

  private String hashPassword(String password) {
    try{
      String hashedPassword = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(password.getBytes()));
      return hashedPassword;
    } catch (NoSuchAlgorithmException e) {
        // SHA-256 should always be available, but we handle it just in case
        throw new RuntimeException("SHA-256 algorithm not available", e);
    }
    
  }

  public void updateStatus(User user) {
    if(user.getStatus() == UserStatus.OFFLINE) {
      user.setStatus(UserStatus.ONLINE);
    } else {
      user.setStatus(UserStatus.OFFLINE);
    }
    userRepository.saveAndFlush(user);
  }
/* 
  private String secureStore(String frontendHashedPassword) {
    return encoder.encode(frontendHashedPassword); 
  }

  // Verify later
  private boolean verify(String frontendHashedInput, String storedBcryptHash) {
    return encoder.matches(frontendHashedInput, storedBcryptHash);
  }
  */
}
