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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Membership Service Implementation
 * This class implements the MembershipService interface and provides
 * concrete implementations for membership operations.
 */
@Service
@Transactional
public class MembershipServiceImpl implements MembershipService {

    private final GroupMembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public MembershipServiceImpl(
            @Qualifier("groupMembershipRepository") GroupMembershipRepository membershipRepository,
            @Qualifier("groupRepository") GroupRepository groupRepository,
            @Qualifier("userRepository") UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GroupMembership addUserToGroup(User user, Group group, MembershipStatus status, Long invitedBy) {
        // Check if membership already exists
        Optional<GroupMembership> existingMembership = membershipRepository.findByGroupAndUser(group, user);
        if (existingMembership.isPresent()) {
            GroupMembership membership = existingMembership.get();
            membership.setStatus(status);
            return membershipRepository.save(membership);
        }

        // Create new membership
        GroupMembership membership = new GroupMembership();
        membership.setUser(user);
        membership.setGroup(group);
        membership.setStatus(status);
        membership.setInvitedBy(invitedBy);
        membership.setInvitedAt(LocalDateTime.now());

        // Set up bidirectional relationships
        if (group.getMemberships() == null) {
            group.setMemberships(new ArrayList<>());
        }
        if (user.getMemberships() == null) {
            user.setMemberships(new ArrayList<>());
        }

        group.getMemberships().add(membership);
        user.getMemberships().add(membership);

        // Save the membership
        membership = membershipRepository.save(membership);

        // Save the updated entities
        groupRepository.save(group);
        userRepository.save(user);

        return membership;
    }

    @Override
    public void removeUserFromGroup(User user, Group group) {
        Optional<GroupMembership> membershipOpt = membershipRepository.findByGroupAndUser(group, user);
        if (membershipOpt.isPresent()) {
            GroupMembership membership = membershipOpt.get();

            // Remove from collections
            group.getMemberships().remove(membership);
            user.getMemberships().remove(membership);

            // Delete the membership
            membershipRepository.delete(membership);

            // Save the updated entities
            groupRepository.save(group);
            userRepository.save(user);
        }
    }

    @Override
    public List<User> getActiveUsersInGroup(Group group) {
        return membershipRepository.findByGroupAndStatus(group, MembershipStatus.ACTIVE)
                .stream()
                .map(GroupMembership::getUser)
                .toList();
    }

    @Override
    public List<Group> getActiveGroupsForUser(User user) {
        return membershipRepository.findByUserAndStatus(user, MembershipStatus.ACTIVE)
                .stream()
                .map(GroupMembership::getGroup)
                .toList();
    }

    @Override
    public GroupMembership findByUserAndGroup(User user, Group group) {
        return membershipRepository.findByGroupAndUser(group, user).orElse(null);
    }

    @Override
    public GroupMembership updateMembershipStatus(GroupMembership membership, MembershipStatus status) {
        membership.setStatus(status);
        return membershipRepository.save(membership);
    }
}