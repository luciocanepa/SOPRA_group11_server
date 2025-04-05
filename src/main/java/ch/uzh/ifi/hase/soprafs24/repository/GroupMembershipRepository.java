package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("groupMembershipRepository")
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    List<GroupMembership> findByUser(User user);
    List<GroupMembership> findByGroup(Group group);
    List<GroupMembership> findByUserAndStatus(User user, MembershipStatus status);
    List<GroupMembership> findByGroupAndStatus(Group group, MembershipStatus status);
    Optional<GroupMembership> findByGroupAndUser(Group group, User user);
} 