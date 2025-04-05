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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository membershipRepository;

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository,
                       @Qualifier("userRepository") UserRepository userRepository,
                       @Qualifier("groupMembershipRepository") GroupMembershipRepository membershipRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    public List<Group> getGroups() {
        return this.groupRepository.findAll();
    }

    public Group getGroupById(Long groupId) {
        return this.groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Group with ID " + groupId + " was not found"));
    }

    public Group createGroup(Group newGroup) {
        final Long adminId = newGroup.getAdminId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Admin user with ID " + adminId + " was not found"));
        
        newGroup.setAdminId(admin.getId());
        
        // Save the group first
        newGroup = groupRepository.save(newGroup);
        
        // Create an active membership for the admin
        GroupMembership membership = new GroupMembership();
        membership.setGroup(newGroup);
        membership.setUser(admin);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setInvitedBy(admin.getId()); // Admin invites themselves
        membership.setInvitedAt(LocalDateTime.now());
        
        // Add the membership to both entities
        newGroup.addMembership(membership);
        admin.addMembership(membership);
        
        // Save everything
        membership = membershipRepository.save(membership);
        newGroup = groupRepository.save(newGroup);
        admin = userRepository.save(admin);
        
        groupRepository.flush();
        userRepository.flush();

        return newGroup;
    }

    public Group addUserToGroup(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "User with ID " + userId + " was not found"));

        // Check if user is already a member (has an ACTIVE membership)
        boolean isAlreadyMember = group.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId) && 
                         m.getStatus() == MembershipStatus.ACTIVE);
        
        if (!isAlreadyMember) {
            // Check if there's a pending invitation
            GroupMembership existingMembership = group.getMemberships().stream()
                    .filter(m -> m.getUser().getId().equals(userId))
                    .findFirst()
                    .orElse(null);
            
            if (existingMembership != null) {
                // Update existing membership to ACTIVE
                existingMembership.setStatus(MembershipStatus.ACTIVE);
                membershipRepository.save(existingMembership);
            } else {
                // Create new ACTIVE membership directly
                GroupMembership membership = new GroupMembership();
                membership.setGroup(group);
                membership.setUser(user);
                membership.setStatus(MembershipStatus.ACTIVE);
                membership.setInvitedBy(group.getAdminId());
                membership.setInvitedAt(LocalDateTime.now());
                
                group.addMembership(membership);
                user.addMembership(membership);
                
                membershipRepository.save(membership);
            }
            
            group = groupRepository.save(group);
            user = userRepository.save(user);
            
            groupRepository.flush();
            userRepository.flush();
        }

        return group;
    }
}
