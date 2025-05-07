package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserService userService;

    private static final String FORBIDDEN = "User is not authorized to perform this action";

    @Autowired
    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public void isUserValid(Long userId, String token) {
        if(!userService.findById(userId).equals(userService.findByToken(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
        }
    }

    public void isUserInGroup(Long userId, Long groupId) {
        if (!userService.isUserInGroup(userId, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
        }
    }

    public void authCheck(String userId, String groupId, String token) {
        userService.validateToken(token);
        isUserValid(Long.parseLong(userId), token);
        isUserInGroup(Long.parseLong(userId), Long.parseLong(groupId));
    }


}
