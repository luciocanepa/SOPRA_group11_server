package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class GroupService {

    private final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;

    private static final String NOT_FOUND = "%s with ID %s was not found";
    private static final String UNAUTHORIZED = "Invalid token";
    private static final String FORBIDDEN = "Only the admin can %s the group";

    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository,
            @Qualifier("userRepository") UserRepository userRepository,
            MembershipService membershipService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipService = membershipService;
    }

    public List<Group> getGroups(String token) {
        validateToken(token);
        return this.groupRepository.findAll();
    }

    public Group getGroupById(Long groupId, String token) {
        validateToken(token);
        return this.groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(NOT_FOUND, "Group", groupId)));
    }

    public Group findById(Long groupId) {
        return this.groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(NOT_FOUND, "Group", groupId)));
    }

    public Group createGroup(Group newGroup, String token) {
        validateToken(token);
        User admin = userRepository.findByToken(token);

        newGroup.setAdminId(admin.getId());
        newGroup = groupRepository.save(newGroup);
        membershipService.addUserToGroup(admin, newGroup, MembershipStatus.ACTIVE, admin.getId());

        return newGroup;
    }

    public Group updateGroup(Long groupId, Group updatedGroup, String token) {
        validateToken(token);
        User admin = userRepository.findByToken(token);

        Group existingGroup = getGroupById(groupId, token);
        if (!admin.getId().equals(existingGroup.getAdminId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(FORBIDDEN, "update"));
        }

        if (updatedGroup.getName() != null) {
            existingGroup.setName(updatedGroup.getName());
        }

        if (updatedGroup.getDescription() != null) {
            existingGroup.setDescription(updatedGroup.getDescription());
        }

        if (updatedGroup.getImage() != null) {
            existingGroup.setImage(updatedGroup.getImage());
        }

        return groupRepository.save(existingGroup);
    }

    public void deleteGroup(Long groupId, String token) {
        validateToken(token);
        User admin = userRepository.findByToken(token);
        Group group = getGroupById(groupId, token);
        if (!admin.getId().equals(group.getAdminId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(FORBIDDEN, "delete"));
        }

        List<GroupMembership> memberships = new ArrayList<>(group.getMemberships());
        for (GroupMembership membership : memberships) {
            User user = membership.getUser();
            membershipService.removeUserFromGroup(user, group);
        }

        groupRepository.delete(group);
    }

    public void removeUserFromGroup(Long groupId, Long userId, String token) {
        validateToken(token);
        User admin = userRepository.findByToken(token);
        Group group = getGroupById(groupId, token);

        // Check if the requesting user is the admin of the group
        if (!admin.getId().equals(group.getAdminId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(FORBIDDEN, "remove users from"));
        }

        // Get the user to remove
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(NOT_FOUND, "User", userId)));

        // Check if the user is a member of the group
        GroupMembership membership = membershipService.findByUserAndGroup(userToRemove, group);
        if (membership == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with ID %s is not a member of the group", userId));
        }

        // Remove the user from the group
        membershipService.removeUserFromGroup(userToRemove, group);

    }

    /**
     * Gets all groups that a user is a member of
     * 
     * @param userId the ID of the user
     * @return list of group IDs the user is a member of
     */
    public List<Long> getGroupsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(NOT_FOUND, "User", userId)));

        return user.getMemberships().stream()
                .map(membership -> membership.getGroup().getId())
                .toList();
    }

    private void validateToken(String token) {
        log.info("Validating token from GroupService: {}", token);
        if (userRepository.findByToken(token) == null) {
            log.warn("Token validation failed from GroupService: {}", token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
        }
    }
}
