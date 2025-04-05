package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository membershipRepository;
    private final MembershipService membershipService;

    private final static String NOT_FOUND = "%s with ID %s was not found";

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository,
                       @Qualifier("userRepository") UserRepository userRepository,
                       @Qualifier("groupMembershipRepository") GroupMembershipRepository membershipRepository,
                       MembershipService membershipService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipService = membershipService;
    }

    public List<Group> getGroups() {
        return this.groupRepository.findAll();
    }

    public Group getGroupById(Long groupId) {
        return this.groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format(NOT_FOUND, "Group", groupId)));
    }

    public Group createGroup(Group newGroup) {
        final Long adminId = newGroup.getAdminId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(NOT_FOUND, "Admin user", adminId)));
        
        newGroup.setAdminId(admin.getId());
        
        // Save the group first
        newGroup = groupRepository.save(newGroup);
        
        // Create an active membership for the admin using the membership service
        membershipService.addUserToGroup(admin, newGroup, MembershipStatus.ACTIVE, admin.getId());
        
        return newGroup;
    }

    public Group addUserToGroup(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format(NOT_FOUND, "User", userId)));

        // Check if user is already a member (has an ACTIVE membership)
        boolean isAlreadyMember = group.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId) && 
                         m.getStatus() == MembershipStatus.ACTIVE);
        
        if (!isAlreadyMember) {
            // Check if there's a pending invitation
            GroupMembership existingMembership = membershipService.findByUserAndGroup(user, group);
            
            if (existingMembership != null) {
                // Update existing membership to ACTIVE
                membershipService.updateMembershipStatus(existingMembership, MembershipStatus.ACTIVE);
            } else {
                // Create new ACTIVE membership using the membership service
                membershipService.addUserToGroup(user, group, MembershipStatus.ACTIVE, group.getAdminId());
            }
        }

        return group;
    }
}
